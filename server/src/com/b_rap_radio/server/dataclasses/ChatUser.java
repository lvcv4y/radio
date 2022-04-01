package com.b_rap_radio.server.dataclasses;

import com.b_rap_radio.server.dataclasses.User;

import java.net.Socket;

public class ChatUser extends User {
	
	public ChatUser(Socket s, String email, String pseudo, int credits, String status) {
		super(email, pseudo, credits, null);
		this.s = s;
	}
	
	public Socket getClientSocket() {return this.s;}
	private Socket s;
	
	
}
