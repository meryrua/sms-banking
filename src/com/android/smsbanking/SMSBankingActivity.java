package com.android.smsbanking;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;


public class SMSBankingActivity extends Activity {
	
	Context context;
	
	private static final String BANK_ADDRESS = "5556";
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        context = getApplicationContext();
        
        Intent intent = new Intent(SMSReceiver.BANK_ADDRESS_ACTION); 
        intent.putExtra(SMSReceiver.TYPE, BANK_ADDRESS); 
        sendBroadcast(intent); 
    }
}