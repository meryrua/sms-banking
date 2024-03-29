package ru.meryrua.smsbanking;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

//���� � �������� � �������� ������� �����! ��������� �� ���

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
	public static final String OPERATION_NAME = "operationName";
	public static final String DEFAULT_BANK_NAME = "Raiffeisen";
	private String cardNumber;
	private String transactionDate;
	private String transactionPlace;
	private String transactionCurrency;
	private String fundCurrency;
	private String operationName;
	private String bankName;
	private float transactionValue = 0;
	private float fundValue = 0;
	
	public static final int numberOfField = 8;
	public static final String PARCED_DATA = "parced_data";
	
	public static final String INCOMING_BANK_OPERATION = "popolnilsya";
	public static final String OUTGOING_BANK_OPERATION = "umenshilsya";
	public static final String CARD_OPERATION = "card_operation";	
	
	TransactionData() {
		cardNumber = null;
		transactionDate = null;
		transactionPlace = null;
		transactionCurrency = null;
		fundCurrency = null;
		operationName = null;
		bankName = null;
	}
	
	TransactionData(Cursor transactionCursor) {
		setCardNumber(transactionCursor.getString(transactionCursor.getColumnIndex(TransactionData.CARD_NUMBER)));
		setFundCurrency(transactionCursor.getString(transactionCursor.getColumnIndex(TransactionData.FUND_CURRENCY)));
		setTransactionCurrency(transactionCursor.getString(transactionCursor.getColumnIndex(TransactionData.TRANSACTION_CURRENCY)));
		setTransactionDate(transactionCursor.getString(transactionCursor.getColumnIndex(TransactionData.TRANSACTION_DATE)));
		setTransactionPlace(transactionCursor.getString(transactionCursor.getColumnIndex(TransactionData.TRANSACTION_PLACE)));
		setFundValue(transactionCursor.getFloat(transactionCursor.getColumnIndex(TransactionData.FUND_VALUE)));
		setTransactionValue(transactionCursor.getFloat(transactionCursor.getColumnIndex(TransactionData.TRANSACTION_VALUE)));
		setOperation(transactionCursor.getString(transactionCursor.getColumnIndex(TransactionData.OPERATION_NAME)));
		setBankName(transactionCursor.getString(transactionCursor.getColumnIndex(TransactionData.BANK_NAME)));
	}
	
	TransactionData(Bundle extras) {
	   	setTransactionValue(extras.getFloat(TransactionData.TRANSACTION_VALUE, 0));
       	setFundValue(extras.getFloat(TransactionData.FUND_VALUE, 0));
       	setBankName(extras.getString(TransactionData.BANK_NAME));
       	setCardNumber(extras.getString(TransactionData.CARD_NUMBER));
       	setFundCurrency(extras.getString(TransactionData.FUND_CURRENCY));
       	setTransactionCurrency(extras.getString(TransactionData.TRANSACTION_CURRENCY));
       	setTransactionDate(extras.getString(TransactionData.TRANSACTION_DATE));
       	setOperation(extras.getString(TransactionData.OPERATION_NAME));
       	setTransactionPlace(extras.getString(TransactionData.TRANSACTION_PLACE));
	}
	
	public void setCardNumber(String number) {
		if (number != null) {
		    DebugLogging.log("setCardNumber");
		    cardNumber = new String(number);
		}
	}
	
	public String getCardNumber() {
		return cardNumber;
	}

	public void setTransactionDate(String date) {
		if (date != null) {
	        DebugLogging.log("setTransactionDate");
		    transactionDate = new String(date);
		}
	}
	
	public String getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionPlace(String place) {
		if (place != null) {
	        DebugLogging.log("setTransactionPlace");
		    transactionPlace = new String(place);
		}
	}
	
	public String getTransactionPlace() {
		return transactionPlace;
	}

	public void setTransactionCurrency(String currency) {
		if (currency != null) {
            DebugLogging.log("setTransactionCurrency");
		    transactionCurrency = new String(currency);
		}
	}
	
	public String getTransactionCurrency() {
		return transactionCurrency;
	}

	public void setFundCurrency(String currency) {
		if (currency != null) {
            DebugLogging.log("setFundCurrency");
		    fundCurrency = new String(currency);
		}
	}
	
	public String getFundCurrency() {
		return fundCurrency;
	}

	public void setBankName(String name) {
		if (name != null) {
            DebugLogging.log("setBankName");
		    bankName = new String(name);
		}
	}
	
	public String getBankName() {
		return bankName;
	}
	
	public void setTransactionValue(float value) {
        DebugLogging.log("setTransactionValue");
		transactionValue = value;
	}
	
	public float getTransactionValue() {
		return transactionValue;
	}

	public void setFundValue(float value) {
        DebugLogging.log("setFundValue");
		fundValue = value;
	}
	
	public float getFundValue() {
		return fundValue;
	}
	
	public void fillIntent(Intent intent) {
		intent.putExtra(TransactionData.TRANSACTION_VALUE, getTransactionValue());
		intent.putExtra(TransactionData.FUND_VALUE, getFundValue());
		intent.putExtra(TransactionData.BANK_NAME, getBankName());
		intent.putExtra(TransactionData.CARD_NUMBER, getCardNumber());
		intent.putExtra(TransactionData.FUND_CURRENCY, getFundCurrency());
		intent.putExtra(TransactionData.TRANSACTION_CURRENCY, getTransactionCurrency());
		intent.putExtra(TransactionData.TRANSACTION_DATE, getTransactionDate());
		intent.putExtra(TransactionData.TRANSACTION_PLACE, getTransactionPlace());
		intent.putExtra(TransactionData.OPERATION_NAME, getOperation());
	}

    public String getOperation() {
        return operationName;
    }
    
    public void setOperation(String name) {
        if (name != null) {
            operationName = name;
        }
    }

}
