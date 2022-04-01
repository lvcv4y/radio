package brap.radio.brap_radio.service;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;

import com.score.rahasak.utils.OpusDecoder;
import com.score.rahasak.utils.OpusError;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import brap.radio.brap_radio.models.JSONConverter;
import brap.radio.brap_radio.models.MusicInfos;
import brap.radio.brap_radio.models.Request;
import brap.radio.brap_radio.models.RequestTypes;


public class PlayerThread extends Thread implements TcpListener {

    public static final int SAMPLE_RATE = 48000;
    public static final int CHANNELS = 2;
    public static final int MINSIZE_TO_PLAY = 15;
    public static final int FRAME_SIZE = 2880;

    private final BlockingQueue<byte[]> queue;
    private final ClientService service;

    private boolean isMuted;
    private boolean isRunning;

    private final Object syncObject;
    private final OpusDecoder decoder;

    private final BlockingQueue<MusicInfos> musicQueue;
    private MusicInfos currentlyPlaying;

    ByteBuffer buffer;


    public PlayerThread(BlockingQueue<byte[]> queue, ClientService service, Object syncObject) {
        this.queue = queue;
        this.service = service;
        isMuted = false;
        musicQueue = new LinkedBlockingQueue<>();
        this.syncObject = syncObject;
        decoder = new OpusDecoder();
        buffer = ByteBuffer.allocate(1024*1024);
    }

    @Override
    public void run() {
        System.out.println();
        decoder.init(SAMPLE_RATE, CHANNELS);
        System.out.println();
        service.addListenerToTcpClientThread(this);

        AudioTrack audioPlayer = null;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            audioPlayer = new AudioTrack(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build(),
                    new AudioFormat.Builder()
                            .setSampleRate(SAMPLE_RATE)
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setChannelMask(AudioFormat.CHANNEL_IN_STEREO)
                            .build(),

                    SAMPLE_RATE, AudioTrack.MODE_STREAM,
                    AudioManager.AUDIO_SESSION_ID_GENERATE);



           /*audioAttr = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build();

            format = new AudioFormat.Builder()
                    .setSampleRate(44100)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_IN_STEREO)
                    .build();

            audioPlayer = new AudioTrack.Builder()
                    .setAudioAttributes(audioAttr)
                    .setAudioFormat(format)
                    .setBufferSizeInBytes(BUFFSIZE)
                    .build();*/
        }

        if(audioPlayer == null) return; // TODO log / smth ?

        audioPlayer.play();

        boolean playing = false;
        isRunning = true;
        boolean firstConnexion = true;

        while(isRunning){
            try {

                // if music server is sleeping, telling him to wake up
                if(musicQueue.size() == 0) {
                    service.tcpQuery(JSONConverter.getJSONFromRequest(new Request(RequestTypes.PLAY_NEXT)));
                }

                currentlyPlaying = musicQueue.take();

                // radio just started, or no one was connected on it for a while
                if(currentlyPlaying.getPlayedPart() == 0 && musicQueue.size() == 0 && firstConnexion) {
                    service.tcpQuery(JSONConverter.getJSONFromRequest(new Request(RequestTypes.PLAY_NEXT)));

                    // in this situation, the client already knows what is about to be played,
                    // because of GET_MUSIC_INFOS request, but the server don't know that, and will
                    // send a PLAYING request. So, let just wait for this to come in, so we don't have
                    // twice the same music. todo optimization ? (skip it from receive ? what about false positive ?)

                    //currentlyPlaying = musicQueue.take();
                }

                firstConnexion = false;
                service.refreshNotificationLargeIcon(null);

                new Thread(() -> {
                    try {
                        URL url = new URL(currentlyPlaying.getAlbumImageUrl());
                        URLConnection connection = url.openConnection();
                        connection.setUseCaches(true);

                        Object res = connection.getContent();
                        Bitmap newImg = null;

                        if(res instanceof Bitmap)
                            newImg = (Bitmap) res;
                        else if(res instanceof InputStream)
                            newImg = BitmapFactory.decodeStream((InputStream) res);
                        else
                            System.out.println("res (" + res.toString() + ") is not either bitmap or inputstream");

                        service.refreshNotificationLargeIcon(newImg);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }).start();

                Long size = currentlyPlaying.getSize();
                service.playingRequest(currentlyPlaying);
                service.setVoteStatus((short) 0);

                while(currentlyPlaying.getPlayedPart() < currentlyPlaying.getSize()){

                    if(queue.size() < MINSIZE_TO_PLAY && !playing){
                        synchronized (syncObject) {
                            try {
                                syncObject.wait();
                            } catch (InterruptedException ignored){
                                continue;
                            }
                        }
                    }

                    if(!playing)
                        playing = true;

                    try {
                        byte[] buffer = queue.take();
                        byte[] decoded = null;

                        if(buffer != null){
                            decoded = decode(buffer);

                        }

                        if(decoded != null){
                            if(!isMuted)
                                audioPlayer.write(decoded, 0, decoded.length);
                        }

                        currentlyPlaying.increasePlayedPart(FRAME_SIZE * 4);
                        service.refreshMusicProgress((double) currentlyPlaying.getPlayedPart() / size);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if(queue.size() == 0)
                        playing = false;

                }

            } catch (InterruptedException ignored) { }
        }

        audioPlayer.stop();
        audioPlayer.release();
        decoder.close();
        service.resetMusicProgress();
        service.removeListenerFromTcpClientThread(this);
        if(isRunning) isRunning = false;
    }

    public byte[] decode(byte[] encoded){
        try {
            byte[] out = buffer.array();
            int decodedSamples = decoder.decode(encoded, out, FRAME_SIZE);
            if (decodedSamples < 0) {
                System.out.println("Decode error: " + decodedSamples);
                buffer.clear();
                return null;
            }
            buffer.position(decodedSamples * 2 * 2); // 2 bytes per sample, * number_of_channels (here, 2)
            buffer.flip();

            byte[] decodedData = new byte[buffer.remaining()];
            buffer.get(decodedData);
            buffer.flip();
            return decodedData;
        } catch (OpusError | IllegalArgumentException e){
            buffer.clear();
            return null;
        }
    }

    @Override
    public void onReceivedRequest(String jsonReq) {
        Request req = JSONConverter.getRequestFromJSON(jsonReq);

        // req != null == req is PLAYING request, see JsonConverter deserializer
        if(req != null && !req.isError() && req.getMusicArgs() != null) {
            if(req.getMusicArgs().size() > 0){
                MusicInfos infos = req.getMusicArgs().get(0);

                if(currentlyPlaying == null ||
                        !(currentlyPlaying.getTitle().equals(infos.getTitle()) &&
                        currentlyPlaying.getAuthors().equals(infos.getAuthors()))) {
                    musicQueue.offer(req.getMusicArgs().get(0));
                }
            }

        }
    }

    public void shutdown(){
        queue.clear();
        isRunning = false;
    }

    public void setMuted(boolean muted){
        this.isMuted = muted;
    }
    public boolean isMuted(){
        return isMuted;
    }

    public MusicInfos getCurrentlyPlayingMusic(){
        return currentlyPlaying;
    }
}
