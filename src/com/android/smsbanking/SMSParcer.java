package com.android.smsbanking;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;

import android.util.Log;


//Should we create new Parcer for each new SMS from bank???
public class SMSParcer {

	private static Pattern smsPattern;
	private static Matcher matcherWithPatter;
	private static String smsMessage = null;
	
	private static List<String> tokenArray;
	private static String defaultPatter = "Karta\\s\\*(\\d+);\\sProvedena\\stranzakcija:(\\d+,\\d+)(RUB|EUR|USD);\\sData:(\\d+/\\d+/\\d+);\\sMesto:\\s([\\w+\\s*]+);\\sDostupny\\sOstatok:\\s(\\d+,\\d+)(RUB|EUR|USD).\\s(\\w+)";
	
	private static String testString = "Karta *1234; Provedena tranzakcija:567,33RUB; Data:23/12/2011; Mesto: any place; Dostupny Ostatok: 342,34RUB. Raiffeisenbank";
	
	SMSParcer(String str, String pattern){
		smsMessage = new String(str);
		smsPattern = Pattern.compile(pattern);
		matcherWithPatter = smsPattern.matcher(smsMessage);
	}
	
	SMSParcer(){
		smsMessage = new String(testString);
		smsPattern = Pattern.compile(defaultPatter);
		tokenArray = new ArrayList<String>();
		matcherWithPatter = smsPattern.matcher(smsMessage);
	}	
	boolean isMatch(){
		boolean matchFound = false;
		int i = 0;
		
		if (matcherWithPatter.matches())
		{
		matchFound = matcherWithPatter.find();
		
		Log.d("NATALIA!!!", "number = % d " + matcherWithPatter.groupCount());
		
		for (int j = 1; j <= matcherWithPatter.groupCount(); j++){
			Log.d("NATALIA!!!", "number %d " + j + " = % d " + matcherWithPatter.group(j));
		}
		}
		else 
			Log.d("NATALIA!!!", "do mot match");
		return matchFound;
	}
}
