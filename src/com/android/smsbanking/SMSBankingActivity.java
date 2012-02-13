package com.android.smsbanking;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


public class SMSBankingActivity extends Activity{
	
	private Context context;
	private Button sendSMSButton;
	private Button viewHistoryButton;
	private Button checkSMSButton;
	
	private static final String BANK_ADDRESS = "5556";
	
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        context = getApplicationContext();
        
        sendSMSButton = (Button) findViewById(R.id.send_sms);
        sendSMSButton.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		Intent intent = new Intent(SMSReceiver.SEND_SMS_ACTION); 
        		sendBroadcast(intent);    	
        	}
        });
        
        viewHistoryButton = (Button) findViewById(R.id.view_transaction_history);
        if (viewHistoryButton != null) {
        viewHistoryButton.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		Intent startIntent = new Intent();
        		startIntent.setClass(context, DataFilterActivity.class);
        		startActivity(startIntent);
        	}
        });
        }
        
        checkSMSButton = (Button) findViewById(R.id.check_sms);
        checkSMSButton.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
            	Uri uriSMSURI = Uri.parse("content://sms/inbox");
                Cursor cur = getContentResolver().query(uriSMSURI, null, null, null,null);
                String sms = "";
                while (cur.moveToNext()) {
                    sms += "From :" + cur.getString(2) + " : " + cur.getString(11)+"\n";   
                    Log.d("NATALIA!!! ", sms);
                }        		
        	}
        });
                
        Intent intent = new Intent(SMSReceiver.BANK_ADDRESS_ACTION); 
        intent.putExtra(SMSReceiver.TYPE, BANK_ADDRESS); 
        sendBroadcast(intent); 
    }
    
    
}