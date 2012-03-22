package com.android.smsbanking;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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
	
	public static final String INCOMING_BANK_OPERATION = "popolnilsya";
	public static final String OUTGOING_BANK_OPERATION = "umenshilsya";
	public static final String CARD_OPERATION = "card_operation";	
	
	TransactionData(){
		cardNumber = null;
		transactionDate = null;
		transactionPlace = null;
		transactionCurrency = null;
		fundCurrency = null;
		bankName = null;
	}
	
	TransactionData(Cursor transactionCursor){
		setCardNumber(transactionCursor.getString(transactionCursor.getColumnIndex(TransactionData.CARD_NUMBER)));
		setFundCurrency(transactionCursor.getString(transactionCursor.getColumnIndex(TransactionData.FUND_CURRENCY)));
		setTransactionCurrency(transactionCursor.getString(transactionCursor.getColumnIndex(TransactionData.TRANSACTION_CURRENCY)));
		setTransactionDate(transactionCursor.getString(transactionCursor.getColumnIndex(TransactionData.TRANSACTION_DATE)));
		setTransactionPlace(transactionCursor.getString(transactionCursor.getColumnIndex(TransactionData.TRANSACTION_PLACE)));
		setFundValue(transactionCursor.getFloat(transactionCursor.getColumnIndex(TransactionData.FUND_VALUE)));
		setTransactionValue(transactionCursor.getFloat(transactionCursor.getColumnIndex(TransactionData.TRANSACTION_VALUE)));
		//setBankName(transactionCursor.getString(transactionCursor.getColumnIndex(TransactionData.BANK_NAME)));
	}
	
	TransactionData(Bundle extras){
	   	setTransactionValue(extras.getFloat(TransactionData.TRANSACTION_VALUE, 0));
       	setFundValue(extras.getFloat(TransactionData.FUND_VALUE, 0));
       	//setBankName(extras.getString(TransactionData.BANK_NAME));
       	setCardNumber(extras.getString(TransactionData.CARD_NUMBER));
       	setFundCurrency(extras.getString(TransactionData.FUND_CURRENCY));
       	setTransactionCurrency(extras.getString(TransactionData.TRANSACTION_CURRENCY));
       	setTransactionDate(extras.getString(TransactionData.TRANSACTION_DATE));
       	setTransactionPlace(extras.getString(TransactionData.TRANSACTION_PLACE));
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
	
	public void fillIntent(Intent intent){
		intent.putExtra(TransactionData.TRANSACTION_VALUE, getTransactionValue());
		intent.putExtra(TransactionData.FUND_VALUE, getFundValue());
		//intent.putExtra(TransactionData.BANK_NAME, getBankName());
		intent.putExtra(TransactionData.CARD_NUMBER, getCardNumber());
		intent.putExtra(TransactionData.FUND_CURRENCY, getFundCurrency());
		intent.putExtra(TransactionData.TRANSACTION_CURRENCY, getTransactionCurrency());
		intent.putExtra(TransactionData.TRANSACTION_DATE, getTransactionDate());
		intent.putExtra(TransactionData.TRANSACTION_PLACE, getTransactionPlace());
	}

}
