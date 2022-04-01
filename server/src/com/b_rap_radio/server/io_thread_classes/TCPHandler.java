package com.b_rap_radio.server.io_thread_classes;

import com.b_rap_radio.server.dataclasses.*;
import com.b_rap_radio.server.dataclasses.request_related.JSONConverter;
import com.b_rap_radio.server.dataclasses.request_related.Request;
import com.b_rap_radio.server.dataclasses.request_related.RequestTypes;
import com.google.gson.JsonParseException;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TCPHandler implements Runnable {

	static final List<TCPHandler> activeHandlers = Collections.synchronizedList(new ArrayList<>());

	public TCPHandler(RadioUser user) {
		System.out.println("new TCP client instantiated !");
		this.user = user;
		this.playlist = PlayList.getInstance();
		this.historique = Historique.getInstance();
		this.userList = UserList.getInstance();
		this.s = user.getClientSocket();
		this.votes = Votes.getInstance();
		eventList = EventList.getInstance();
		this.running = true;
	}
	
	@Override
	public void run() {
		System.out.println("running..");
		BufferedReader input;
		Request ans;
		try {
			input = new BufferedReader(new InputStreamReader(s.getInputStream()));
			user.setOutputThread();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}


		boolean ending = false;

		while (!this.s.isClosed() && running) {

			try {
				final Request r;
				final String strReq = input.readLine();

				try {
					r = JSONConverter.getRequestFromJSON(String.valueOf(strReq));
				} catch (JsonParseException | NullPointerException e) { // nullptr thrown when no type or no isError
					System.out.println("[TCPHandler from " + user.getNickname() + "] : illegal json =>");
					System.out.println(strReq);
					System.out.println("[END ILLEGAL JSON]");
					user.sendRequest(new Request(RequestTypes.ERROR.getName(), "JSON PARSE ERROR"));
					continue;
				}

				final RequestTypes type;
				try {
					type = RequestTypes.getFromName(r.getType());
				} catch (NullPointerException e) { // input.readLine() returning shit (r == null), means client just disconnected
					break;
				}

				if(type == null){
					user.sendRequest(new Request(RequestTypes.ERROR.getName(), "UNKNOWN CMD"));
					continue;
				}

				if(type != RequestTypes.GET_VOTES_INFOS)
					System.out.println("new request spotted ! : type : ".concat(type.getName()));

				switch (type) {

					case PLAY_NEXT:
						System.out.println("[TcpHandler from " + user.getNickname() + "] : notifying SoundManager...");
						SoundManager.playNext();
						break;

					case MUTE:
						this.user.mute();
						break;

					case UNMUTE:
						this.user.unmute();
						break;

					case POSITIVE_VOTE:
						votes.positiveVote(user);
						break;

					case NEGATIVE_VOTE:
						votes.negativeVote(user);
						break;

					case GET_VOTES_INFOS:
						// this.votes.refreshVoteStatus();
						ans = new Request(type);
						ans.addIntArgs(this.votes.getNumberOfVotes(), this.votes.getNumberOfPositiveVotes());
						user.sendRequest(ans);
						break;

					case END:
						user.sendRequest(new Request(type));
						ending = true;
						break;

					case GET_MUSIC_INFOS:
						ans = new Request(RequestTypes.PLAYING);
						ans.addMusicInfosArgs(playlist.getCurrentMusic().getInfos(votes.getNumberOfVotes(), votes.getNumberOfPositiveVotes()));
						user.sendRequest(ans);
						break;

					/*case ADD_MUSIC:
						if (r.getMusicArgs() != null && !r.getMusicArgs().isEmpty()) {
							MusicInfos infos = r.getMusicArgs().get(0);
							File musicFile = new File(PATH.concat(infos.getAlbumName().concat("/").concat(infos.getTitle().concat(".wav"))));
							if (musicFile.exists()) {
								if (!historique.isInHistorique(infos)) {
									Music music = new Music(infos.getTitle(), infos.getAlbumName(), infos.getAuthors(), musicFile, 0L, user.getNickname());
									this.playlist.add(music);
									user.sendRequest(new Request(RequestTypes.ADDED.getName()));
								} else {
									user.sendRequest(new Request(type.getName(), "already"));
								}
							} else {
								user.sendRequest(new Request(type.getName(), "FILE NOT FOUND"));
							}
						}
						break;*/

					case GET_HISTORY:
						final List<Integer> intArgs = r.getIntArgs();
						if (intArgs.size() >= 2 && intArgs.get(1) > intArgs.get(0)) {
							ans = new Request(type.getName());
							for (int i = intArgs.get(0); i < intArgs.get(1); i++) {

								if (historique.getMusicInfos(i) == null)
									break;

								ans.addMusicInfosArgs(historique.getMusicInfos(i));
							}

							user.sendRequest(ans);
						} else {
							user.sendRequest(new Request(type.getName(), "BAD ARGS"));
						}

						break;

					case GET_EVENT:
						eventList.refresh();
						ans = new Request(type.getName());
						ans.setEventArgs(eventList.getEventList());
						user.sendRequest(ans);
						break;

					case CHAT_MSG:
						if(r.getStringArgs() != null && r.getStringArgs().size() >= 1){

							r.setStringArgs(Arrays.asList(user.getNickname(), r.getStringArgs().get(0), user.getStatus().get(0)));
							generalQuery(r, true);

						} else {
							user.sendRequest(new Request(RequestTypes.CHAT_MSG, "BAD ARGS"));
						}

						break;


					default:
						user.sendRequest(new Request(RequestTypes.ERROR.getName(), "UNKNOWN CMD"));
						break;
				}


				if (ending)
					break;

			} catch (IOException e) {
				e.printStackTrace();
				break;

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		Votes.getInstance().disconnect(user);
		userList.remove(user);
		SoundUserList.getInstance().remove(user);
		user.shutdownOutputThread();
		user.resetSoundSocket();

		try {
			s.close();
		} catch (IOException ignored) { }
	}

	private void generalQuery(Request res, boolean autoPing){

		if(autoPing) {
			for (RadioUser u : userList.getUsers())
				u.sendRequest(res);
		} else {
			for(RadioUser u : userList.getUsers()){
				if(!u.equals(user))
					u.sendRequest(res);
			}
		}
	}

	public void shutdown(){
		this.running = false;
	}
	
	private final Socket s;
	private final RadioUser user;
	private final UserList userList;
	private final PlayList playlist;
	private final Historique historique;
	private final Votes votes;
	private final EventList eventList;
	private boolean running;
}
