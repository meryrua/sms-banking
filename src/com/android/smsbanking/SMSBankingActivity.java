package com.android.smsbanking;

import android.app.Activity;
import android.os.Bundle;


public class SMSBankingActivity extends Activity {
	
	public String bankAddress = "5556";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
    }
}