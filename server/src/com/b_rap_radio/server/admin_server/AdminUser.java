package com.b_rap_radio.server.admin_server;

import com.b_rap_radio.server.io_thread_classes.TCPHandler;

import java.net.Socket;

public class AdminUser {

    private final String nickname;
    private final Status status;
    private final Socket s;
    private TCPAdminHandler handler;
    private UDPAdminAudioHandler audioHandler;
    private boolean isConnectedToAudio;


    public AdminUser(String nick, Status status, Socket s){
        this.nickname = nick;
        this.status = status;
        this.s = s;
        this.handler = null;
        this.audioHandler = null;
        this.isConnectedToAudio = false;
    }

    public AdminUser(String nick, String status, Socket s){
        this(nick, Status.getStatusFromName(status), s);
    }

    public Socket getSocket() { return s; }

    public String getNickname() { return nickname; }

    public Status getStatus() { return status; }

    public void registerHandler(TCPAdminHandler handler){
        if(this.handler == null)
            this.handler = handler;
    }

    public void unregisterHandler(){
        this.handler = null;
    }

    public TCPAdminHandler getHandler(){
        return handler;
    }

    public void registerAudioHandler(UDPAdminAudioHandler handler){
        if(this.audioHandler == null)
            this.audioHandler = handler;
    }

    public void unregisterAudioHandler(){
        this.audioHandler = null;
    }

    public UDPAdminAudioHandler getAudioHandler(){
        return audioHandler;
    }

    public boolean isConnectedToAudio() {
        return isConnectedToAudio;
    }

    public void setConnectedToAudio(boolean connectedToAudio) {
        isConnectedToAudio = connectedToAudio;
    }

    public enum Status {

        ADMIN("ADMIN");

        private final String name;

        Status(String name){
            this.name = name;
        }

        public String getName() {return name;}

        public static Status getStatusFromName(String name){
            for(Status status : Status.values()){
                if(status.name.equals(name))
                    return status;
            }

            return null;
        }
    }
}
