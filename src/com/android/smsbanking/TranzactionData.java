package com.android.smsbanking;

import java.util.regex.Matcher;

import android.util.Log;

public class TranzactionData {
	private static String[] dataForTranzaction; //maybe it's needed to make 8 fields?
	
	public static final int numberOfField = 8;
	
	TranzactionData(){
		dataForTranzaction = new String[numberOfField];
	}
	
	void setData(String[] dataFromSMS){
		for (int i = 0; i < numberOfField; i++){
			dataForTranzaction[i] = dataFromSMS[i];
		}
	}

	String getItem(int i){
		return dataForTranzaction[i];
	}
	
	void logElemnts(){
		for (int i = 0; i < numberOfField; i++){
			Log.d("NATALIA!!! trans", " data " + dataForTranzaction[i]);
		}
	}
}
