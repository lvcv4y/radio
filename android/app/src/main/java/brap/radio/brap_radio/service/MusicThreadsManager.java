package brap.radio.brap_radio.service;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import brap.radio.brap_radio.models.JSONConverter;
import brap.radio.brap_radio.models.MusicInfos;
import brap.radio.brap_radio.models.Request;
import brap.radio.brap_radio.models.RequestTypes;


public class MusicThreadsManager {
    // private UDPReceiverThread receiverThread;
    private TCPReceiverThread receiverThread;
    private PlayerThread playerThread;
    private final ClientService service;
    private boolean isRunning;


    public MusicThreadsManager(ClientService service){
        receiverThread = null;
        playerThread = null;
        this.service = service;
        isRunning = false;
    }

    public void initThreads() {
        new Thread(() -> {
            try {
                final BlockingQueue<byte[]> queue = new LinkedBlockingQueue<>();
                final Object syncObject = new Object();
                receiverThread = new TCPReceiverThread(queue, service, syncObject);
                System.out.println();
                playerThread = new PlayerThread(queue, service, syncObject);

                receiverThread.initSocket();
                receiverThread.start();
                playerThread.start();
                service.tcpQuery(JSONConverter.getJSONFromRequest(new Request(RequestTypes.GET_MUSIC_INFOS)));
                isRunning = true;
            } catch (IOException e){
                e.printStackTrace();
                shutdownThreads();
                service.onErrorOnMusicReceiverInit();
            }
        }).start();
    }

    public void shutdownThreads(){
        receiverThread.shutdown();
        playerThread.shutdown();
        isRunning = false;
    }

    public void setMuted(boolean muted){
        if (playerThread != null) {
            playerThread.setMuted(muted);
            service.refreshMuteStatus(muted);
        }
    }

    public boolean isMuted() {
        if(playerThread != null)
            return playerThread.isMuted();

        return false;
    }

    public void switchMuted(){
        if(playerThread != null)
            setMuted(!playerThread.isMuted());
    }

    public MusicInfos getCurrentlyPlayingMusic(){
        if(playerThread != null)
            return playerThread.getCurrentlyPlayingMusic();

        return null;
    }

    public boolean isRunning() {
        return isRunning;
    }
}
