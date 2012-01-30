package com.android.smsbanking;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class SMSDetailActivity extends Activity{
	
	private static TranzactionData tranzactionData = new TranzactionData();
	
	//private static String textForView = "%s po karte %s byla provedena tranzakcija na summu %s%s. Mesto provedenija operacii %s. Ostatok na karte %s.";
	private static String textForView = "%s po karte %s byla provedena tranzakcija";
	
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms_detail);       
        Log.d("NATALIA!!!", "SMSDetail");

        Bundle extras = getIntent().getExtras();
        getBundleExtra(extras, tranzactionData);
        
        TextView smsDetailText = (TextView) findViewById(R.id.sms_detail_text);
        Log.d("NATALIA!!!", "SMSDetail %lu " + smsDetailText);
        String strForView = textForView + tranzactionData.getCardNumber() + tranzactionData.getTranzactionDate();
        smsDetailText.setText(strForView);

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
}
