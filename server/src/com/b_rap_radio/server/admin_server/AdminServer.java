package com.b_rap_radio.server.admin_server;

import java.io.IOException;

public class AdminServer {
    final AdminSoundListener listener;
    final AdminTCPListener tcpListener;

    public AdminServer() throws IOException {
        listener = new AdminSoundListener();
        tcpListener = new AdminTCPListener();
    }

    public void start(){
        new Thread(listener).start();
        new Thread(tcpListener).start();
    }

    public void shutdown(){
        listener.shutdown();
        tcpListener.shutdown();
    }
}
