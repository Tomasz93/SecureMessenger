package com.tmdroid.securemessenger;

import java.math.BigInteger;
import java.util.Random;

public class DH {
	public Random rnd;
	public BigInteger publicP,publicG,sendValueA,sendValueB,keyA,keyB;
	public int secretValueA,secretValueB;
	DH(){};
	public void initialize(){
		rnd= new Random();
		publicP= new BigInteger(16,6,rnd);
		publicG= new BigInteger(16,9,rnd);
		secretValueA =rnd.nextInt();
		sendValueA= publicG.pow(secretValueA).mod(publicP);
	}
	
	public void answer(){
		rnd=new Random();
		secretValueB=rnd.nextInt();
		sendValueB=publicG.pow(secretValueB).mod(publicP);
	}
	
	public void calculateKeyA(){
		keyA=sendValueB.pow(secretValueA).mod(publicP);
	}
	public void calculateKeyB(){
		keyB=sendValueA.pow(secretValueB).mod(publicP);
	}
	public boolean matchKeys(){
		if(keyA==keyB)
			return true;
		else
			return false;
	}
}
