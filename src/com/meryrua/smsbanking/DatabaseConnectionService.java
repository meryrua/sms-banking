package com.meryrua.smsbanking;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

public class DatabaseConnectionService extends Service{
	private boolean handlerIsCreated = false;
	private MyDBAdapter myDBAdapter;
	private IBinder myBinder = new MyBinder();
	private DatabaseConnectionCallbackInterface callbackObject;
	private ServiceHandlerThread thread;
	
	protected static final int GET_TRANSACTION_DATA = 1;
	protected static final int GET_CARDS_DATA = 2;
	protected static final int INSERT_DATA = 3;
	protected static final int UPDATE_CARD_ALIAS = 4;
	protected static final int DELETE_DATA = 5;
	protected static final int LOAD_DATA_FROM_SMS = 6;
	protected static final int DELETE_AND_LOAD_DATA_FROM_SMS = 7;
	protected static final int OPEN_DATABASE = 8;
	protected static final int GET_BALANCE = 9;
	protected static final int DELETE_CARD = 10;
	
	public static final String INSERT_DATA_ACTION = "action.insert_data";
	
	protected static final String CARD_DATA = "card_data";
	protected static final String CARD_ALIAS_DATA = "card_alias_data";
	
	private static final String LOG_TAG = "com.meryrua.smsbanking:DatabaseConnectionService";

	private final class ServiceHandler extends Handler {
				
        @Override
	    public void handleMessage(Message msg) {
	    	Log.d(LOG_TAG, "handleMessage thread " + (Process.myTid()));
    		boolean loadResult = false, deleteResult = false;
    		
    		if (!myDBAdapter.isDatabaseOpen()) myDBAdapter.open();
    		
	    	switch(msg.what){
	    	case GET_TRANSACTION_DATA:
	    		Cursor transactionCursor = myDBAdapter.getTransactionWithFilter(msg.obj.toString());
	    		Log.d(LOG_TAG, "Handler service call callback");
	    		callbackObject.showTransactionData(transactionCursor);
	    		break;
	    	case GET_BALANCE:
	    		Log.d(LOG_TAG, "Handler service balance");
	    		String balanceValue = myDBAdapter.getBalance(msg.obj.toString());
	    		callbackObject.setBalance(balanceValue);
	    		break;
	    	case GET_CARDS_DATA:
	    		Cursor cardsCursor = myDBAdapter.selectCardsNumber(msg.obj.toString());
	    		callbackObject.showCardsData(cardsCursor, msg.obj.toString());
	    		break;
	    	case INSERT_DATA:
	    		Bundle bundle = (Bundle) msg.obj;
	    		TransactionData transactionData = new TransactionData(bundle);
	    		boolean insertResult = myDBAdapter.insertTransaction(transactionData);
	    		Log.d(LOG_TAG, "service handler insert data");
				if (insertResult){
					Intent updateIntent = new Intent(SMSBankingActivity.UPDATE_TRANSACTION_LIST_INTENT);
					getApplicationContext().sendBroadcast(updateIntent);
				}
	    		break;
	    	case UPDATE_CARD_ALIAS:
	    		Bundle cardMap = (Bundle) msg.obj;
	    		Boolean updateResult = myDBAdapter.updateCardAlias(cardMap.getString(CARD_ALIAS_DATA), cardMap.getString(CARD_DATA));
	    		callbackObject.aliasUpdated(updateResult);
	    		break;	 
	    	case DELETE_DATA:
	    		myDBAdapter.beginDatabaseTranzaction();
				try	{
					//for (int j = 0; j < 10000000; j++);
					deleteResult = myDBAdapter.deleteData();
					if (deleteResult) myDBAdapter.setSuccesfullTranzaction();
				}catch(Exception ex){
				    Log.d(LOG_TAG, "Catch error");
				}
				myDBAdapter.endDatabaseTranzaction();
				callbackObject.dataWasDeleted(deleteResult);
	    		break;	 
	    	case LOAD_DATA_FROM_SMS:
				myDBAdapter.beginDatabaseTranzaction();
				try	{
					//for (int j = 0; j < 10000000; j++);

					loadResult = loadDataFromSMS();
					if (loadResult){
						myDBAdapter.setSuccesfullTranzaction();
					}
				}catch(Exception ex){
                    Log.d(LOG_TAG, "Error in deleting data");
				}
				myDBAdapter.endDatabaseTranzaction();
				callbackObject.dataWasLoaded(loadResult);
	    		break;	 
	    	case OPEN_DATABASE:
	    		myDBAdapter.open();
	    		break;
	    	case DELETE_AND_LOAD_DATA_FROM_SMS:
				myDBAdapter.beginDatabaseTranzaction();
				try	{
					//for (int j = 0; j < 10000000; j++);
					deleteResult = myDBAdapter.deleteData();
					
					if (deleteResult){
						loadResult = loadDataFromSMS();
						if (loadResult){
				        	myDBAdapter.setSuccesfullTranzaction();
						}
					}
				}catch(Exception ex){
	                  Log.d(LOG_TAG, "Error in deleting and loading data from SMS");
				}
				myDBAdapter.endDatabaseTranzaction();
				callbackObject.dataWasLoaded(loadResult);
	    		break;
	    	case DELETE_CARD:
                myDBAdapter.beginDatabaseTranzaction();	 
                try{
                    if (myDBAdapter.deleteCardData(msg.obj.toString())){
                        myDBAdapter.setSuccesfullTranzaction();                       
                    }
                }catch(Exception exc){
                    Log.d(LOG_TAG, "Error in deleting card data");
                }
                myDBAdapter.endDatabaseTranzaction();
                callbackObject.cardDataWasDeleted();
	    	    break;
	    	default:
	    		break;
	    	}
	    }
	    
	    private boolean loadDataFromSMS(){
			Uri uriSms = Uri.parse("content://sms/inbox");
			boolean loadResult = false;
			String sort_by = new String(SMSViewingAdapter.SMS_DATE_FIELD + " ASC");
			Context context = getApplicationContext();
			try{
				Cursor inboxSMSCursor = context.getContentResolver().query(
							uriSms, 
							new String[] { SMSViewingAdapter.SMS_ID_FIELD,
							SMSViewingAdapter.SMS_DATE_FIELD,
							SMSViewingAdapter.SMS_BODY_FIELD}, 
							null, null, sort_by);
				loadResult = true;
				if ((inboxSMSCursor != null) && (inboxSMSCursor.moveToFirst())){
				    SMSParcer smsParcer = new SMSParcer();
					//for (int j = 0; j < 10000000; j++);
					do {
						smsParcer.setParcedString(inboxSMSCursor.getString(
									inboxSMSCursor.getColumnIndex(SMSViewingAdapter.SMS_BODY_FIELD)));
					    if (smsParcer.isMatch(myDBAdapter)){
					    	loadResult = myDBAdapter.insertTransaction(smsParcer.getTransactionData());
					    }
					}while ((inboxSMSCursor.moveToNext()) && (loadResult));
				inboxSMSCursor.close();	    	
				}
			}catch(Exception exc){}
			return loadResult;
	    }
	}
	
	public class MyBinder extends Binder {
		DatabaseConnectionService getService() {
		return DatabaseConnectionService.this;
		}
	}
	
	class ServiceHandlerThread extends Thread{
		private ServiceHandler mServiceHandler;

		public void run(){
			Log.d(LOG_TAG, "run thread");
			Looper.prepare();
			mServiceHandler = new ServiceHandler();
			handlerIsCreated = true;
			
			if (callbackObject != null) callbackObject.onReady();
			Looper.loop();
		}
		
		public ServiceHandler getHandler(){
			return mServiceHandler;
		}
		
	}
	
	@Override
	public void onCreate(){
		Log.d(LOG_TAG, "onCreate thread " + android.os.Process.getThreadPriority(Process.myTid()));
		thread = new ServiceHandlerThread();
	    thread.start();
    
	    myDBAdapter = new MyDBAdapter(getApplicationContext());
	}
	
	@Override
	public void onDestroy(){
		myDBAdapter.close();
		super.onDestroy();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
	    Log.d(LOG_TAG, "onStartCommand thread " + android.os.Process.getThreadPriority(Process.myTid()));
	    
	    if (handlerIsCreated){
		    Message msg = thread.getHandler().obtainMessage();
		    msg.arg1 = startId;
		    if (intent.getAction().equals(INSERT_DATA_ACTION)){
		    	msg.what = INSERT_DATA;
		    	msg.obj = intent.getExtras();
		    }
		    thread.getHandler().sendMessage(msg);
	    }
	      
	    return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Log.d(LOG_TAG, "onBind  thread " + android.os.Process.getThreadPriority(Process.myTid()));
		//for (int i = 0; i < 1000000; i++);
		return myBinder;
	}
	
	public void setCallbackItem(DatabaseConnectionCallbackInterface callback){
		Log.d(LOG_TAG, "Set Callback  thread " + android.os.Process.getThreadPriority(Process.myTid()));
		callbackObject = callback;
		if (handlerIsCreated) callbackObject.onReady();
	}
	
	public void getTransactionData(String filter){
		Log.d(LOG_TAG, "getTransactionData  thread " + android.os.Process.getThreadPriority(Process.myTid()));
	    Message msg = thread.getHandler().obtainMessage();
    	msg.what = GET_TRANSACTION_DATA;
    	msg.obj = filter;
    	thread.getHandler().sendMessage(msg);
	}
	
	public void getBalance(String cardNumber){
	    Message msg = thread.getHandler().obtainMessage();
    	msg.what = GET_BALANCE;
    	msg.obj = cardNumber;
    	thread.getHandler().sendMessage(msg);		
	}
	
	public void getCardsData(String cardsNumber){
	    Message msg = thread.getHandler().obtainMessage();
    	msg.what = GET_CARDS_DATA;
    	msg.obj = cardsNumber;
    	thread.getHandler().sendMessage(msg);
	}
	
	public void updateCardAlias(String cardsNumber, String alias){
	    Bundle bundle = new Bundle();
	    bundle.putString(CARD_DATA, cardsNumber);
	    bundle.putString(CARD_ALIAS_DATA, alias);
	    Message msg = thread.getHandler().obtainMessage();
    	msg.what = UPDATE_CARD_ALIAS;
    	msg.obj = bundle;
    	thread.getHandler().sendMessage(msg);
	}
	
	public void deleteAllData(boolean loadFromSMS){
	    Message msg = thread.getHandler().obtainMessage();
		if (loadFromSMS){
		    msg.what = DELETE_AND_LOAD_DATA_FROM_SMS;
		    thread.getHandler().sendMessage(msg);			
		}else{
	    	msg.what = DELETE_DATA;
	    	thread.getHandler().sendMessage(msg);
		}
	}
	
	public void loadDataFromSMS(){
	    Message msg = thread.getHandler().obtainMessage();
		msg.what = LOAD_DATA_FROM_SMS;
		thread.getHandler().sendMessage(msg);			
	}
	
	public void deleteCardData(String cardNumber){
        Message msg = thread.getHandler().obtainMessage();
        msg.what = DELETE_CARD;
        msg.obj = cardNumber;
        thread.getHandler().sendMessage(msg);	    
	}
}
