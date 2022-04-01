package com.b_rap_radio.server.io_thread_classes;

import com.b_rap_radio.server.dataclasses.*;
import com.b_rap_radio.server.dataclasses.request_related.Request;
import com.b_rap_radio.server.dataclasses.request_related.RequestTypes;
import de.jarnbjo.ogg.EndOfOggStreamException;
import de.jarnbjo.ogg.FileStream;
import de.jarnbjo.ogg.LogicalOggStream;
//import org.jitsi.impl.neomedia.codec.audio.opus.Opus;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Collection;
import java.util.concurrent.*;


public class SoundSender {
	public static final int ERROR = -2;
	public static final int FORCED_CUT = -1;
	public static final int NO_ERROR = 0;

    //private final static int SOUND_BUFFER_SIZE = 48000;
    //public static final AudioFormat FORMAT = new AudioFormat(SOUND_BUFFER_SIZE, 16, 2, true, false);

    private final SoundUserList userList;
    private Exception lastException;
    private final Votes votes;
    private boolean instantStop;
    private Music currentMusic;
    private final ExecutorService threadPool;
    //private long opusState;
    private ByteBuffer buffer;

    // private final DatagramSocket sender;


    public SoundSender() {
        this.userList = SoundUserList.getInstance();
        this.instantStop = false;
        this.threadPool = Executors.newCachedThreadPool();
        this.votes = Votes.getInstance();
        lastException = null;

        //sender = new DatagramSocket();
    }

    public int play(Music music) {
        /* TODO
         * A MODIFIER
         * Ou qu'il récup les sons dispo a X endroit selon la playlist, si les sons sont stockés en dur
         */
        int positiveVotes;
        int totalVotes;
        FileStream fs;
		currentMusic = music;
		currentMusic.resetPlayedPart();
		//opusState = Opus.decoder_create(Music.SAMPLES_RATE, Music.CHANNELS);
		buffer = ByteBuffer.allocate(1024*1024);

        // get stream with specific format (the same one is used by the apps to read the buffers)
        try {
            fs = new FileStream(new RandomAccessFile(currentMusic.getMusicFile(), "r"));
        } catch (IOException e) {
            e.printStackTrace();
			lastException = e;
			currentMusic = null; // TODO null or saved ?
            return ERROR;
        }

        Request req = new Request(RequestTypes.PLAYING.getName());
        req.addMusicInfosArgs(currentMusic.getInfos(0, 0));

        // ping all connected users about the music that will be played
        for (RadioUser user : userList.getUsers()) {
            user.resetVote();
            user.sendRequest(req);
        }

        for (LogicalOggStream stream : (Collection<LogicalOggStream>) fs.getLogicalStreams()) {
            try {
                byte[] nextPacket = stream.getNextOggPacket();

                while (nextPacket != null) {

                    if(!isPacketPlayable(nextPacket)){
                        System.out.println("NOT DECODABLE PACKET SPOTTED");
                        nextPacket = new byte[nextPacket.length];
                    }

                    if (instantStop) {
                        return FORCED_CUT;
                    }

                    final String base64Packet = Base64.getEncoder().encodeToString(nextPacket);
                    for (RadioUser u : userList.getUsers()) {
                        /*threadPool.execute(() -> {
                        }); nop bc of sync pbs */
                        u.offerSoundPacket(base64Packet);
                    }

                    // update music data to keep track of played part (warranty that new user will receive correct infos)
                    currentMusic.increasePlayedPart(Music.FRAME_SIZE * 4);

                    // check if 50% >= of users has voted positively and music has been played for 30sec
                    // if not, skip the music

                    // votes.refreshVoteStatus();
                    positiveVotes = this.votes.getNumberOfPositiveVotes();
                    totalVotes = this.votes.getNumberOfVotes();

                    int playedDuration = (int) ((int) (currentMusic.getPlayedPart() * currentMusic.getDuration()) / currentMusic.getSize());

                    if (positiveVotes < (totalVotes / 2) && playedDuration >= 30) {
                        System.out.println("[SoundSender] SKIPPING SITUATION DETECTED");
                        break;
                    }

                    nextPacket = stream.getNextOggPacket();

                    // wait the time taken by the buffer to be played (44100o using this format == 1/4 sec of music)
                    // todo wait until someone request to the server to play
                    try {
                        Thread.sleep(55);
                    } catch (InterruptedException ignored) {
                    }

                }


            } catch (IOException e1) {
                if(!(e1 instanceof EndOfOggStreamException)){
                    lastException = e1;
                    currentMusic = null; // TODO null or saved ?
                    return ERROR;
                }
            }
        }

        try {
            fs.close();
        } catch (IOException ignored) {
        }
        currentMusic = null;
        return NO_ERROR;
    }

    private boolean isPacketPlayable(byte[] packet){
        //return Opus.decode(opusState, packet, 0, packet.length, buffer.array(), 0, Music.FRAME_SIZE, 0) > 0;
        return true;
    }

    public Music getCurrentMusic(){ return currentMusic; }

	public Exception getLastException() { return lastException; }

	public void shutdown(){
    	threadPool.shutdown();
    	instantStop = true;
	}

	public void instantStop(){instantStop = true;}
}
