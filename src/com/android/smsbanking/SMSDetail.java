package com.android.smsbanking;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class SMSDetail extends Activity{
	Context context;
	Resources resources;
	TransactionData transactionData;
	@Override
    public void onCreate(Bundle savedInstanceState) {
	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sms_detail);   
        context = getApplicationContext();
        resources = context.getResources();
        
        Bundle extras = getIntent().getExtras();
        TransactionData transactionData = getBundleExtra(extras);
        
		//LayoutInflater inflaterSMSDetail = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
		//View layoutSMSDetail = inflaterSMSDetail.inflate(R.layout.sms_detail, (ViewGroup) findViewById(R.id.sms_detail_layout));
        View layoutSMSDetail = (View) findViewById(R.id.sms_detail_layout);
		layoutSMSDetail.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
        	
        });
        
        TextView cardNumberText = (TextView) findViewById(R.id.card_number);
        cardNumberText.setText(resources.getString(R.string.operation_card_number) + transactionData.getCardNumber());
        
        TextView dateText = (TextView) findViewById(R.id.date);
        dateText.setText(resources.getString(R.string.operation_date) + " " + transactionData.getTransactionDate());

        TextView amountText = (TextView) findViewById(R.id.amount);
        String tranzValue = new String(Float.toString(transactionData.getTransactionValue()).replace(".", ","));
        tranzValue += transactionData.getTransactionCurrency();
        amountText.setText(resources.getString(R.string.operation_amount) + " " + tranzValue);
        
        TextView placeText = (TextView) findViewById(R.id.place);
        String placeOrOperation = transactionData.getTransactionPlace();
        if (placeOrOperation.equals(TransactionData.INCOMING_BANK_OPERATION)){
        	placeText.setText(resources.getString(R.string.operation_name) + " " + resources.getString(R.string.operation_incoming_name));
        }else if (placeOrOperation.equals(TransactionData.OUTGOING_BANK_OPERATION)){
        	placeText.setText(resources.getString(R.string.operation_name) + " " + resources.getString(R.string.operation_outgoing_name));
        }else {
        	placeText.setText(resources.getString(R.string.operation_place) + " " + transactionData.getTransactionPlace());
        }
        int height = placeText.getMeasuredHeight();
        Log.d("NATALIA!!! ", "height " + height);

        TextView balanceText = (TextView) findViewById(R.id.balance);
        String balanceValue = new String(Float.toString(transactionData.getFundValue()).replace(".", ","));
        balanceValue += transactionData.getFundCurrency();
        Log.d("NATALIA!!!", "balance " + balanceValue);
        balanceText.setText(resources.getString(R.string.operation_balance) + " " + balanceValue);
        //balanceText.append(balanceValue);
	}

    private TransactionData getBundleExtra(Bundle extras){
    	TransactionData transactionData = new TransactionData();
    	transactionData.setTransactionValue(extras.getFloat(TransactionData.TRANSACTION_VALUE, 0));
    	transactionData.setFundValue(extras.getFloat(TransactionData.FUND_VALUE, 0));
    	//transactionData.setBankName(extras.getString(TransactionData.BANK_NAME));
    	transactionData.setCardNumber(extras.getString(TransactionData.CARD_NUMBER));
    	transactionData.setFundCurrency(extras.getString(TransactionData.FUND_CURRENCY));
    	transactionData.setTransactionCurrency(extras.getString(TransactionData.TRANSACTION_CURRENCY));
    	transactionData.setTransactionDate(extras.getString(TransactionData.TRANSACTION_DATE));
    	transactionData.setTransactionPlace(extras.getString(TransactionData.TRANSACTION_PLACE));
       	return transactionData;
    }
    

}
