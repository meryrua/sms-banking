package com.android.smsbanking;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import android.util.Log;

/*
 *Maybe we should save SMS receiving date... ?
 *Also we should know if it debit or kredit (oplata ili postuplenie) 
*/
public class TranzactionData {
	
	//data from parced SMS
	public static final String CARD_NUMBER = "cardNumber";
	public static final String TRANZACTION_DATE = "tranzactionDate";
	public static final String TRANZACTION_PLACE = "tranzactionPlace";
	public static final String TRANZACTION_CURRENCY = "tranzactionCurrency";
	public static final String FUND_CURRENCY = "fundCurrency";
	public static final String BANK_NAME = "bankName";
	public static final String TRANZACTION_VALUE = "tranzactionValue";
	public static final String FUND_VALUE = "fundValue";
	private static String cardNumber;
	private static String tranzactionDate;
	private static String tranzactionPlace;
	private static String tranzactionCurrency;
	private static String fundCurrency;
	private static String bankName;
	private static float tranzactionValue = 0;
	private static float fundValue = 0;
	
	public static final int numberOfField = 8;
	public static final String PARCED_DATA = "parced_data";
	
	
	TranzactionData(){
		cardNumber = null;
		tranzactionDate = null;
		tranzactionPlace = null;
		tranzactionCurrency = null;
		fundCurrency = null;
		bankName = null;
	}
	
	void setCardNumber(String number){
		cardNumber = new String(number);
	}
	
	String getCardNumber(){
		return cardNumber;
	}

	void setTranzactionDate(String date){
		tranzactionDate = new String(date);
	}
	
	String getTranzactionDate(){
		return tranzactionDate;
	}

	void setTranzactionPlace(String place){
		tranzactionPlace = new String(place);
	}
	
	String getTranzactionPlace(){
		return tranzactionPlace;
	}

	void setTranzactionCurrency(String currency){
		tranzactionCurrency = new String(currency);
	}
	
	String getTranzactionCurrency(){
		return tranzactionCurrency;
	}

	void setFundCurrency(String currency){
		fundCurrency = new String(currency);
	}
	
	String getFundCurrency(){
		return fundCurrency;
	}

	void setBankName(String name){
		bankName = new String(name);
	}
	
	String getBankName(){
		return bankName;
	}
	
	void setTranzactionValue(float value){
		tranzactionValue = value;
	}
	
	float getTranzactionValue(){
		return tranzactionValue;
	}

	void setFundValue(float value){
		fundValue = value;
	}
	
	float getFundValue(){
		return fundValue;
	}

}
