package com.tmdroid.securemessenger;

public class Message {
	private boolean mine;
	private String msg;
	
	public Message(String msg, boolean mine){
		this.msg = msg;
		this.mine = mine;
	}
	
	public boolean isMine(){
		return mine;
	}
	public String getMessage(){
		return msg;
	}
}
