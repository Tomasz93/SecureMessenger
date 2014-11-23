package com.tmdroid.securemessenger;

import android.annotation.SuppressLint;

import java.math.BigInteger;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.spec.SecretKeySpec;

public class DH {
	@SuppressLint("TrulyRandom") 
	public SecureRandom rnd = new SecureRandom();
	public BigInteger publicP, publicG, publicR;
	public BigInteger myPublicKey, yourPublicKey;
	private int myPrivateKey;
	public byte[] secretKey = null;
	public boolean success = false;
	public Key key;
	
	public void initialize(){
		publicP = BigInteger.probablePrime(512, rnd);
		publicG = BigInteger.probablePrime(32, rnd);
	}
	
	public void getInitializedValues(BigInteger p, BigInteger g){
		publicP = p;
		publicG = g;
	}
	
	public void calculateMyKeys(){
		myPrivateKey = rnd.nextInt(2000);
		myPublicKey = (publicG.pow(myPrivateKey)).mod(publicP);
	}
	
	public void getYourPublicKey(BigInteger b){
		yourPublicKey = b;
	}
	
	public void calculateSecretKey(){
		secretKey = hashSecretKey((yourPublicKey.pow(myPrivateKey)).mod(publicP).toByteArray());
		key = new SecretKeySpec(secretKey, 0, secretKey.length, "AES");
	}
	
	public byte[] hashSecretKey(byte[] key){
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
			byte[] hashedKey = md.digest(key);
			return hashedKey;
		} catch (NoSuchAlgorithmException e) {
			return key;
		}
	}
	
	

}
