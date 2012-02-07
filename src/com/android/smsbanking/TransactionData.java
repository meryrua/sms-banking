package com.android.smsbanking;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;

import android.util.Log;

/*
 *Maybe we should save SMS receiving date... ?
 *Also we should know if it debit or kredit (oplata ili postuplenie) 
*/
public class TransactionData {
	
	//data from parced SMS
	public static final String CARD_NUMBER = "cardNumber";
	public static final String TRANSACTION_DATE = "transactionDate";
	public static final String TRANSACTION_PLACE = "transactionPlace";
	public static final String TRANSACTION_CURRENCY = "transactionCurrency";
	public static final String FUND_CURRENCY = "fundCurrency";
	public static final String BANK_NAME = "bankName";
	public static final String TRANSACTION_VALUE = "transactionValue";
	public static final String FUND_VALUE = "fundValue";
	public static final String DEFAULT_BANK_NAME = "Raiffeisen";
	private String cardNumber;
	private String transactionDate;
	private String transactionPlace;
	private String transactionCurrency;
	private String fundCurrency;
	private String bankName;
	private float transactionValue = 0;
	private float fundValue = 0;
	
	public static final int numberOfField = 8;
	public static final String PARCED_DATA = "parced_data";
	
	
	TransactionData(){
		cardNumber = null;
		transactionDate = null;
		transactionPlace = null;
		transactionCurrency = null;
		fundCurrency = null;
		bankName = null;
	}
	
	void setCardNumber(String number){
		cardNumber = new String(number);
	}
	
	String getCardNumber(){
		return cardNumber;
	}

	void setTransactionDate(String date){
		transactionDate = new String(date);
	}
	
	String getTransactionDate(){
		return transactionDate;
	}

	void setTransactionPlace(String place){
		transactionPlace = new String(place);
	}
	
	String getTransactionPlace(){
		return transactionPlace;
	}

	void setTransactionCurrency(String currency){
		transactionCurrency = new String(currency);
	}
	
	String getTransactionCurrency(){
		return transactionCurrency;
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
	
	void setTransactionValue(float value){
		transactionValue = value;
	}
	
	float getTransactionValue(){
		return transactionValue;
	}

	void setFundValue(float value){
		fundValue = value;
	}
	
	float getFundValue(){
		return fundValue;
	}

}
