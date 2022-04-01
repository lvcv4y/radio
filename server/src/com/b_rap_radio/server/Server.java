package com.b_rap_radio.server;

import com.b_rap_radio.server.admin_server.AdminServer;
import com.b_rap_radio.server.dataclasses.DatabaseAPI;
import com.b_rap_radio.server.dataclasses.PlayList;
import com.b_rap_radio.server.dataclasses.RSAConverter;
import com.b_rap_radio.server.io_thread_classes.SoundListener;
import com.b_rap_radio.server.io_thread_classes.SoundManager;
import com.b_rap_radio.server.io_thread_classes.TCPListener;

import javax.sound.sampled.UnsupportedAudioFileException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Server {
	public static void main(String[] args) throws IOException {

		new Thread(() -> {
		RSAConverter.getInstance();
		System.out.println("SERVER IS ON");
		}).start(); // init keys

		List<Integer> list = IntStream.range(1, 29).boxed().collect(Collectors.toList());
		Collections.shuffle(list);
		final PlayList pl = PlayList.getInstance();

		for(int i : list)
			pl.add(DatabaseAPI.getMusic(i));




		/*final AdminServer adminServer = new AdminServer();
		adminServer.start();*/

		final TCPListener listener = new TCPListener();
		new Thread(listener).start();

		final SoundManager soundManager = SoundManager.getInstance();
		new Thread(soundManager).start();

		final SoundListener soundListener = new SoundListener();
		new Thread(soundListener).start();

		System.out.println("started");
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			//adminServer.shutdown();
			listener.shutdown();
			soundManager.shutdown();
			soundListener.shutdown();
		}));
	}
}
