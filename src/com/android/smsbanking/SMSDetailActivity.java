package com.android.smsbanking;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class SMSDetailActivity extends Activity implements OnClickListener{
	
	private TransactionData transactionData = new TransactionData();
	private Button close_button;
	private Context context;
	private Resources resources;
	
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms_detail);       
        Log.d("NATALIA!!!", "SMSDetail");

        context = getApplicationContext();
        resources = context.getResources();
        
        close_button = (Button) findViewById(R.id.close_button);
        close_button.setOnClickListener(this);

        Bundle extras = getIntent().getExtras();
        getBundleExtra(extras, transactionData);
        
        TextView cardNumberText = (TextView) findViewById(R.id.card_number);
        cardNumberText.setText(resources.getString(R.string.operation_card_number) + transactionData.getCardNumber());
        //cardNumberText.append(transactionData.getCardNumber());
        
        TextView dateText = (TextView) findViewById(R.id.date);
        dateText.setText(resources.getString(R.string.operation_date) + " " + transactionData.getTransactionDate());
        //dateText.append(transactionData.getTransactionDate());

        TextView amountText = (TextView) findViewById(R.id.amount);
        String tranzValue = new String(Float.toString(transactionData.getTransactionValue()).replace(".", ","));
        tranzValue += transactionData.getTransactionCurrency();
        amountText.setText(resources.getString(R.string.operation_amount) + " " + tranzValue);
        //amountText.append(tranzValue);
        
        TextView placeText = (TextView) findViewById(R.id.place);
        String placeOrOperation = transactionData.getTransactionPlace();
        if (placeOrOperation.equals(TransactionData.INCOMING_BANK_OPERATION)){
        	placeText.setText(resources.getString(R.string.operation_name) + " " + resources.getString(R.string.operation_incoming));
        }else if (placeOrOperation.equals(TransactionData.OUTGOING_BANK_OPERATION)){
        	placeText.setText(resources.getString(R.string.operation_name) + " " + resources.getString(R.string.operation_outgoing));
        }else {
        	placeText.setText(resources.getString(R.string.operation_place) + " " + transactionData.getTransactionPlace());
        	//placeText.append(transactionData.getTransactionPlace());
        }

        TextView balanceText = (TextView) findViewById(R.id.balance);
        String balanceValue = new String(Float.toString(transactionData.getFundValue()).replace(".", ","));
        balanceValue += transactionData.getFundCurrency();
        balanceText.setText(resources.getString(R.string.operation_balance) + " " + balanceValue);
        //balanceText.append(balanceValue);
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
