package com.android.smsbanking;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;

import android.util.Log;


//Should we create new Parcer for each new SMS from bank???
public class SMSParcer {

	private static Pattern smsPattern;
	private static Matcher matcherWithPattern;
	private static String smsMessage = null;
	
	private static List<String> tokenArray;
	private static String defaultPatter = "Karta\\s\\*(\\d+);\\sProvedena\\stranzakcija:(\\d+,\\d+)(RUB|EUR|USD);\\sData:(\\d+/\\d+/\\d+);\\sMesto:\\s([\\w+\\s*]+);\\sDostupny\\sOstatok:\\s(\\d+,\\d+)(RUB|EUR|USD).\\s(\\w+)";
	
	private static String testString = "Karta *1234; Provedena tranzakcija:567,33RUB; Data:23/12/2011; Mesto: any place; Dostupny Ostatok: 342,34RUB. Raiffeisenbank";
	
	SMSParcer(String str, String pattern){
		smsMessage = new String(str);
		smsPattern = Pattern.compile(pattern);
		matcherWithPattern = smsPattern.matcher(smsMessage);
	}
	
	SMSParcer(){
		smsMessage = new String(testString);
		smsPattern = Pattern.compile(defaultPatter);
		matcherWithPattern = smsPattern.matcher(smsMessage);
	}	
	
	boolean isMatch(){
		boolean matchFound = false;
		int i = 0;
		
		if (matcherWithPattern.matches())
		{
		matchFound = matcherWithPattern.find();
		
		Log.d("NATALIA!!!", "number = % d " + matcherWithPattern.groupCount());
		
		for (int j = 1; j <= matcherWithPattern.groupCount(); j++){
			Log.d("NATALIA!!!", "number %d " + j + " = % d " + matcherWithPattern.group(j));
		}
		}
		else 
			Log.d("NATALIA!!!", "do mot match");
		return matchFound;
	}
	
	void setTranzactionData(TranzactionData tranzactionData){
		tranzactionData.setCardNumber(matcherWithPattern.group(1));
		tranzactionData.setTranzactionValue(Float.valueOf(matcherWithPattern.group(2).replace(",", ".")).floatValue());
		tranzactionData.setTranzactionCurrency(matcherWithPattern.group(3));
		tranzactionData.setTranzactionDate(matcherWithPattern.group(4));
		tranzactionData.setTranzactionPlace(matcherWithPattern.group(5));
		tranzactionData.setFundValue(Float.valueOf(matcherWithPattern.group(6).replace(",", ".")).floatValue());
		tranzactionData.setFundCurrency(matcherWithPattern.group(7));
		tranzactionData.setBankName(matcherWithPattern.group(8));
	}
}
