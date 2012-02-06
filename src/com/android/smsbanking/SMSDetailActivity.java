package com.android.smsbanking;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class SMSDetailActivity extends Activity implements OnClickListener{
	
	private static TransactionData transactionData = new TransactionData();
	Button close_button;
	
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms_detail);       
        Log.d("NATALIA!!!", "SMSDetail");
        
        close_button = (Button) findViewById(R.id.close_button);
        close_button.setOnClickListener(this);

        Bundle extras = getIntent().getExtras();
        getBundleExtra(extras, transactionData);
        
        TextView cardNumberText = (TextView) findViewById(R.id.card_number);
        cardNumberText.append(transactionData.getCardNumber());
        
        TextView dateText = (TextView) findViewById(R.id.date);
        dateText.append(transactionData.getTransactionDate());

        TextView amountText = (TextView) findViewById(R.id.amount);
        String tranzValue = new String(Float.toString(transactionData.getTransactionValue()).replace(".", ","));
        tranzValue += transactionData.getTransactionCurrency();
        amountText.append(tranzValue);
        
        TextView placeText = (TextView) findViewById(R.id.place);
        placeText.append(transactionData.getTransactionPlace());

        TextView balanceText = (TextView) findViewById(R.id.balance);
        String balanceValue = new String(Float.toString(transactionData.getFundValue()).replace(".", ","));
        balanceValue += transactionData.getFundCurrency();
        balanceText.append(balanceValue);
	}

    private void getBundleExtra(Bundle extras, TransactionData tranzactionData){
    	tranzactionData.setTransactionValue(extras.getFloat(TransactionData.TRANSACTION_VALUE, 0));
       	tranzactionData.setFundValue(extras.getFloat(TransactionData.FUND_VALUE, 0));
       	tranzactionData.setBankName(extras.getString(TransactionData.BANK_NAME));
       	tranzactionData.setCardNumber(extras.getString(TransactionData.CARD_NUMBER));
       	tranzactionData.setFundCurrency(extras.getString(TransactionData.FUND_CURRENCY));
       	tranzactionData.setTransactionCurrency(extras.getString(TransactionData.TRANSACTION_CURRENCY));
       	tranzactionData.setTransactionDate(extras.getString(TransactionData.TRANSACTION_DATE));
       	tranzactionData.setTransactionPlace(extras.getString(TransactionData.TRANSACTION_PLACE));
    }
    
    public void onClick(View v){
    	finish();
    }
}
