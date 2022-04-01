package com.b_rap_radio.server.dataclasses;

import com.b_rap_radio.server.dataclasses.request_related.JSONConverter;
import com.b_rap_radio.server.dataclasses.request_related.Request;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

public class RadioUser extends User {

	private final Socket s;
	private boolean muted = false;
	private boolean voted = false;
	private short voteStatus = 0;
	private Thread outputThread;
	private final BlockingQueue<Request> requestQueue;
	private RequestSender runnable;

	private int soundPort;
	private InetAddress soundAddress;

	private BlockingQueue<String> soundQueue;
	private SoundPacketSender soundSender;


	public RadioUser(Socket socket, String email, String nick,  int credits, List<String> status) {
		super(email, nick, credits, status);
		this.s = socket;
		requestQueue = new LinkedBlockingDeque<>();
		outputThread = null;
		runnable = null;

		soundPort = -1;
		soundAddress = null;
		soundSender = null;
		soundQueue = null;
	}

	public InetAddress getAddress() {
		return this.s.getInetAddress();
	}

	public void vote(short vote) {
		this.voted = true;
		this.voteStatus = vote;
	}
	
	synchronized public boolean hasVoted() {
		return this.voted;
	}
	
	synchronized public short getVote() {
		return this.voteStatus;
	}
	
	public void resetVote() {
		this.voteStatus = 0;
		this.voted = false;
	}
	
	synchronized public boolean hasMuted() {
		return this.muted;
	}
	
	public void mute() {
		this.muted = true;
	}
	
	public void unmute() {
		this.muted = false;
	}

	/*
	 * socket client related method
	 * this TCP socket is used for request sending / receiving
	 */
	
	public Socket getClientSocket() {
		return this.s;
	}

	public void setOutputThread() throws IOException {
		runnable = new RequestSender(requestQueue, s);
		outputThread = new Thread(runnable);
		outputThread.start();
	}

	public boolean isOutputThreadOn(){ return outputThread == null; }

	public void shutdownOutputThread(){
		if(runnable != null) runnable.shutdown();
		runnable = null;
		outputThread = null;
	}

	/*
	 * sendRequest method and RequestSender inner class
	 * used for send request to client
	 * output stream and object (PrintWriter) controlled and singled to collapse any stream / request corruption
	 * TODO change thread to singlethreadpool ?
	 */

	public void sendRequest(final Request r){
		requestQueue.offer(r);
	}


	public static class RequestSender implements Runnable {

		final BlockingQueue<Request> queue;
		final Socket s;
		boolean running;
		final PrintWriter out;

		public RequestSender(BlockingQueue<Request> q, final Socket s) throws IOException {
			queue = q;
			running = true;
			this.s = s;
			out = new PrintWriter(s.getOutputStream());
		}


		@Override
		public void run() {

			while(!s.isClosed() && running){

				try {
					final Request r = queue.take();
					out.println(JSONConverter.getJSONFromRequest(r));
					out.flush();

					if(out.checkError())
						break;

				} catch (InterruptedException ignored) { }

			}

			out.close();

			try {
				s.close();
			} catch (IOException ignored) {
			}
		}

		public void shutdown(){ this.running = false; }
	}


	/*
	 * Sound socket and out object related methods
	 * output object memorized to limit instantiation
	 */

	public void resetSoundSocket(){
		soundAddress = null;
		soundPort = -1;
	}

	// UDP

	public void setSoundAddress(InetAddress address){
		this.soundAddress = address;
	}

	public void setSoundPort(int port){
		this.soundPort = port;
	}

	public InetAddress getSoundAddress(){
		return soundAddress;
	}

	public int getSoundPort(){
		return soundPort;
	}

	// TCP

	public void setSoundSocket(Socket socket) throws IOException {
		soundQueue = new LinkedBlockingQueue<>();
		soundSender = new SoundPacketSender(soundQueue, socket, this);
		new Thread(soundSender).start();
	}

	public void offerSoundPacket(String packet){
		if(soundQueue != null && soundSender != null && soundSender.isRunning()){
			soundQueue.offer(packet);
		} else {
			// needs to be totally shut down

			soundQueue = null;

			if(soundSender != null) {
				soundSender.shutdown();
				soundSender = null;
			}

			SoundUserList.getInstance().remove(this);
		}
	}

	private static class SoundPacketSender implements Runnable {

		private final BlockingQueue<String> queue;
		private final Socket socket;
		private final PrintWriter output;
		private boolean running;
		private final RadioUser instance;

		public SoundPacketSender(BlockingQueue<String> queue, Socket socket, RadioUser u) throws IOException {
			this.socket = socket;
			this.queue = queue;
			this.output = new PrintWriter(socket.getOutputStream());
			this.instance = u;
		}

		@Override
		public void run() {
			running = true;

			while(running){

				String packet;

				try {
					packet = queue.take();
				} catch (InterruptedException ignored){
					continue;
				}

				output.println(packet);
				output.flush();

				if(output.checkError() || instance == null){
					break;
				}
			}

			running = false;

			output.close();
			try {
				socket.close();
			} catch (IOException ignored) {
			}

			SoundUserList.getInstance().remove(instance);

		}

		public void shutdown() {
			this.running = false;
		}

		public boolean isRunning(){
			return running;
		}
	}
}
