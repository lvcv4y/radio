package com.b_rap_radio.server.dataclasses;


import com.b_rap_radio.server.dataclasses.request_related.Request;
import com.b_rap_radio.server.dataclasses.request_related.UserEntity;
import com.b_rap_radio.server.io_thread_classes.WebClient;

import java.util.List;

public class User {
	public User(String email, String nick, int credits, List<String> status) {
		this.nickname = nick;
		this.email = email;
		this.status = status;
		this.credits = credits;
	}
	
	public void refreshInfos() {
		Request res = DatabaseAPI.getUserInfos(this.nickname);

		if(res.isError() || res.getUserArgs() == null)
			return;

		if(res.getUserArgs().size() <= 0)
			return;

		UserEntity entity = res.getUserArgs().get(0);

		this.email = entity.getEmail();
		this.nickname = entity.getNickname();
		this.credits = entity.getCredits();
		this.status = entity.getStatus();
	}
	
	public String getNickname() {return this.nickname;}
	protected String nickname;
	
	public String getEmail() {return this.email;}
	protected String email;
	
	public List<String> getStatus() {return this.status;}
	protected List<String> status;
	
	public int getCredits() {return this.credits;}
	protected int credits;

}
