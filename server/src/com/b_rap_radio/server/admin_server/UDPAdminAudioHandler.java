package com.b_rap_radio.server.admin_server;


import com.b_rap_radio.server.dataclasses.request_related.Request;
import com.b_rap_radio.server.dataclasses.request_related.RequestTypes;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPAdminAudioHandler implements Runnable {

    public static final int BUFFSIZE = 1024;

    private final DatagramSocket s;
    private final AdminUser user;
    private final InetAddress addr;
    private boolean running;
    private int serverPort;
    private final int clientPort;


    public UDPAdminAudioHandler(AdminUser user, int clientPort) throws IOException {
        this.user = user;
        this.s = new DatagramSocket();
        this.serverPort = -1; // yet not connected
        this.clientPort = clientPort;
        this.addr = user.getSocket().getInetAddress();

        this.user.registerAudioHandler(this);
    }

    @Override
    public void run() {
        s.connect(addr, clientPort);
        serverPort = s.getPort();
        running = true;

        while(running) {
            try {
                final DatagramPacket p = new DatagramPacket(new byte[BUFFSIZE], BUFFSIZE);
                s.receive(p);
                new GlobalQuery(p.getData());

            } catch (IOException e){
                e.printStackTrace();
                break;
            }
        }

        user.setConnectedToAudio(false);
        user.unregisterAudioHandler();

        final Request res = new Request(RequestTypes.CHANNEL_DISCONNECT.getName());
        res.addStringArgs(user.getNickname());
        for(AdminUser u: AdminUserList.getInstance().getUsers()){
            u.getHandler().query(res);
        }

        s.close();
    }

    public void query(byte[] buff){
        if(running)
            new Query(s, buff, addr, clientPort);
    }

    public int getServerPort() { return serverPort; }

    public void shutdown(){ this.running = false; }


    public static class GlobalQuery implements Runnable {
        private final byte[] buffer;

        public GlobalQuery(byte[] buffer){
            this.buffer = buffer;
            new Thread(this).start();
        }

        @Override
        public void run() {
            AdminUserList list = AdminUserList.getInstance();

            for(AdminUser u: list.getUsers()){
                if(u.isConnectedToAudio())
                    u.getAudioHandler().query(buffer);
            }
        }
    }


    public static class Query implements Runnable {
        private final DatagramSocket s;
        private final DatagramPacket p;

        public Query(DatagramSocket s, byte[] buffer, InetAddress addr, int port) {
            this.s = s;
            this.p = new DatagramPacket(buffer, buffer.length, addr, port);
            new Thread(this).start();
        }

        @Override
        public void run() {
            try {
                s.send(p);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

