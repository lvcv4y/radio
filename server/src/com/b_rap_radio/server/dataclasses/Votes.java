package com.b_rap_radio.server.dataclasses;

import com.b_rap_radio.server.dataclasses.request_related.Request;
import com.b_rap_radio.server.dataclasses.request_related.RequestTypes;

public class Votes {
	private static Votes instance;
	public static final String RESET_FLAG = "RESET";

	public static Votes getInstance() {
		if(instance == null) instance = new Votes();
		return instance;
	}

	public Votes() {
		this.userList = UserList.getInstance();
		// DEBUG PURPOSES todo remove it / set to 0 / call resetVotes()
		this.numberOfPositiveVotes = 0;
		this.numberOfVotes = 0;

	}
	
	synchronized public void refreshVoteStatus() {
		resetVotes();
		for (RadioUser user : userList.getUsers()) {
			if (user.hasVoted()) {
				this.numberOfVotes++;
				if (user.getVote() == 1)
					this.numberOfPositiveVotes++;
			}
		}
	}

	synchronized public void sendRefreshingVoteRequest() {

		final Request r = new Request(RequestTypes.REFRESH_VOTES);
		r.addIntArgs(numberOfVotes, numberOfPositiveVotes);

		for(RadioUser u : userList.getUsers()){
			u.sendRequest(r);
		}
	}

	synchronized public void positiveVote(RadioUser u){
		if(u.hasVoted() && u.getVote() == (short) 1)
			return;

		if(!u.hasVoted())
			numberOfVotes++;

		numberOfPositiveVotes++;
		u.vote((short) 1);
		sendRefreshingVoteRequest();
	}

	synchronized public void negativeVote(RadioUser u){
		if(u.hasVoted() && u.getVote() == (short) -1)
			return;

		if(!u.hasVoted())
			numberOfVotes++;
		else // if user has already voted, it means he changed his positive vote to a negative one, so decrease positive votes
			numberOfPositiveVotes--;

		u.vote((short) -1);
		sendRefreshingVoteRequest();
	}

	synchronized public void disconnect(RadioUser u){
		if(u.hasVoted()){
			numberOfVotes--;

			if(u.getVote() == (short) 1)
				numberOfPositiveVotes--;
		}

		sendRefreshingVoteRequest();
	}
	
	synchronized public int getNumberOfVotes() {
		return this.numberOfVotes;
	}
	
	synchronized public int getNumberOfPositiveVotes() {
		return this.numberOfPositiveVotes;
	}
	
	synchronized public void resetVotes() {
		// debug purposes, emulate multiple users, todo set to 0
		this.numberOfPositiveVotes = 0;
		this.numberOfVotes = 0;

		final Request r = new Request(RequestTypes.REFRESH_VOTES);
		r.addStringArgs(RESET_FLAG);

		for(RadioUser u : userList.getUsers())
			u.sendRequest(r);
	}
	
	private final UserList userList;
	private int numberOfVotes;
	private int numberOfPositiveVotes;
}
