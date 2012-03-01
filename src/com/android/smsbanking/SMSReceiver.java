package com.android.smsbanking;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;


public class SMSReceiver extends BroadcastReceiver {
	
	public static final String BANK_ADDRESS_ACTION = "com.android.smsbanking.BANK_ADDRESS";
	public static final String SEND_SMS_ACTION = "com.android.smsbanking.SEND_SMS";
	public static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	public static final String TYPE = "address"; 
	
	private static final String BANK_ADDRESS = "5556";
	
	private static int NOTIFICATION_ID = 0;
	
	static String bankAddress = null;
	String currBankAddress = null;
	private Notification notification;
	
	private SMSParcer smsParcer;// = new SMSParcer(); //One parcer 
	private MyDBAdapter myDBAdapter;
	
	private TransactionData transactionData;
	
	private boolean allowSMSProcessing = false;
	
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
	        String messageForParcing = "";
	        if (bundle != null)
	        {
	            //---retrieve the SMS message received---
	            Object[] pdus = (Object[]) bundle.get("pdus");
	            msgs = new SmsMessage[pdus.length];  
	           
	                msgs1 = SmsMessage.createFromPdu((byte[])pdus[0]);
	                str1 += msgs1.getOriginatingAddress();
	                
	                //if (str1.contains(bankAddress))
	                {
	                	smsMessage += "SMS is from bank " + msgs1.getOriginatingAddress();
			            for (int i=0; i<msgs.length; i++){
				            msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
				            messageForParcing += msgs[i].getMessageBody().toString();
			        }
			            
			        smsParcer = new SMSParcer(messageForParcing);
			        boolean matchSMS = smsParcer.isMatch();
			        Toast.makeText(context, "SMS is " + ((matchSMS)?"":"not ") +
			        		"from bank.", Toast.LENGTH_LONG).show();
			        Log.d("NATALIA!!! ", "match " + matchSMS);
			        
			       if (matchSMS){
			        	

			        	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
			        	String smsProcessing = settings.getString(context.getResources().getString(R.string.sms_process), context.getResources().getString(R.string.application_only));
			        	if (smsProcessing.equals(context.getResources().getString(R.string.application_only)))
			        			abortBroadcast(); // I don't know if it's good decision	
			            	
				        transactionData = new TransactionData();
						smsParcer.setTranzactionData(transactionData);
								            
						myDBAdapter = new MyDBAdapter(context);
						myDBAdapter.open();
						myDBAdapter.insertTransaction(transactionData);
						myDBAdapter.close();
							
						setSMSNotification(context, "New sms", transactionData); 		
			        }
	                }
		        }		 
		 }
		 else if (intent.getAction().equals(SEND_SMS_ACTION)) {
		            
			 boolean matchSMS = smsParcer.isMatch();
				          			            
			 transactionData = new TransactionData();
			 smsParcer.setTranzactionData(transactionData);
				            
			 myDBAdapter = new MyDBAdapter(context);
			 myDBAdapter.open();
			 myDBAdapter.insertTransaction(transactionData);
			 myDBAdapter.close();
			 
			 setSMSNotification(context, "New sms", transactionData); 
		}
	 }
	
	 static Intent[] makeMessageIntentStack(Context context, CharSequence from,
		        CharSequence msg) {
		    Intent[] intents = new Intent[1];

		    // First: root activity of ApiDemos.
		    // This is a convenient way to make the proper Intent to launch and
		    // reset an application's task.
		    //intents[0] = Intent.makeRestartActivityTask(new ComponentName(context,
		    //        com.android.smsbanking.SMSBankingActivity.class));

		    // "App"
		    //intents[1] = new Intent(context, com.android.smsbanking.SMSBankingActivity.class);
		    //intents[1].putExtra("com.android.smsbanking.Path", "SMSBankingActivity");

		    return intents;
		} 
	 
	private void setSMSNotification(Context context,CharSequence notiDetail, TransactionData tranzactionData) {
		String ns = Context.NOTIFICATION_SERVICE;
	    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);
	            
	    String smsNoti = context.getResources().getString(R.string.notification_string) + " " +
	    				tranzactionData.getFundValue() + tranzactionData.getFundCurrency();
	    long when = System.currentTimeMillis();
	    int icon = R.drawable.icon;
	    notification = new Notification(icon, smsNoti, when);
	    notification.flags = notification.flags | Notification.FLAG_AUTO_CANCEL | Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND; //remove notification on user click
	            

	    //Intent notiIntent = new Intent(SMSBankingActivity.VIEW_TRANSACTION_LIST_INTENT, null, context, SMSBankingActivity.class);
	    Intent notiIntent = new Intent(context, SMSBankingActivity.class);
	    notiIntent.setAction(Intent.ACTION_MAIN);
	    notiIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    //Intent notiIntent = new Intent(context, DataFilterActivity.class);
	    //Intent notiIntent = new Intent(context, DataFilterActivity.class);
	    //Intent notiIntent = new Intent(SMSBankingActivity.VIEW_TRANSACTION_LIST_INTENT);
	    //notiIntent.setClass(context, SMSBankingActivity.class);
	    fillIntent(notiIntent, tranzactionData);
	    //notiIntent.setAction(SMSBankingActivity.VIEW_TRANSACTION_LIST_INTENT);
	    //notiIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
	    PendingIntent launchIntent = PendingIntent.getActivity(context, 0, notiIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	    notification.setLatestEventInfo(context, smsNoti.subSequence(0, (smsNoti.length() - 1)) , notiDetail, launchIntent);
	            
	    //NOTIFICATION_ID ++;
	    mNotificationManager.cancel(NOTIFICATION_ID);
	    mNotificationManager.notify(NOTIFICATION_ID, notification);
	}
	
	static public void fillIntent(Intent intent, TransactionData tranzactionData){
		intent.putExtra(TransactionData.TRANSACTION_VALUE, tranzactionData.getTransactionValue());
		intent.putExtra(TransactionData.FUND_VALUE, tranzactionData.getFundValue());
		intent.putExtra(TransactionData.BANK_NAME, tranzactionData.getBankName());
		intent.putExtra(TransactionData.CARD_NUMBER, tranzactionData.getCardNumber());
		intent.putExtra(TransactionData.FUND_CURRENCY, tranzactionData.getFundCurrency());
		intent.putExtra(TransactionData.TRANSACTION_CURRENCY, tranzactionData.getTransactionCurrency());
		intent.putExtra(TransactionData.TRANSACTION_DATE, tranzactionData.getTransactionDate());
		intent.putExtra(TransactionData.TRANSACTION_PLACE, tranzactionData.getTransactionPlace());
	}

}
