package com.b_rap_radio.server.dataclasses;

public class Message {
	
	public Message(String content, ChatUser author) {
		this.content = content;
		this.author = author;
	}
	
	public String getContent() { return this.content; }
	final String content;
	
	public ChatUser getAuthor() { return this.author; }
	public String getAuthorName() { return this.author.getNickname(); }
	final ChatUser author;
}
