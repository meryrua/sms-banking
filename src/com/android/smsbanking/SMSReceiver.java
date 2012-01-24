package com.android.smsbanking;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;


public class SMSReceiver extends BroadcastReceiver {
	
	public static final String BANK_ADDRESS_ACTION = "com.android.smsbanking.BANK_ADDRESS";
	public static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	public static final String TYPE = "address"; 
	private static final String BANK_ADDRESS = "5556";

	static String bankAddress = null;
	String currBankAddress = null;
	 @Override
	  public void onReceive(Context context, Intent intent) {
		 	
		 //Log.d("NATALIA!!!", bankAddress);
		 if (BANK_ADDRESS_ACTION.equals(intent.getAction()))
		 {
			 bankAddress = intent.getStringExtra(TYPE); 
			 //Log.d("NATALIA!!!",  bankAddress);
		 }
		 else if (SMS_RECEIVED.equals(intent.getAction()))
		 {
			//Log.d("NATALIA!!!",  bankAddress);
		 	Bundle bundle = intent.getExtras();        
	        SmsMessage[] msgs = null;
			SmsMessage msgs1 = null;
	        String str1 = "";            
	        String str = "";            
	        if (bundle != null)
	        {
	            //---retrieve the SMS message received---
	            Object[] pdus = (Object[]) bundle.get("pdus");
	            msgs = new SmsMessage[pdus.length];  
	           
	                msgs1 = SmsMessage.createFromPdu((byte[])pdus[0]);
	                str1 += msgs1.getOriginatingAddress();
	                if (str1.contains(bankAddress))
	                {
			            for (int i=0; i<msgs.length; i++){
				            msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
			                str += "SMS is from bank " + msgs[i].getOriginatingAddress();                     
			                str += " :";
			                str += msgs[i].getMessageBody().toString();
			                str += "\n";        
			            }
	                }
			        else
			        	str += "SMS is not from bank";
		        }		 
		 Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
		 }
	 }

}
