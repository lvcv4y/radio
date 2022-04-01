package radio;

import org.jitsi.impl.neomedia.codec.audio.opus.Opus;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.LinkedBlockingQueue;

public class ReceiverAndPlayer extends Thread {


    LinkedBlockingQueue<byte[]> queue;
    Socket s;
    final Player player;
    boolean running;

    public ReceiverAndPlayer(Socket s){
        this.s = s;
        queue = new LinkedBlockingQueue<>();
        player = new Player(queue);
    }

    @Override
    public void run() {
        System.out.println("[Receiver] Starting...");
        final BufferedReader is;
        try {
             is = new BufferedReader(new InputStreamReader(s.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        System.out.println("[Receiver] got input, starting player and waiting for packets...");

        player.start();


        running = true;
        while(running){
            try {
                final String base64Buffer = is.readLine();

                queue.offer(Base64.getDecoder().decode(base64Buffer));

            } catch (IOException e){
                e.printStackTrace();
                break;
            }
        }

        try {
            is.close();
        } catch (IOException ignored) {
        }

        try {
            s.close();
        } catch (IOException ignored) {
        }
    }

    public void shutdown(){this.running = false;}

    public static class Player extends Thread {

        public static final int DECODE_BUFF_SIZE = 1024*1024*2;
        private final AudioFormat audioFormat = new AudioFormat(48000, 16, 2, true, false);

        private final long opusState;
        private final ByteBuffer decodeBuffer;

        LinkedBlockingQueue<byte[]> queue;
        boolean running;
        public Player(LinkedBlockingQueue<byte[]> queue){
            this.queue = queue;
            decodeBuffer = ByteBuffer.allocate(DECODE_BUFF_SIZE);
            opusState = Opus.decoder_create(48000, 2);
        }

        @Override
        public void run() {
            System.out.println("[Player] Starting...");
            running = true;
            SourceDataLine speaker;
            try {
                speaker = AudioSystem.getSourceDataLine(audioFormat);
                speaker.open();
            } catch (LineUnavailableException e) {
                e.printStackTrace();
                return;
            }
            speaker.start();

            System.out.println("[Player] speaker set, waiting for packets...");

            while(running){
                try {
                    byte[] toPlay = decode(queue.take());

                    if(toPlay != null)
                        speaker.write(toPlay, 0, toPlay.length);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

            speaker.drain();
            speaker.close();
        }

        private byte[] decode(byte[] packetData) {

            //int frameSize = Opus.decoder_get_nb_samples(opusState, packetData, 0, packetData.length);
            int decodedSamples = Opus.decode(opusState, packetData, 0, packetData.length,
                    decodeBuffer.array(), 0, 2880, 0);

            if (decodedSamples < 0) {
                System.out.println("Decode error: " + decodedSamples);
                //decodeBuffer.clear();
                return null;
            }

            decodeBuffer.position(decodedSamples * 2 * 2); // 2 bytes per sample, * number_of_channels (here, 2)
            decodeBuffer.flip();

            byte[] decodedData = new byte[decodeBuffer.remaining()];
            decodeBuffer.get(decodedData);
            decodeBuffer.flip();
            System.out.println(decodedData.length);
            return decodedData;
        }

        public void shutdown(){this.running = false;}
    }
}
