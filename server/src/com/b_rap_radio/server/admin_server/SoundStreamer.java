package com.b_rap_radio.server.admin_server;

import com.b_rap_radio.server.dataclasses.RadioUser;
import com.b_rap_radio.server.dataclasses.UserList;
import com.b_rap_radio.server.dataclasses.request_related.Event;
import com.b_rap_radio.server.io_thread_classes.SoundManager;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

public class SoundStreamer implements Runnable {

    private static SoundStreamer instance;

    public static SoundStreamer getInstance(SoundManager manager) throws IOException {
        if(instance == null) instance = new SoundStreamer(manager);
        return instance;
    }

    private boolean running;
    private boolean streaming;
    private final SoundManager soundManager;
    private final UserList users;
    private final BlockingQueue<byte[]> bufferQueue;
    private final DatagramSocket udpSocket;

    private SoundStreamer(SoundManager manager) throws IOException {
        running = true;
        streaming = false;
        soundManager = manager;
        bufferQueue = new LinkedBlockingDeque<>();
        users = UserList.getInstance();
        udpSocket = new DatagramSocket();
    }

    @Override
    public void run() {

        ExecutorService executor = Executors.newCachedThreadPool();

        while(running){
            try {
                wait();
                if(!running)
                    break;

                while(streaming){
                    byte[] buffer = new byte[0];
                    try {
                        buffer = bufferQueue.take();
                    } catch(InterruptedException ignored){ }

                    // TODO add other sound
                    // TODO modified return sound
                    if(!(buffer.length > 0)) {
                        byte[] finalBuffer = buffer;
                        for (RadioUser u : users.getUsers()) {
                            executor.execute(() -> {
                                try {
                                    udpSocket.send(new DatagramPacket(finalBuffer, finalBuffer.length,
                                            u.getSoundAddress(), u.getSoundPort()));
                                } catch (IOException ignored) { }
                            });
                        }
                    }
                }

            } catch (InterruptedException ignored) { }
        }

        executor.shutdown();
    }

    public void streamBuffer(@NotNull final byte[] buff){
        if(streaming)
            bufferQueue.offer(buff);
    }

    public void shutdown(){ this.running = false; }

    public void start(){
        this.running = true;
        new Thread(this).start();
    }

    public void startStreaming(Event e){
        soundManager.pause(e);
        bufferQueue.clear();
        streaming = true;
    }

    public void instantStart(Event e){
        soundManager.instantPause(e);
        bufferQueue.clear();
        streaming = true;
    }

    public void stopStreaming(){
        streaming = false;
        soundManager.unPause();
    }

    public boolean getStreaming(){return streaming; }
}
