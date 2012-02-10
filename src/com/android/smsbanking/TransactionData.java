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
	
	private static float currentBalance = 0; //But we should have several Balances (for each of funds number) and currency
	private static String balanceCurrency; //But we should have several Balances (for each of funds number) and currency
	
	
	TransactionData(){
		cardNumber = null;
		transactionDate = null;
		transactionPlace = null;
		transactionCurrency = null;
		fundCurrency = null;
		bankName = null;
	}
	
	public void setCardNumber(String number){
		cardNumber = new String(number);
	}
	
	public String getCardNumber(){
		return cardNumber;
	}

	public void setTransactionDate(String date){
		transactionDate = new String(date);
	}
	
	public String getTransactionDate(){
		return transactionDate;
	}

	public void setTransactionPlace(String place){
		transactionPlace = new String(place);
	}
	
	public String getTransactionPlace(){
		return transactionPlace;
	}

	public void setTransactionCurrency(String currency){
		transactionCurrency = new String(currency);
	}
	
	public String getTransactionCurrency(){
		return transactionCurrency;
	}

	public void setFundCurrency(String currency){
		fundCurrency = new String(currency);
	}
	
	public String getFundCurrency(){
		return fundCurrency;
	}

	public void setBankName(String name){
		bankName = new String(name);
	}
	
	public String getBankName(){
		return bankName;
	}
	
	public void setTransactionValue(float value){
		transactionValue = value;
	}
	
	public float getTransactionValue(){
		return transactionValue;
	}

	public void setFundValue(float value){
		fundValue = value;
	}
	
	public float getFundValue(){
		return fundValue;
	}

	static public void setBalance(float balance){
		currentBalance = balance;
	}
	
	static public float getBalance(){
		return currentBalance;
	}
	
	static public void setBalanceCurrency(String currency){
		balanceCurrency = new String(currency);
	}
	
	static public String getBalanceCurrency(){
		return balanceCurrency;
	}

}