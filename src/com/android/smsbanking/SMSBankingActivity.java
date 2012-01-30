package com.android.smsbanking;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


public class SMSBankingActivity extends Activity  implements OnClickListener{
	
	Context context;
	Button sendSMSButton;
	
	private static final String BANK_ADDRESS = "5556";
	
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        sendSMSButton = (Button) findViewById(R.id.send_sms);
        sendSMSButton.setOnClickListener(this);
                
        Intent intent = new Intent(SMSReceiver.BANK_ADDRESS_ACTION); 
        intent.putExtra(SMSReceiver.TYPE, BANK_ADDRESS); 
        sendBroadcast(intent); 
    }
    
    public void onClick(View v){
        Intent intent = new Intent(SMSReceiver.SEND_SMS_ACTION); 
        
        sendBroadcast(intent);    	
    }
}