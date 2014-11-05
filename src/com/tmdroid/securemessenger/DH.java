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
	
	public static int[] findPrime(int n){
		int N[] = new int[n];
		for(int i=2; i<n; i++){
			N[i-2] = i;
		}

		int p = 2;
		int next = 0;
		while(p*p <= n){
			for(int i=p; i<=n; i++){
				if(p != i && N[i-2] != 0){
					if(N[i-2]%p == 0){
						N[i-2] = 0;
					}else{
						if(next == 0){
						next = N[i-2];
						}
					}
				}
			}
			p = next;
			next = 0;
		}
		return N;
	}

}
