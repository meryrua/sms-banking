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
	
	private static TranzactionData tranzactionData = new TranzactionData();
	Button close_button;
	
	//private static String textForView = "%s po karte %s byla provedena tranzakcija na summu %s%s. Mesto provedenija operacii %s. Ostatok na karte %s.";
	private static String textForView = "%s po karte %s byla provedena tranzakcija";
	
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms_detail);       
        Log.d("NATALIA!!!", "SMSDetail");
        
        close_button = (Button) findViewById(R.id.close_button);
        close_button.setOnClickListener(this);

        Bundle extras = getIntent().getExtras();
        getBundleExtra(extras, tranzactionData);
        
        TextView cardNumberText = (TextView) findViewById(R.id.card_number);
        cardNumberText.append(tranzactionData.getCardNumber());
        
        TextView dateText = (TextView) findViewById(R.id.date);
        dateText.append(tranzactionData.getTranzactionDate());

        TextView amountText = (TextView) findViewById(R.id.amount);
        String tranzValue = new String(Float.toString(tranzactionData.getTranzactionValue()).replace(".", ","));
        tranzValue += tranzactionData.getTranzactionCurrency();
        amountText.append(tranzValue);
        
        TextView placeText = (TextView) findViewById(R.id.place);
        placeText.append(tranzactionData.getTranzactionPlace());

        TextView balanceText = (TextView) findViewById(R.id.balance);
        String balanceValue = new String(Float.toString(tranzactionData.getFundValue()).replace(".", ","));
        balanceValue += tranzactionData.getFundCurrency();
        balanceText.append(balanceValue);
	}

    private void getBundleExtra(Bundle extras, TranzactionData tranzactionData){
    	tranzactionData.setTranzactionValue(extras.getFloat(TranzactionData.TRANZACTION_VALUE, 0));
       	tranzactionData.setFundValue(extras.getFloat(TranzactionData.FUND_VALUE, 0));
       	tranzactionData.setBankName(extras.getString(TranzactionData.BANK_NAME));
       	tranzactionData.setCardNumber(extras.getString(TranzactionData.CARD_NUMBER));
       	tranzactionData.setFundCurrency(extras.getString(TranzactionData.FUND_CURRENCY));
       	tranzactionData.setTranzactionCurrency(extras.getString(TranzactionData.TRANZACTION_CURRENCY));
       	tranzactionData.setTranzactionDate(extras.getString(TranzactionData.TRANZACTION_DATE));
       	tranzactionData.setTranzactionPlace(extras.getString(TranzactionData.TRANZACTION_PLACE));
    }
    
    public void onClick(View v){
    	finish();
    }
}
