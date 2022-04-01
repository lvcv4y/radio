package com.b_rap_radio.server.admin_server;

import com.b_rap_radio.server.dataclasses.PORTS;
import com.b_rap_radio.server.dataclasses.request_related.Request;
import com.b_rap_radio.server.dataclasses.request_related.RequestTypes;
import com.b_rap_radio.server.dataclasses.request_related.UserEntity;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class AdminSoundListener implements Runnable {

    private final DatagramSocket s;
    private boolean running;
    private final AdminUserList userlist;

    public AdminSoundListener() throws IOException {
        s = new DatagramSocket(PORTS.ADMIN_CHANNEL.value);
        userlist = AdminUserList.getInstance();
        running = true;
    }

    @Override
    public void run() {

        while(running){
            try {
                final DatagramPacket p = new DatagramPacket(new byte[10], 10);
                s.receive(p);

                final InetAddress addr = p.getAddress();
                AdminUser user = null;

                for(AdminUser u : userlist.getUsers()){
                    if(u.getSocket().getInetAddress().equals(addr) && !u.isConnectedToAudio()) // TODO what if on same network (=same public ip) ?
                        user = u;
                }

                if(user == null){
                    continue;
                }

                user.setConnectedToAudio(true);
                new Thread(new UDPAdminAudioHandler(user, p.getPort())).start(); // TODO use of executor / selector ??

                final Request ping = new Request(RequestTypes.CHANNEL_CONNECT.getName());
                ping.addUserArgs(new UserEntity(-1, null, user.getNickname(), user.getStatus().getName(), 0));
                new Thread(new TCPAdminHandler.GeneralQuery(ping, true, user)).start(); // TODO use of executor / selector ?
            } catch (IOException e){
                e.printStackTrace();
                // TODO LOG
            }
        }
    }

    public void shutdown() {this.running = false;}
}
