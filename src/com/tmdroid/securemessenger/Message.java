package com.tmdroid.securemessenger;

public class Message {
	private boolean mine;
	private String msg;
	private boolean info;
	
	public Message(String msg, boolean mine, boolean info){
		this.msg = msg;
		this.mine = mine;
		this.info = info;
	}

	public boolean isMine(){
		return mine;
	}
	public boolean isInfo(){
		return info;
	}
	public String getMessage(){
		return msg;
	}
}
