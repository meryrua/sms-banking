package com.android.smsbanking;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
	
	private static int NOTIFICATION_ID = 0;
	
	static String bankAddress = null;
	String currBankAddress = null;
	private Notification notification;
	
	 @Override
	  public void onReceive(Context context, Intent intent) {
		 	
		 //Log.d("NATALIA!!!", bankAddress);
		 if (intent.getAction().equals(BANK_ADDRESS_ACTION))
		 {
			 bankAddress = intent.getStringExtra(TYPE); 
			 //Log.d("NATALIA!!!",  bankAddress);
		 }
		 else if (intent.getAction().equals(SMS_RECEIVED))
		 {
			//Log.d("NATALIA!!!",  bankAddress);
		 	Bundle bundle = intent.getExtras();        
	        SmsMessage[] msgs = null;
			SmsMessage msgs1 = null;
	        String str1 = "";            
	        String smsMessage = "";            
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
			                smsMessage += "SMS is from bank " + msgs[i].getOriginatingAddress();                     
			                smsMessage += " :";
			                smsMessage += msgs[i].getMessageBody().toString();
			                smsMessage += "\n";        
			            }
			            setSMSNotification(context, smsMessage.subSequence(0, (smsMessage.length() - 1)));  
	                }
			        //else
			        	//str += "SMS is not from bank";
		        }		 
		 //Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
		 }
	 }
		 
	private void setSMSNotification(Context context, CharSequence notiDetail) {
		String ns = Context.NOTIFICATION_SERVICE;
	    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);
	            
	    String smsNoti = "New SMS from bank";
	    long when = System.currentTimeMillis();
	    int icon = R.drawable.icon;
	    notification = new Notification(icon, smsNoti, when);
	    notification.flags = notification.flags | Notification.FLAG_AUTO_CANCEL; //remove notification on user click
	            
	    Intent notiIntent = new Intent(context, SMSBankingActivity.class);
	    PendingIntent launchIntent = PendingIntent.getActivity(context, 0, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	    notification.setLatestEventInfo(context, smsNoti.subSequence(0, (smsNoti.length() - 1)) , notiDetail, launchIntent);
	            
	    NOTIFICATION_ID ++;
	    mNotificationManager.notify(NOTIFICATION_ID, notification);
			 
	}
}
