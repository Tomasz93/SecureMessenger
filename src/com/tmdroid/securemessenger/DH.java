package com.tmdroid.securemessenger;

import java.math.BigInteger;
import java.util.Random;

public class DH {
	public Random rnd;
	public BigInteger publicP,publicG,mySendValue,key;
	public int mySecretValue;
	public void initialize(){
		rnd= new Random();
		publicP= new BigInteger(16,6,rnd);
		publicG= new BigInteger(16,9,rnd);
	}
	
	public void calculateMySecretValue(){
		mySecretValue =rnd.nextInt();
		mySendValue= publicG.pow(mySecretValue).mod(publicP);
	}
	
	public void calculateKey(){
		key=mySendValue.pow(mySecretValue).mod(publicP);
	}

}
