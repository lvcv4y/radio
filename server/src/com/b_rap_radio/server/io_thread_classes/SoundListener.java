package com.b_rap_radio.server.io_thread_classes;

import com.b_rap_radio.server.dataclasses.PORTS;
import com.b_rap_radio.server.dataclasses.RadioUser;
import com.b_rap_radio.server.dataclasses.SoundUserList;
import com.b_rap_radio.server.dataclasses.UserList;

import java.io.IOException;
import java.net.*;

public class SoundListener implements Runnable {
    private boolean running;
    //private final DatagramSocket s;
    private final ServerSocket s;
    private final UserList userlist;
    private final SoundUserList connectedUsers;

    public SoundListener() throws IOException {
        //this.s = new DatagramSocket(PORTS.RADIO_SOUND_SERVER.value);
        this.s = new ServerSocket(PORTS.RADIO_SOUND_SERVER.value);
        this.userlist = UserList.getInstance();
        this.connectedUsers = SoundUserList.getInstance();
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            try {

                /*
                final DatagramPacket p = new DatagramPacket(new byte[1], 1);
                s.receive(p);

                final InetAddress newComerAddress = p.getAddress();

                boolean contains = false;

                for (RadioUser u : connectedUsers.getUsers()) {
                    if (u.getClientSocket().getInetAddress().equals(newComerAddress)) {
                        contains = true;
                    }
                }

                if (contains) {
                    continue;
                }

                for (RadioUser user : userlist.getUsers()) {

                    final InetAddress userAddress = user.getClientSocket().getInetAddress();

                    if (userAddress.equals(newComerAddress)) {
                        user.setSoundAddress(newComerAddress);
                        user.setSoundPort(p.getPort());
                        connectedUsers.add(user);
                        break;
                    }
                }

                */

                final Socket newComerSocket = s.accept();

                boolean contains = false;

                for (RadioUser u : connectedUsers.getUsers()) {
                    if (u.getClientSocket().getInetAddress().equals(newComerSocket.getInetAddress())) {
                        contains = true;
                    }
                }

                if (contains) {
                    newComerSocket.close();
                    continue;
                }

                for (RadioUser user : userlist.getUsers()) {

                    final InetAddress userAddress = user.getClientSocket().getInetAddress();

                    if (userAddress.equals(newComerSocket.getInetAddress())) {
                        user.setSoundSocket(newComerSocket);
                        connectedUsers.add(user);
                        break;
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
                // TODO LOG
            }
        }
    }

    public void shutdown() {
        this.running = false;
    }
}
