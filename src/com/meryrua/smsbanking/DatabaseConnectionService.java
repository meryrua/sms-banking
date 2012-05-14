package com.meryrua.smsbanking;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.IBinder;
import android.os.Looper;

public class DatabaseConnectionService extends Service {
	private boolean handlerIsCreated = false;
	private MyDBAdapter myDBAdapter;
	private IBinder myBinder = new MyBinder();
	private DatabaseConnectionCallbackInterface callbackObject;
	private ServiceHandlerThread thread;
	
	private static final int GET_TRANSACTION_DATA = 1;
	private static final int GET_CARDS_DATA = 2;
	private static final int INSERT_DATA = 3;
	private static final int UPDATE_CARD_ALIAS = 4;
	private static final int DELETE_DATA = 5;
	private static final int LOAD_DATA_FROM_SMS = 6;
	private static final int DELETE_AND_LOAD_DATA_FROM_SMS = 7;
	private static final int OPEN_DATABASE = 8;
	private static final int GET_BALANCE = 9;
	private static final int DELETE_CARD = 10;
	
	public static final String INSERT_DATA_ACTION = "action.insert_data";
	
	private static final String CARD_DATA = "card_data";
	private static final String CARD_ALIAS_DATA = "card_alias_data";
	
    private static final String LOG_TAG = "DatabaseConnectionService";

	private final class ServiceHandler extends Handler {
				
        @Override
	    public void handleMessage(Message msg) {
            DebugLogging.log(getApplicationContext(), (LOG_TAG + " handleMessage " + msg.what));
    		boolean loadResult = false;
    		boolean deleteResult = false;
    		
    		if (!myDBAdapter.isDatabaseOpen()) {
    		    myDBAdapter.open();
    		}
    		
	    	switch(msg.what) {
	    	case GET_TRANSACTION_DATA: {
	    	    try {
	    	        Cursor transactionCursor = myDBAdapter.getTransactionWithFilter(msg.obj.toString());
	    	        callbackObject.showTransactionData(transactionCursor);
	    	    } catch (SQLiteException exc) {
                    callbackObject.sqlLiteExceptionIsCatched();
                    DebugLogging.log(getApplicationContext(), (LOG_TAG + exc.getMessage()));	    	        
	    	    }
	    		break;
	    	}
	    	case GET_BALANCE: {
	    	    try {
	    	        String balanceValue = myDBAdapter.getBalance(msg.obj.toString());
	    	        callbackObject.setBalance(balanceValue);
	    	    } catch (SQLiteException exc) {
                    callbackObject.sqlLiteExceptionIsCatched();
                    DebugLogging.log(getApplicationContext(), (LOG_TAG + exc.getMessage()));	    	        
	    	    }
	    		break;
	    	}
	    	case GET_CARDS_DATA: {
	    	    try {
	    	        Cursor cardsCursor = myDBAdapter.selectCardsNumber(msg.obj.toString());
	    	        callbackObject.showCardsData(cardsCursor, msg.obj.toString());
	    	    } catch (SQLiteException exc) {
                    callbackObject.sqlLiteExceptionIsCatched();
                    DebugLogging.log(getApplicationContext(), (LOG_TAG + exc.getMessage()));
	    	    }
	    		break;
	    	}
	    	case INSERT_DATA: {
	    		Bundle bundle = (Bundle) msg.obj;
	    		TransactionData transactionData = new TransactionData(bundle);
	    		try {
	    		    boolean insertResult = myDBAdapter.insertTransaction(transactionData);
	    		    if (insertResult) {
	    		        Intent updateIntent = new Intent(SMSBankingActivity.UPDATE_TRANSACTION_LIST_INTENT);
	    		        getApplicationContext().sendBroadcast(updateIntent);
	    		    }
	    		} catch (SQLiteException exc) {
	    		    //TODO if error I should inform 
                    callbackObject.sqlLiteExceptionIsCatched();
                    DebugLogging.log(getApplicationContext(), (LOG_TAG + exc.getMessage()));
	    		}
	    		break;
	    	}
	    	case UPDATE_CARD_ALIAS: {
	    		Bundle cardMap = (Bundle) msg.obj;
	    		try {
	    		    Boolean updateResult = myDBAdapter.updateCardAlias(cardMap.getString(CARD_ALIAS_DATA), cardMap.getString(CARD_DATA));
	    		    callbackObject.aliasUpdated(updateResult);
	    		} catch (SQLiteException exc) {
                    callbackObject.sqlLiteExceptionIsCatched();
                    DebugLogging.log(getApplicationContext(), (LOG_TAG + exc.getMessage()));
	    		}
	    		break;	 
	    	}
	    	case DELETE_DATA: {
	    		//for (int j = 0; j < 10000000; j++);
	    		try {
	    		    myDBAdapter.beginDatabaseTranzaction();
    				deleteResult = myDBAdapter.deleteData();
    				if (deleteResult) {
    				    myDBAdapter.setSuccesfullTranzaction();
    				}
                    myDBAdapter.endDatabaseTranzaction();
                    callbackObject.dataWasDeleted(deleteResult);
	    		} catch (SQLiteException exc) {
	    		    callbackObject.sqlLiteExceptionIsCatched();
	    	        DebugLogging.log(getApplicationContext(), (LOG_TAG + exc.getMessage()));
	    		}
	    		break;
	    	}
	    	case LOAD_DATA_FROM_SMS: {
				try	{
	                myDBAdapter.beginDatabaseTranzaction();
					//for (int j = 0; j < 10000000; j++);

					loadResult = loadDataFromSMS();
					if (loadResult) {
						myDBAdapter.setSuccesfullTranzaction();
					}
		            myDBAdapter.endDatabaseTranzaction();
		            callbackObject.dataWasLoaded(loadResult);
				} catch(SQLiteException exc) {
                    callbackObject.sqlLiteExceptionIsCatched();
                    DebugLogging.log(getApplicationContext(), (LOG_TAG + exc.getMessage()));
				}
	    		break;	 
	    	}
	    	case OPEN_DATABASE: {
	    	    try {
	    	        DebugLogging.log(getApplicationContext(), (LOG_TAG + " OPEN_DATABASE"));
	    	        myDBAdapter.open();
	    	    } catch (SQLiteException exc) {
	                callbackObject.sqlLiteExceptionIsCatched();
	                DebugLogging.log(getApplicationContext(), (LOG_TAG + exc.getMessage()));
	    	    }
	    		break;
	    	}
	    	case DELETE_AND_LOAD_DATA_FROM_SMS: {
				try	{
				    myDBAdapter.beginDatabaseTranzaction();					
				    //for (int j = 0; j < 10000000; j++);
					deleteResult = myDBAdapter.deleteData();
					
					if (deleteResult) {
						loadResult = loadDataFromSMS();
						if (loadResult){
				        	myDBAdapter.setSuccesfullTranzaction();
						}
					}
	                myDBAdapter.endDatabaseTranzaction();
	                callbackObject.dataWasLoaded(loadResult);
	            } catch (SQLiteException exc) {
                    callbackObject.sqlLiteExceptionIsCatched();
                    DebugLogging.log(getApplicationContext(), (LOG_TAG + exc.getMessage()));
                }
	    		break;
	    	}
	    	case DELETE_CARD: {
                try{
                    myDBAdapter.beginDatabaseTranzaction();  
                    if (myDBAdapter.deleteCardData(msg.obj.toString())) {
                        myDBAdapter.setSuccesfullTranzaction();                       
                    }
                    myDBAdapter.endDatabaseTranzaction();
                    callbackObject.cardDataWasDeleted();
                } catch (SQLiteException exc) {
                    callbackObject.sqlLiteExceptionIsCatched();
                    DebugLogging.log(getApplicationContext(), (LOG_TAG + exc.getMessage()));
                }
	    	    break;
	    	}
	    	default:
	    	    DebugLogging.log(getApplicationContext(), (LOG_TAG + " ServiceHandler handleMessage default tag"));
	    		break;
	    	}
	    }
	    
	    private boolean loadDataFromSMS() {
			Uri uriSms = Uri.parse("content://sms/inbox");
			boolean loadResult = false;
			String sort_by = SMSViewingAdapter.SMS_DATE_FIELD + " ASC";
			Context context = getApplicationContext();
			Cursor inboxSMSCursor = context.getContentResolver().query(
							uriSms, 
							new String[] { SMSViewingAdapter.SMS_ID_FIELD,
							SMSViewingAdapter.SMS_DATE_FIELD,
							SMSViewingAdapter.SMS_BODY_FIELD}, 
							null, null, sort_by);
			loadResult = true;
			if ((inboxSMSCursor != null) && (inboxSMSCursor.moveToFirst())) {
				SMSParcer smsParcer = new SMSParcer();
				//for (int j = 0; j < 10000000; j++);
				do {
					smsParcer.setParcedString(inboxSMSCursor.getString(
								inboxSMSCursor.getColumnIndex(SMSViewingAdapter.SMS_BODY_FIELD)));
					if (smsParcer.isMatch(myDBAdapter)) {
					    loadResult = myDBAdapter.insertTransaction(smsParcer.getTransactionData());
					}
				} while ((inboxSMSCursor.moveToNext()) && (loadResult));
			inboxSMSCursor.close();	    	
			}
			return loadResult;
	    }
	}
	
	public class MyBinder extends Binder {
		DatabaseConnectionService getService() {
		    return DatabaseConnectionService.this;
		}
	}
	
	class ServiceHandlerThread extends Thread {
		private ServiceHandler mServiceHandler;

		public void run() {
			Looper.prepare();
			mServiceHandler = new ServiceHandler();
			handlerIsCreated = true;
			
            Message msg = mServiceHandler.obtainMessage();
            msg.what = OPEN_DATABASE;
            mServiceHandler.sendMessage(msg);
			
			if (callbackObject != null) {
			    callbackObject.onReady();
			}
			Looper.loop();
		}
		
		public ServiceHandler getHandler(){
			return mServiceHandler;
		}
		
	}
	
	@Override
	public void onCreate() {
        DebugLogging.log(getApplicationContext(), (LOG_TAG + " onCreate"));
        myDBAdapter = new MyDBAdapter(getApplicationContext());
        
		thread = new ServiceHandlerThread();
	    thread.start();
	}
	
	@Override
	public void onDestroy() {
        DebugLogging.log(getApplicationContext(), (LOG_TAG + " onDestroy"));
		myDBAdapter.close();
		super.onDestroy();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	    DebugLogging.log(getApplicationContext(), (LOG_TAG + " onStartCommand handlerIsCreated " + handlerIsCreated + " " +
	            (intent == null)));
	    if (handlerIsCreated){
		    if ((intent != null) && (intent.getAction() != null) && (intent.getAction().equals(INSERT_DATA_ACTION))){
		        Message msg = thread.getHandler().obtainMessage();
		        msg.arg1 = startId;
		 	    
		    	msg.what = INSERT_DATA;
		    	msg.obj = intent.getExtras();
		    	thread.getHandler().sendMessage(msg);
		    }

	    }
	      
	    return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		//for (int i = 0; i < 1000000; i++);
        DebugLogging.log(getApplicationContext(), (LOG_TAG + " onBind"));
		return myBinder;
	}
	
	public void setCallbackItem(DatabaseConnectionCallbackInterface callback) {
		callbackObject = callback;
		if (handlerIsCreated) {
		    callbackObject.onReady();
		}
	}
	
	public void getTransactionData(String filter) {
	    Message msg = thread.getHandler().obtainMessage();
    	msg.what = GET_TRANSACTION_DATA;
    	msg.obj = filter;
    	thread.getHandler().sendMessage(msg);
	}
	
	public void getBalance(String cardNumber) {
	    Message msg = thread.getHandler().obtainMessage();
    	msg.what = GET_BALANCE;
    	msg.obj = cardNumber;
    	thread.getHandler().sendMessage(msg);		
	}
	
	public void getCardsData(String cardsNumber) {
	    Message msg = thread.getHandler().obtainMessage();
    	msg.what = GET_CARDS_DATA;
    	msg.obj = cardsNumber;
    	thread.getHandler().sendMessage(msg);
	}
	
	public void updateCardAlias(String cardsNumber, String alias) {
	    Bundle bundle = new Bundle();
	    bundle.putString(CARD_DATA, cardsNumber);
	    bundle.putString(CARD_ALIAS_DATA, alias);
	    Message msg = thread.getHandler().obtainMessage();
    	msg.what = UPDATE_CARD_ALIAS;
    	msg.obj = bundle;
    	thread.getHandler().sendMessage(msg);
	}
	
	public void deleteAllData(boolean loadFromSMS) {
	    Message msg = thread.getHandler().obtainMessage();
		if (loadFromSMS) {
		    msg.what = DELETE_AND_LOAD_DATA_FROM_SMS;
		    thread.getHandler().sendMessage(msg);			
		} else {
	    	msg.what = DELETE_DATA;
	    	thread.getHandler().sendMessage(msg);
		}
	}
	
	public void loadDataFromSMS() {
	    Message msg = thread.getHandler().obtainMessage();
		msg.what = LOAD_DATA_FROM_SMS;
		thread.getHandler().sendMessage(msg);			
	}
	
	public void deleteCardData(String cardNumber) {
        Message msg = thread.getHandler().obtainMessage();
        msg.what = DELETE_CARD;
        msg.obj = cardNumber;
        thread.getHandler().sendMessage(msg);	    
	}
}
