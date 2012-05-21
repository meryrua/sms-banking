package com.meryrua.smsbanking;

import java.util.regex.*;

import android.content.Context;

public class SMSParcer {
	private Pattern smsPattern;
	private Matcher matcherWithPattern;
	private String smsMessage = null;
	private String operationName;
	
    public static final String DEFAULT_TRANSACTION_PATTERN = "Karta\\s*\\*(\\d+);\\s*Provedena\\stranzakcija:(\\d+,\\d+)(\\w+);\\s*Data:(\\d+/\\d+/\\d+);\\s*Mesto:\\s*([\\w+\\W+\\s*]+);\\s*Dostupny\\s*Ostatok:\\s*(\\d+,\\d+)(\\w+).\\s*(\\w+)";

	public static final String DEFAULT_INCOMING_PATTERN_PLACE = "Balans vashey karty\\s*\\*(\\d+)\\spopolnilsya\\s*(\\d+/\\d+/\\d+)\\s*na:(\\d+,\\d+)(\\w+).\\s*Mesto:\\s*([\\w+\\W+\\s*]+);\\s*Dostupny\\s*Ostatok:\\s*(\\d+,\\d+)(\\w+).\\s*(\\w+)";
    public static final String DEFAULT_INCOMING_PATTERN = "Balans vashey karty\\s*\\*(\\d+)\\spopolnilsya\\s*(\\d+/\\d+/\\d+)\\s*na:(\\d+,\\d+)(\\w+).\\s*Dostupny\\s*Ostatok:\\s*(\\d+,\\d+)(\\w+).\\s*(\\w+)";

	public static final String DEFAULT_OUTGOING_PATTERN_PLACE = "Balans vashey karty\\s*\\*(\\d+)\\sumenshilsya\\s*(\\d+/\\d+/\\d+)\\s*na:(\\d+,\\d+)(\\w+).\\s*Mesto:\\s*([\\w+\\W+\\s*]+);\\s*Dostupny\\s*Ostatok:\\s*(\\d+,\\d+)(\\w+).\\s*(\\w+)";
    public static final String DEFAULT_OUTGOING_PATTERN = "Balans vashey karty\\s*\\*(\\d+)\\sumenshilsya\\s*(\\d+/\\d+/\\d+)\\s*na:(\\d+,\\d+)(\\w+).\\s*Dostupny\\s*Ostatok:\\s*(\\d+,\\d+)(\\w+).\\s*(\\w+)";
	
	//private static final String LOG_TAG = "com.meryrua.smsbanking:SMSParcer";
	
	/*private static final String testString = "Karta *1234; Provedena tranzakcija:567,33RUB; Data:23/12/2011; Mesto: any place; Dostupny Ostatok: 342,34RUB. Raiffeisenbank";
	private static final String testOutgoingString = "Balans vashey karty *1234 umenshilsya 23/12/2011 na:567,33RUR. Dostupny Ostatok: 342,34RUR. Raiffeisenbank";
	private static final String testIncomingString = "Balans vashey karty *1234 popolnilsya 23/12/2011 na:567,33RUR. Dostupny Ostatok: 342,34RUR. Raiffeisenbank";
	*/
	
	public SMSParcer(String str, Context myContext) {
		smsMessage = new String(str);
	}
	
	public SMSParcer() {
	}
	
	public void setParcedString(String str) {
	    smsMessage = new String(str);
	}
	
	public boolean isCardOperation(String patterString) {
		boolean matchFound = false;
		
		smsPattern = Pattern.compile(patterString);
		matcherWithPattern = smsPattern.matcher(smsMessage);
		
		if (matcherWithPattern.matches()) {
			operationName = TransactionData.CARD_OPERATION;
			matchFound = true;
		}
		return matchFound;
	}
	
	public boolean isIncomingFundOperation(String patterString) {
		boolean matchFound = false;
		
		smsPattern = Pattern.compile(patterString);
		matcherWithPattern = smsPattern.matcher(smsMessage);
		
		if (matcherWithPattern.matches()) {
			operationName = TransactionData.INCOMING_BANK_OPERATION;
			matchFound = true;
		}
		return matchFound;
	}

	public boolean isOutgoingFundOperation(String patterString) {
		boolean matchFound = false;
		
		smsPattern = Pattern.compile(patterString);
		matcherWithPattern = smsPattern.matcher(smsMessage);
		
		if (matcherWithPattern.matches()) {
			operationName = TransactionData.OUTGOING_BANK_OPERATION;
			matchFound = true;		
		}
		return matchFound;
	}
	
	public boolean isMatch() {
		boolean isBankSMS = false;
		
		if (SMSBankingApplication.operationPatterns.containsKey(XMLParcerSerializer.TRANSACTION_TAG)) {
		    for (int i = 0; ((i < SMSBankingApplication.operationPatterns.get(XMLParcerSerializer.TRANSACTION_TAG).size()) && (!isBankSMS)); i++) {
		        isBankSMS = isCardOperation(SMSBankingApplication.operationPatterns.get(XMLParcerSerializer.TRANSACTION_TAG).get(i));
		    }
		}
        if ((!isBankSMS) && (SMSBankingApplication.operationPatterns.containsKey(XMLParcerSerializer.INCOMING_TAG))) {
            for (int i = 0; ((i < SMSBankingApplication.operationPatterns.get(XMLParcerSerializer.INCOMING_TAG).size()) && (!isBankSMS)); i++) {
                isBankSMS = isIncomingFundOperation(SMSBankingApplication.operationPatterns.get(XMLParcerSerializer.INCOMING_TAG).get(i));
            }
        }
        if ((!isBankSMS) && (SMSBankingApplication.operationPatterns.containsKey(XMLParcerSerializer.OUTGOING_TAG))) {
            for (int i = 0; ((i < SMSBankingApplication.operationPatterns.get(XMLParcerSerializer.OUTGOING_TAG).size()) && (!isBankSMS)); i++) {
                isBankSMS = isOutgoingFundOperation(SMSBankingApplication.operationPatterns.get(XMLParcerSerializer.OUTGOING_TAG).get(i));
            }
        }
		return isBankSMS;
	}
	
	TransactionData getTransactionData() {
		TransactionData tranzactionData =  new TransactionData();
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
			if (operationName.equals(TransactionData.INCOMING_BANK_OPERATION)) {
				tranzactionData.setTransactionPlace(TransactionData.INCOMING_BANK_OPERATION);				
			} else {
				tranzactionData.setTransactionPlace(TransactionData.OUTGOING_BANK_OPERATION);				
			}
			tranzactionData.setTransactionDate(matcherWithPattern.group(2));
			tranzactionData.setTransactionValue(Float.valueOf(matcherWithPattern.group(3).replace(",", ".")).floatValue());
			tranzactionData.setTransactionCurrency(matcherWithPattern.group(4));
			tranzactionData.setFundValue(Float.valueOf(matcherWithPattern.group(5).replace(",", ".")).floatValue());
			tranzactionData.setFundCurrency(matcherWithPattern.group(6));
			tranzactionData.setBankName(matcherWithPattern.group(7));
			
		}
		return tranzactionData;

	}

}
