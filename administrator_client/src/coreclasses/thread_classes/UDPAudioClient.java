package coreclasses.thread_classes;

import controllers.AlertBox;
import coreclasses.RequestListener;
import coreclasses.RequestTypes;
import coreclasses.dataclasses.JSONConverter;
import coreclasses.dataclasses.Request;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class UDPAudioClient implements RequestListener {
    public static final int SERVER_PORT = 60607;
    public static final String IP = "192.168.0.34";
    public static final int BUFFSIZE = 1024;
    public static final AudioFormat FORMAT = new AudioFormat(44100, 16, 2, true, true);

    public static UDPAudioClient instance;

    public static UDPAudioClient getInstance() {
        if(instance == null) instance = new UDPAudioClient();
        return instance;
    }

    private DatagramSocket s;
    private Recorder recorder;
    private Receiver receiver;
    private boolean connected;
    private int port;
    private final TCPClient client;

    private UDPAudioClient() {
        client = TCPClient.getInstance();
        this.s = null;
        recorder = null;
        receiver = null;
        this.connected = false;
        this.port = 0;
    }

    @Override
    public void onReceived(Request request) {
        if(RequestTypes.GET_PORT.getName().equals(request.getType()) && request.getIntArgs() != null && request.getIntArgs().size() > 0)
            this.setPort(request.getIntArgs().get(0));

    }

    public void connect() {
        if(!connected){
            try {
                client.registerListener(this);
                this.s = new DatagramSocket();
                final DatagramPacket p = new DatagramPacket(new byte[10], 10, InetAddress.getByName(IP), SERVER_PORT);
                s.send(p);

                this.receiver = new Receiver(s);
                this.recorder = new Recorder(s, port);

                new Thread(receiver).start();
                new Thread(recorder).start();

                client.query(new Request(RequestTypes.GET_PORT.getName()));

                connected = true;
            } catch(IOException e){
                e.printStackTrace();
                AlertBox.ExceptionError("ERREUR", "Erreur lors de la connexion au serveur vocal",
                        "une erreur est survenue lors de la mise en place des threads liés à la partie audio", e);
                disconnect();
            }
        }
    }

    public void disconnect(){
        if(receiver != null){
            receiver.shutdown();
            receiver = null;
        }

        if(recorder != null){
            recorder.shutdown();
            recorder = null;
        }

        if(s != null){
            s.close();
            s = null;
        }

        client.unregisterListener(this);
        connected = false;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        System.out.println("[AUDIOCLIENT] port set to ".concat(String.valueOf(port)));
        this.port = port;
        recorder.setPort(port);
    }


    private static class Receiver implements Runnable {

        private boolean running;
        private final Player player;
        private final DatagramSocket s;

        public Receiver(DatagramSocket s){
            this.s = s;
            this.running = false;
            this.player = new Player();
        }

        @Override
        public void run() {
            running = true;

            while(running){
                try {
                    final DatagramPacket p = new DatagramPacket(new byte[BUFFSIZE], BUFFSIZE);
                    s.receive(p);
                    player.addBuffer(p.getData());
                } catch (IOException e){
                    e.printStackTrace();
                    UDPAudioClient.getInstance().disconnect();
                    break;
                    //TODO LOG
                }
            }

        }

        public void shutdown() {
            running = false;
            player.shutdown();
        }
    }

    private static class Player implements Runnable {
        private boolean running;
        private final BlockingQueue<byte[]> queue;

        public Player(){
            this.queue = new LinkedBlockingDeque<>();
            new Thread(this).start();
        }

        public void addBuffer(byte[] buffer){
            this.queue.offer(buffer);
        }

        @Override
        public void run() {
            running = true;
            SourceDataLine out;

            try {
                out = AudioSystem.getSourceDataLine(FORMAT);
                out.open(FORMAT);
            } catch (LineUnavailableException e) {
                e.printStackTrace();
                UDPAudioClient.getInstance().disconnect();
                //TODO LOG error
                return;
            }

            out.start();

            while(running){
                try {
                    out.write(queue.take(), 0, BUFFSIZE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            out.flush();
            out.close();
            out.stop();
        }

        public void shutdown(){running = false;}
    }

    public static class Recorder implements Runnable {

        private boolean running;
        private final Sender sender;

        public Recorder(DatagramSocket s, int port) {
            this.sender = new Sender(s, port);
            running = true;
        }

        public void setPort(int port){ sender.setPort(port); }

        @Override
        public void run() {
            TargetDataLine microphone;
            try {
                microphone = AudioSystem.getTargetDataLine(FORMAT);
                microphone.open(FORMAT);
                microphone.start();
            } catch (LineUnavailableException e) {
                e.printStackTrace();
                UDPAudioClient.getInstance().disconnect();
                return;
            }



            while(running){

                byte[] buffer = new byte[BUFFSIZE];

                microphone.read(buffer, 0, buffer.length);
                /*if(calculateLevel(buffer,0, (buffer.length - 1)) > 0.0001f){
                    sender.addBuffer(buffer);
                }*/

                sender.addBuffer(buffer);
            }
            microphone.stop();
            microphone.close();
        }

        private float calculateLevel (byte[] buffer, int readPoint, int leftOver) {
            float level;
            int max = 0;
            boolean use16Bit = (FORMAT.getSampleSizeInBits() == 16);
            boolean signed = (FORMAT.getEncoding() ==
                    AudioFormat.Encoding.PCM_SIGNED);
            boolean bigEndian = (FORMAT.isBigEndian());

            if (use16Bit) {
                for (int i=readPoint; i<buffer.length-leftOver; i+=2) {
                    int value = 0;
                    // deal with endianness
                    int hiByte = (bigEndian ? buffer[i] : buffer[i+1]);
                    int loByte = (bigEndian ? buffer[i+1] : buffer [i]);
                    if (signed) {
                        short shortVal = (short) hiByte;
                        shortVal = (short) ((shortVal << 8) | (byte) loByte);
                        value = shortVal;
                    } else {
                        value = (hiByte << 8) | loByte;
                    }
                    max = Math.max(max, value);
                } // for

            } else {
                // 8 bit - no endianness issues, just sign
                for (int i=readPoint; i<buffer.length-leftOver; i++) {
                    int value = 0;
                    if (signed) {
                        value = buffer [i];
                    } else {
                        short shortVal = 0;
                        shortVal = (short) (shortVal | buffer [i]);
                        value = shortVal;
                    }
                    max = Math.max (max, value);
                } // for
            } // 8 bit

            // express max as float of 0.0 to 1.0 of max value
            // of 8 or 16 bits (signed or unsigned)
            if (signed) {
                if (use16Bit) { level = (float) max / Short.MAX_VALUE; }
                else { level = (float) max / Byte.MAX_VALUE; }
            } else {
                if (use16Bit) { level = (float) max / 0xffff; }
                else { level = (float) max / 0xff; }
            }

            return level;
        }

        public void shutdown(){
            running = false;
            sender.shutdown();
        }
    }

    private static class Sender implements Runnable {

        private final BlockingQueue<byte[]> queue;
        private final DatagramSocket s;
        private boolean running;
        private int port;

        public Sender(DatagramSocket s, int port){
            this.queue = new LinkedBlockingDeque<>();
            this.s = s;
            this.port = port;
            new Thread(this).start();
        }

        public void setPort(int port){ this.port = port; }

        public void addBuffer(byte[] buffer){
            this.queue.offer(buffer);
        }

        @Override
        public void run() {

            InetAddress addr;

            try {
                addr = InetAddress.getByName(IP);
            } catch (UnknownHostException e) {
                e.printStackTrace();
                //todo log
                UDPAudioClient.getInstance().disconnect();
                return;
            }

            running = true;
            byte[] b;

            while(running){
                try {
                    b = queue.take();
                    final DatagramPacket p = new DatagramPacket(b, b.length, addr, port);
                    s.send(p);
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();

                    if(e instanceof IOException){
                        UDPAudioClient.getInstance().disconnect();
                        break;
                        // TODO log
                    }
                }
            }
        }

        public void shutdown() {running = false;}

    }

}
