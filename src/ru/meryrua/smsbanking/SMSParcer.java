package ru.meryrua.smsbanking;

import java.util.regex.*;

import android.content.Context;


//TODO: in 2.1 sms from short number is saved not correct 
//(from the second word and the first one become the address)
public class SMSParcer {
	private Pattern smsPattern;
	private Matcher matcherWithPattern;
	private String smsMessage = null;
	private String operationName;
	private TransactionPattern currentPattern; 
	
    //public static final String DEFAULT_TRANSACTION_PATTERN = "Karta\\s*\\*(\\d+);\\s*Provedena\\s*tranzakcija:\\s*(\\d+,\\d+)(\\w+);\\s*Data:\\s*(\\d+/\\d+/\\d+);\\s*Mesto:\\s*([\\w+\\W+\\s*]+);\\s*Dostupny\\s*Ostatok:\\s*(\\d+,\\d+)(\\w+).\\s*(\\w+)";
    //public static final int[] DEFAULT_TRANSACTION_PATTERN_GROUP = {1, 4, 5, 2, 3, 6, 7, 8};

	public static final String DEFAULT_INCOMING_PATTERN_PLACE = "Balans vashey karty\\s*\\*(\\d+)\\s*popolnilsya\\s*(\\d+/\\d+/\\d+)\\s*na:\\s*(\\d+,\\d+)(\\w+).\\s*Mesto:\\s*([\\w+\\W+\\s*]+);\\s*Dostupny\\s*Ostatok:\\s*(\\d+,\\d+)(\\w+).\\s*(\\w+)";
    public static final int[] DEFAULT_INCOMING_PATTERN_PLACE_GROUP = {1, 2, 5, 3, 4, 6, 7, 8};
	public static final String DEFAULT_INCOMING_PATTERN = "Balans vashey karty\\s*\\*(\\d+)\\s*popolnilsya\\s*(\\d+/\\d+/\\d+)\\s*na:\\s*(\\d+,\\d+)(\\w+).\\s*Dostupny\\s*Ostatok:\\s*(\\d+,\\d+)(\\w+).\\s*(\\w+)";
    public static final int[] DEFAULT_INCOMING_PATTERN_GROUP = {1, 2, 0, 3, 4, 5, 6, 7};
    
	public static final String DEFAULT_OUTGOING_PATTERN_PLACE = "Balans vashey karty\\s*\\*(\\d+)\\s*umenshilsya\\s*(\\d+/\\d+/\\d+)\\s*na:\\s*(\\d+,\\d+)(\\w+).\\s*Mesto:\\s*([\\w+\\W+\\s*]+);\\s*Dostupny\\s*Ostatok:\\s*(\\d+,\\d+)(\\w+).\\s*(\\w+)";
    public static final int[] DEFAULT_OUTGOING_PATTERN_PLACE_GROUP = {1, 2, 5, 3, 4, 6, 7, 8};
    public static final String DEFAULT_OUTGOING_PATTERN = "Balans vashey karty\\s*\\*(\\d+)\\s*umenshilsya\\s*(\\d+/\\d+/\\d+)\\s*na:\\s*(\\d+,\\d+)(\\w+).\\s*Dostupny\\s*Ostatok:\\s*(\\d+,\\d+)(\\w+).\\s*(\\w+)";
    public static final int[] DEFAULT_OUTGOING_PATTERN_GROUP = {1, 2, 0, 3, 4, 5, 6, 7};
    
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
		
		if (SMSBankingApplication.operationPatterns.containsKey(XMLParcerSerializer.CARD_OPERATION_TAG)) {
		    for (int i = 0; ((i < SMSBankingApplication.operationPatterns.get(XMLParcerSerializer.CARD_OPERATION_TAG).size()) && (!isBankSMS)); i++) {
		        isBankSMS = isCardOperation(SMSBankingApplication.operationPatterns.get(XMLParcerSerializer.CARD_OPERATION_TAG).get(i).getPattern());
		        if (isBankSMS) {
		            currentPattern = SMSBankingApplication.operationPatterns.get(XMLParcerSerializer.CARD_OPERATION_TAG).get(i);
		        }
		    }
		}
        if ((!isBankSMS) && (SMSBankingApplication.operationPatterns.containsKey(XMLParcerSerializer.INCOMING_TAG))) {
            for (int i = 0; ((i < SMSBankingApplication.operationPatterns.get(XMLParcerSerializer.INCOMING_TAG).size()) && (!isBankSMS)); i++) {
                isBankSMS = isIncomingFundOperation(SMSBankingApplication.operationPatterns.get(XMLParcerSerializer.INCOMING_TAG).get(i).getPattern());
                if (isBankSMS) {
                    currentPattern = SMSBankingApplication.operationPatterns.get(XMLParcerSerializer.INCOMING_TAG).get(i);
                }
            }
        }
        if ((!isBankSMS) && (SMSBankingApplication.operationPatterns.containsKey(XMLParcerSerializer.OUTGOING_TAG))) {
            for (int i = 0; ((i < SMSBankingApplication.operationPatterns.get(XMLParcerSerializer.OUTGOING_TAG).size()) && (!isBankSMS)); i++) {
                isBankSMS = isOutgoingFundOperation(SMSBankingApplication.operationPatterns.get(XMLParcerSerializer.OUTGOING_TAG).get(i).getPattern());
                if (isBankSMS) {
                    currentPattern = SMSBankingApplication.operationPatterns.get(XMLParcerSerializer.OUTGOING_TAG).get(i);
                }
            }
        }
		return isBankSMS;
	}
	
	public TransactionData getTransactionData() {
		TransactionData tranzactionData =  new TransactionData();
		if (operationName.equals(TransactionData.CARD_OPERATION)) {
		    tranzactionData.setOperation(TransactionData.CARD_OPERATION);
		} else 
		    if (operationName.equals(TransactionData.INCOMING_BANK_OPERATION)) {
                tranzactionData.setOperation(TransactionData.INCOMING_BANK_OPERATION);              
            } else {
                tranzactionData.setOperation(TransactionData.OUTGOING_BANK_OPERATION);              
            }
		tranzactionData.setCardNumber(matcherWithPattern.group(currentPattern.getValue(TransactionPattern.GROUP_CARD_NUMBER)));
		tranzactionData.setTransactionValue(Float.valueOf(matcherWithPattern.group(currentPattern.getValue(TransactionPattern.GROUP_TRANSACTION_VALUE_NUMBER)).replace(",", ".")).floatValue());
		tranzactionData.setTransactionCurrency(matcherWithPattern.group(currentPattern.getValue(TransactionPattern.GROUP_TRANSACTION_CURRENCY_NUMBER)));
		tranzactionData.setTransactionDate(matcherWithPattern.group(currentPattern.getValue(TransactionPattern.GROUP_DATE_NUMBER)));
		if (currentPattern.getValue(TransactionPattern.GROUP_PLACE_NUMBER) == 0) {
		    tranzactionData.setTransactionPlace("");
		} else {
		    tranzactionData.setTransactionPlace(matcherWithPattern.group(currentPattern.getValue(TransactionPattern.GROUP_PLACE_NUMBER)));
		}
		tranzactionData.setFundValue(Float.valueOf(matcherWithPattern.group(currentPattern.getValue(TransactionPattern.GROUP_FUND_VALUE_NUMBER)).replace(",", ".")).floatValue());
		tranzactionData.setFundCurrency(matcherWithPattern.group(currentPattern.getValue(TransactionPattern.GROUP_FUND_CURRENCY_NUMBER)));
		tranzactionData.setBankName(matcherWithPattern.group(currentPattern.getValue(TransactionPattern.GROUP_BANK_NUMBER)));

		return tranzactionData;

	}

}
