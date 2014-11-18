package com.tmdroid.securemessenger;

import android.annotation.SuppressLint;
import java.math.BigInteger;
import java.security.SecureRandom;

public class DH {
	@SuppressLint("TrulyRandom") 
	public SecureRandom rnd = new SecureRandom();
	public BigInteger publicP, publicG;
	public BigInteger myPublicKey, yourPublicKey;
	private int myPrivateKey;
	public BigInteger secretKey = null;
	
	public void initialize(){
		publicP = BigInteger.probablePrime(1024, rnd);
		publicG = new BigInteger(160, rnd);
	}
	
	public void getInitializedValues(BigInteger p, BigInteger g){
		publicP = p;
		publicG = g;
	}
	
	public void calculateMyKeys(){
		myPrivateKey = rnd.nextInt(200);
		myPublicKey = (publicG.pow(myPrivateKey)).mod(publicP);
	}
	
	public void getYourPublicKey(BigInteger b){
		yourPublicKey = b;
	}
	
	public void calculateSecretKey(){
		secretKey = (yourPublicKey.pow(myPrivateKey)).mod(publicP);
	}

}
