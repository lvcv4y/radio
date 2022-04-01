package brap.radio.brap_radio.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import brap.radio.brap_radio.models.JSONConverter;
import brap.radio.brap_radio.models.Request;
import brap.radio.brap_radio.models.RequestTypes;

interface TcpListener {
    void onReceivedRequest(String jsonReq);
}

public class TcpClientThread implements Runnable {

    public static final int PORT = 32768;
    public static final String SERVER_IP = /*"192.168.1.105";*/ "88.161.217.130";

    private final List<TcpListener> listeners;

    private boolean isRunning;
    private Socket socket;
    private BufferedReader input;
    private short voteStatus;
    private final ClientService service;



    public TcpClientThread(ClientService service) {
        listeners = new ArrayList<>();
        this.service = service;

        
    }

    public void initAndStartThread() {
        System.out.println("[TCP Client] initializing...");
        if (socket == null){
            new Thread(() -> {
                try {
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(SERVER_IP, PORT), 3000);

                    input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    new Thread(TcpClientThread.this).start();
                    service.onSuccessOnTcpInit();
                } catch(IOException e){
                    // if(e is SocketTimeoutException) -> timeout error

                    e.printStackTrace();
                    service.onErrorOnTcpInit();
                    if(input != null)
                        try{ input.close(); } catch (IOException ignored){}

                    if(socket != null)
                        try{ socket.close(); } catch (IOException ignored){}

                    input = null;
                    socket = null;
                }
            }).start();
        } else
            service.onErrorOnTcpInit();
    }

    @Override
    public void run() {

        if(socket == null) return;

        isRunning = true;
        while(isRunning) {
            try {
                String jsonReq = input.readLine();

                new Thread(() -> {
                    for(TcpListener listener : listeners)
                        listener.onReceivedRequest(jsonReq);
                }).start();

            } catch (IOException e) {
                e.printStackTrace();
                service.onConnectionError(e);
                break;
            }
        }
        try {
            input.close();
        } catch (IOException ignored) { }

        try {
            socket.close();
        } catch (IOException ignored) { }

        input = null;
        socket = null;
        if(isRunning) isRunning = false;
    }

    public void addListener(TcpListener listener){
        listeners.add(listener);
    }

    public void removeListener(TcpListener listener){
        listeners.remove(listener);
    }

    public void shutdown(){
        isRunning = false;
    }

    public short getVoteStatus() {
        return voteStatus;
    }

    public void setVoteStatus(short voteStatus, boolean notify) {

        if(socket != null){

            if(voteStatus != 0) // "null" vote (no vote yet, so no need to send anything)
                query(new Request(voteStatus == -1 ? RequestTypes.NEGATIVE_VOTE : RequestTypes.POSITIVE_VOTE));

            this.voteStatus = voteStatus;
            if(notify)
                service.refreshVoteStatus(voteStatus);
        }
    }

    public void query(Request req){
        query(JSONConverter.getJSONFromRequest(req));
    }

    public void query(String jsonReq){
        new Thread(() -> {
            if(socket != null) {
                try {
                    final PrintWriter out = new PrintWriter(socket.getOutputStream());
                    out.println(jsonReq);
                    out.flush();
                } catch (IOException ignored) {
                }
            }
        }).start();
    }

    public boolean isRunning(){
        return isRunning;
    }

}
