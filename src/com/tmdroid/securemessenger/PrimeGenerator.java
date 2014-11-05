package com.tmdroid.securemessenger;

import java.math.BigInteger;

public class PrimeGenerator {
	private int number;
	private int tab[];
	private int highest;
	public void generatePrime(int n){
		number=n;
		highest=2;
		tab[0]=2;
		for (int j=0;j<n;j++){
			if(tab[j]!=0 && tab[j]*tab[j]<number){
				for(int i=2;i<=n;i++){
					if(i%tab[j]==0)
						tab[i-1]=0;
					else
						tab[i-1]=i;	
				}
			}
		}
		for(int m=0;m<n;m++){
			if(tab[m]>highest)
				highest=tab[m];
		}
	}
}
