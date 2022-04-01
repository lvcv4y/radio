package com.b_rap_radio.server.admin_server;

import com.b_rap_radio.server.dataclasses.PORTS;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class AdminTCPListener implements Runnable {
    public AdminTCPListener() {
        try {
            this.s = new ServerSocket(PORTS.ADMIN_SERVER_LISTENER.value);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while(this.running) {
            Socket newClient;
            try {
                newClient = s.accept();
                new Thread(new AdminIdentificationHandler(newClient)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void shutdown() {
        this.running = false;
    }


    private ServerSocket s;
    private boolean running = true;

}
