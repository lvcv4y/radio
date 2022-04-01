package com.b_rap_radio.server.io_thread_classes;

import com.b_rap_radio.server.dataclasses.PORTS;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPListener implements Runnable {
	
	public TCPListener() {
		this.webclientPool = Executors.newCachedThreadPool();
		try {
			this.s = new ServerSocket(PORTS.RADIO_TCP_LISTENER.value);
			s.setSoTimeout(10);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		Socket newClient;

		while(this.running) {
			try {
				newClient = s.accept();
				System.out.println("new client spotted !");
				webclientPool.execute(new WebClient(newClient));
			} catch (IOException e) {
				if(!(e instanceof SocketTimeoutException))
					e.printStackTrace();
			}
		}

		try {
			s.close();
		} catch (IOException ignored) { }
	}

	public void shutdown() {
		this.running = false;

		webclientPool.shutdown();

		final List<WebClient> clients = WebClient.activeWebClients;
		for(WebClient c : clients)
			c.shutdown();

		final List<TCPHandler> handlers = TCPHandler.activeHandlers;
		for(TCPHandler h : handlers)
			h.shutdown();

	}
	
	
	private ServerSocket s;
	private boolean running = true;
	private final ExecutorService webclientPool;
}
