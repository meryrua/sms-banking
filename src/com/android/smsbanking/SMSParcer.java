package com.android.smsbanking;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;

import android.util.Log;


//Should we create new Parcer for each new SMS from bank???
public class SMSParcer {

	private Pattern smsPattern;
	private Matcher matcherWithPattern;
	private String smsMessage = null;
	private String operationName;
	
	private List<String> tokenArray;
//	private static final String DEFAULT_TRANSACTION_PATTERN = "Karta\\s\\*(\\d+);\\sProvedena\\stranzakcija:(\\d+,\\d+)(\\w+);\\sData:(\\d+/\\d+/\\d+);\\sMesto:\\s([[\\w\\-\\,\\.]+\\s*]+);\\sDostupny\\sOstatok:\\s(\\d+,\\d+)(\\w+).\\s(\\w+)";
	private static final String DEFAULT_TRANSACTION_PATTERN = "Karta\\s*\\*(\\d+);\\s*Provedena\\stranzakcija:(\\d+,\\d+)(\\w+);\\s*Data:(\\d+/\\d+/\\d+);\\s*Mesto:\\s*([[\\w-,.]+\\s*]+);\\s*Dostupny\\s*Ostatok:\\s*(\\d+,\\d+)(\\w+).\\s*(\\w+)";

	private static final String DEFAULT_INCOMING_PATTERN = "Balans vashey karty\\s*\\*(\\d+)\\spopolnilsya\\s*(\\d+/\\d+/\\d+)\\s*na:(\\d+,\\d+)(\\w+).\\s*Dostupny\\s*Ostatok:\\s*(\\d+,\\d+)(\\w+).\\s*(\\w+)";

	private static final String DEFAULT_OUTGOING_PATTERN = "Balans vashey karty\\s*\\*(\\d+)\\sumenshilsya\\s*(\\d+/\\d+/\\d+)\\s*na:(\\d+,\\d+)(\\w+).\\s*Dostupny\\s*Ostatok:\\s*(\\d+,\\d+)(\\w+).\\s*(\\w+)";
	
	private static final String testString = "Karta *1234; Provedena tranzakcija:567,33RUB; Data:23/12/2011; Mesto: any place; Dostupny Ostatok: 342,34RUB. Raiffeisenbank";
	private static final String testOutgoingString = "Balans vashey karty *1234 umenshilsya 23/12/2011 na:567,33RUR. Dostupny Ostatok: 342,34RUR. Raiffeisenbank";
	private static final String testIncomingString = "Balans vashey karty *1234 popolnilsya 23/12/2011 na:567,33RUR. Dostupny Ostatok: 342,34RUR. Raiffeisenbank";
	
	SMSParcer(String str, String pattern){
		smsMessage = new String(str);
		smsPattern = Pattern.compile(pattern);
		matcherWithPattern = smsPattern.matcher(smsMessage);
	}
	
	SMSParcer(String str){
		smsMessage = new String(str);
		//smsPattern = Pattern.compile(DEFAULT_TRANSACTION_PATTERN);
		//matcherWithPattern = smsPattern.matcher(smsMessage);
	}
	
	SMSParcer(){
		smsMessage = new String(testString);
		smsPattern = Pattern.compile(DEFAULT_TRANSACTION_PATTERN);
		matcherWithPattern = smsPattern.matcher(smsMessage);
	}
	
	public boolean isCardOperation(){
		boolean matchFound = false;
		int i = 0;
		
		smsPattern = Pattern.compile(DEFAULT_TRANSACTION_PATTERN);
		matcherWithPattern = smsPattern.matcher(smsMessage);
		
		if (matcherWithPattern.matches())
		{
			operationName = TransactionData.CARD_OPERATION;
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
	
	public boolean isIncomingFundOperation(){
		boolean matchFound = false;
		int i = 0;
		
		smsPattern = Pattern.compile(DEFAULT_INCOMING_PATTERN);
		matcherWithPattern = smsPattern.matcher(smsMessage);
		
		if (matcherWithPattern.matches())
		{
			operationName = TransactionData.INCOMING_BANK_OPERATION;
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

	public boolean isOutgoingFundOperation(){
		boolean matchFound = false;
		int i = 0;
		
		smsPattern = Pattern.compile(DEFAULT_OUTGOING_PATTERN);
		matcherWithPattern = smsPattern.matcher(smsMessage);
		
		if (matcherWithPattern.matches())
		{
			operationName = TransactionData.OUTGOING_BANK_OPERATION;
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

	
	public boolean isMatch(){
		return (isCardOperation() || isIncomingFundOperation() || isOutgoingFundOperation());
	}
	
	void setTranzactionData(TransactionData tranzactionData){
		if (operationName.equals(TransactionData.CARD_OPERATION)) {
			tranzactionData.setCardNumber(matcherWithPattern.group(1));
			tranzactionData.setTransactionValue(Float.valueOf(matcherWithPattern.group(2).replace(",", ".")).floatValue());
			tranzactionData.setTransactionCurrency(matcherWithPattern.group(3));
			tranzactionData.setTransactionDate(matcherWithPattern.group(4));
			tranzactionData.setTransactionPlace(matcherWithPattern.group(5));
			tranzactionData.setFundValue(Float.valueOf(matcherWithPattern.group(6).replace(",", ".")).floatValue());
			tranzactionData.setFundCurrency(matcherWithPattern.group(7));
			tranzactionData.setBankName(matcherWithPattern.group(8));
		} else {
			tranzactionData.setCardNumber(matcherWithPattern.group(1));
			if (operationName.equals(TransactionData.INCOMING_BANK_OPERATION)){
				tranzactionData.setTransactionPlace(TransactionData.INCOMING_BANK_OPERATION);				
			}else {
				tranzactionData.setTransactionPlace(TransactionData.OUTGOING_BANK_OPERATION);				
			}
			tranzactionData.setTransactionDate(matcherWithPattern.group(2));
			tranzactionData.setTransactionValue(Float.valueOf(matcherWithPattern.group(3).replace(",", ".")).floatValue());
			tranzactionData.setTransactionCurrency(matcherWithPattern.group(4));
			tranzactionData.setFundValue(Float.valueOf(matcherWithPattern.group(5).replace(",", ".")).floatValue());
			tranzactionData.setFundCurrency(matcherWithPattern.group(6));
			tranzactionData.setBankName(matcherWithPattern.group(7));
			
		}
	}
}
