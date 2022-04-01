package brap.radio.brap_radio.service;

import android.util.Base64;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class TCPReceiverThread extends Thread {
    public static final int SERVER_PORT = 32769;

    private final Object syncObject;
    private final BlockingQueue<byte[]> queue;
    private boolean isRunning;
    private Socket socket;
    private BufferedReader input;
    private final ClientService service;


    public TCPReceiverThread(BlockingQueue<byte[]> queue, ClientService service, Object syncObject) {
        this.queue = queue;
        this.syncObject = syncObject;
        input = null;
        this.service = service;
    }

    public void initSocket() throws IOException {
        if(socket == null){
            try {
                socket = new Socket(TcpClientThread.SERVER_IP, SERVER_PORT);
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e){
                e.printStackTrace();
                socket.close();
                socket = null;
                throw e;
            }
        }

    }

    @Override
    public void run() {
        isRunning = true;
        boolean needToNotify = true;

        while(isRunning){
            try {
                final String base64Buffer = input.readLine();

                if(base64Buffer == null){
                    System.out.println("NULL BUFFER SPOTTED");
                    // todo notify user and disconnect him
                    break;
                }

                queue.offer(Base64.decode(base64Buffer, Base64.DEFAULT));

                if(needToNotify && queue.size() > PlayerThread.MINSIZE_TO_PLAY){
                    synchronized (syncObject){
                        syncObject.notify(); // must change to notifyAll() if more than one thing wait for this
                    }

                    needToNotify = false;
                } else if(!needToNotify && queue.size() <= PlayerThread.MINSIZE_TO_PLAY) // playing thread will wait
                    needToNotify = true;
            } catch (IOException e){
                e.printStackTrace();
                break;
            }
        }

        try {
            input.close();
        } catch (IOException ignored){
        }

        try {
            socket.close();
        } catch (IOException ignored) {
        }

        socket = null;
        input = null;
        if(isRunning) isRunning = false;
    }

    public void shutdown(){
        isRunning = false;
    }
}
