package com.android.smsbanking;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SMSReceiver extends BroadcastReceiver {
	
	String bankAddress = "5556";
	 @Override
	  public void onReceive(Context context, Intent intent) {
		 	
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
			                str += "SMS from " + msgs[i].getOriginatingAddress();                     
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
