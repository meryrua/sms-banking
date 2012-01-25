package com.android.smsbanking;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;


//Should we create new Parcer for each new SMS from bank???
public class SMSParcer {

	private static Pattern smsPattern;
	private static Matcher matcherWithPatter;
	private static String smsMessage = null;
	
	private static List<String> tokenArray;
	
	private static String testString = "Karta *1234; Provedena tranzakcija: 567RUB;";
	
	SMSParcer(String str, String pattern){
		smsMessage = new String(testString);
		smsPattern = Pattern.compile("[a-zA-Z\\d]");
		matcherWithPatter = smsPattern.matcher(smsMessage);
	}
	
	SMSParcer(){
		smsMessage = new String(testString);
		smsPattern = Pattern.compile("[a-zA-Z\\d]+");
		tokenArray = new ArrayList<String>();
		matcherWithPatter = smsPattern.matcher(smsMessage);
	}	
	boolean isMatch(){
		boolean matchFound = false;
		int i = 0;
		
		while (matcherWithPatter.find()){
			tokenArray.add(new String(matcherWithPatter.group()));
			i++;
		}
		return matchFound;
	}
}
