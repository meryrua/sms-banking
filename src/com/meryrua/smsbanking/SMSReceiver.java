package com.meryrua.smsbanking;

import com.meryrua.smsbanking.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.widget.Toast;


public class SMSReceiver extends BroadcastReceiver {
	public static final String BANK_ADDRESS_ACTION = "com.meryrua.smsbanking.BANK_ADDRESS";
	public static final String SEND_SMS_ACTION = "com.meryrua.smsbanking.SEND_SMS";
	public static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	public static final String TYPE = "address"; 
	
	private static final String LOG_TAG = "SMSReceiver";
	
	private static int NOTIFICATION_ID = 0;
	
	private Notification notification;
	
	private SMSParcer smsParcer;
	
	private TransactionData transactionData;
	
	Context myContext;
	
	@Override
	  public void onReceive(Context context, Intent intent) {
        DebugLogging.log(context, (LOG_TAG + " onReceive  "));
        
		 myContext = context;
		 if (intent.getAction().equals(SMS_RECEIVED)) {
		 	Bundle bundle = intent.getExtras();        
	        SmsMessage[] msgs = null;
			SmsMessage msgs1 = null;
	        String str1 = "";            
	        String smsMessage = "";    
	        String messageForParcing = "";
	        if (bundle != null) {
	            //---retrieve the SMS message received---
	            Object[] pdus = (Object[]) bundle.get("pdus");
	            msgs = new SmsMessage[pdus.length];  
	           
	                msgs1 = SmsMessage.createFromPdu((byte[])pdus[0]);
	                str1 += msgs1.getOriginatingAddress();
	                
	                smsMessage += "SMS is from bank " + msgs1.getOriginatingAddress();
			        for (int i=0; i<msgs.length; i++) {
				        msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
				        messageForParcing += msgs[i].getMessageBody().toString();
			        }
			         
			        smsParcer = new SMSParcer(messageForParcing, myContext);
					boolean matchSMS = smsParcer.isMatch();
			        Toast.makeText(context, "SMS is " + ((matchSMS)?"":"not ") +
			        		"from bank.", Toast.LENGTH_LONG).show();
			        
			       if (matchSMS) {
			        	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
			        	String smsProcessing = settings.getString(context.getResources().getString(R.string.sms_processing), context.getResources().getString(R.string.normal_processing));
			        	if (smsProcessing.equals(context.getResources().getString(R.string.application_only))) {
			        			abortBroadcast();
			        	}
			        	
						transactionData = smsParcer.getTransactionData();
						Intent startServiceIntent = new Intent();
						startServiceIntent.setAction(DatabaseConnectionService.INSERT_DATA_ACTION);
						transactionData.fillIntent(startServiceIntent);
						startServiceIntent.setClass(context, DatabaseConnectionService.class);
						context.startService(startServiceIntent);
				        DebugLogging.log(context, (LOG_TAG + " startService  "));	
				        
						setSMSNotification(myContext, "New sms", transactionData); 		
			        }
		        }		 
		 }
	 }
	
	private void setSMSNotification(Context context,CharSequence notiDetail, TransactionData tranzactionData) {
		String ns = Context.NOTIFICATION_SERVICE;
	    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);
	            
	    String smsNoti = context.getResources().getString(R.string.notification_string) + " " +
	    				tranzactionData.getFundValue() + tranzactionData.getFundCurrency();
	    long when = System.currentTimeMillis();
	    int icon = R.drawable.icon;
	    notification = new Notification(icon, smsNoti, when);
	    notification.flags = notification.flags | Notification.FLAG_AUTO_CANCEL;// | Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND; //remove notification on user click

	    Intent notiIntent = new Intent(context, SMSBankingActivity.class);
	    notiIntent.setAction(SMSBankingActivity.VIEW_TRANSACTION_DETAIL_INTENT);
	    notiIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

	    tranzactionData.fillIntent(notiIntent);
	    PendingIntent launchIntent = PendingIntent.getActivity(context, 0, notiIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	    notification.setLatestEventInfo(context, smsNoti.subSequence(0, (smsNoti.length() - 1)) , notiDetail, launchIntent);
	            
	    mNotificationManager.cancel(NOTIFICATION_ID);
	    mNotificationManager.notify(NOTIFICATION_ID, notification);
	}
}
