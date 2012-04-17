package com.meryrua.smsbanking;

import java.util.HashMap;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class DatabaseConnectionService extends Service{
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;
	private MyDBAdapter myDBAdapter;
	private IBinder myBinder = new MyBinder();
	private DatabaseConnectionCallbackInterface callbackObject;
	
	protected static final int GET_TRANSACTION_DATA = 1;
	protected static final int GET_CARDS_DATA = 2;
	protected static final int INSERT_TRANSACTION_DATA = 3;
	protected static final int INSERT_CARD_DATA = 4;
	protected static final int INSERT_DATA = 5;
	protected static final int UPDATE_CARD_ALIAS = 6;
	protected static final int DELETE_DATA = 7;
	protected static final int LOAD_DATA_FROM_SMS = 8;
	protected static final int DELETE_AND_LOAD_DATA_FROM_SMS = 9;
	protected static final int OPEN_DATABASE = 10;
	protected static final int GET_BALANCE = 11;

	private final class ServiceHandler extends Handler {
				
	    public ServiceHandler(Looper looper) {
	        super(looper);
	    }
	    
	    @Override
	    public void handleMessage(Message msg) {
    		boolean loadResult = false, deleteResult = false;
	    	switch(msg.what){
	    	case GET_TRANSACTION_DATA:
	    		Cursor transactionCursor = myDBAdapter.getTransactionWithFilter(msg.obj.toString());
	    		Log.d("NATALIA!!!", "Handler service call callback");
	    		callbackObject.showTransactionData(transactionCursor);
	    		break;
	    	case GET_BALANCE:
	    		Log.d("NATALIA!!!", "Handler service balance");
	    		String balanceValue = myDBAdapter.getBalance(msg.obj.toString());
	    		callbackObject.setBalance(balanceValue);
	    		break;
	    	case GET_CARDS_DATA:
	    		Cursor cardsCursor = myDBAdapter.selectCardsNumber(msg.obj.toString());
	    		callbackObject.showCardsData(cardsCursor, msg.obj.toString());
	    		break;
	    	case INSERT_TRANSACTION_DATA:
	    		break;
	    	case INSERT_CARD_DATA:
	    		break;
	    	case INSERT_DATA:
	    		Bundle bundle = (Bundle) msg.obj;
	    		TransactionData transactionData = new TransactionData(bundle);
	    		boolean insertResult = myDBAdapter.insertTransaction(transactionData);
	    		Log.d("NATALIA!!! ", "service handler insert data");
				if (insertResult){
					Intent updateIntent = new Intent(SMSBankingActivity.UPDATE_TRANSACTION_LIST_INTENT);
					getApplicationContext().sendBroadcast(updateIntent);
				}
	    		break;
	    	case UPDATE_CARD_ALIAS:
	    		HashMap<String, String> cardMap = (HashMap<String, String>)msg.obj;
	    		Boolean updateResult = myDBAdapter.updateCardAlias(cardMap.get(SMSBankingGlobalClass.CARD_ALIAS_DATA), cardMap.get(SMSBankingGlobalClass.CARD_DATA));
	    		callbackObject.aliasUpdated(updateResult);
	    		break;	 
	    	case DELETE_DATA:
	    		myDBAdapter.beginDatabaseTranzaction();
				try	{
					//for (int j = 0; j < 10000000; j++);
					deleteResult = myDBAdapter.deleteData();
					if (deleteResult) myDBAdapter.setSuccesfullTranzaction();
				}catch(Exception ex){Log.d("NATALIA!!! ", "Catch error");}
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
				}catch(Exception ex){}
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
				}catch(Exception ex){}
				myDBAdapter.endDatabaseTranzaction();
				callbackObject.dataWasLoaded(loadResult);
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
					//for (int j = 0; j < 10000000; j++);
					do {
						SMSParcer smsParcer = new SMSParcer(inboxSMSCursor.getString(
									inboxSMSCursor.getColumnIndex(SMSViewingAdapter.SMS_BODY_FIELD)), context);
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
	
	@Override
	public void onCreate(){
	    HandlerThread thread = new HandlerThread("ServiceStartArguments",
	    		Process.THREAD_PRIORITY_BACKGROUND);
	    thread.start();
	    
	    mServiceLooper = thread.getLooper();
	    mServiceHandler = new ServiceHandler(mServiceLooper);	
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
	    Log.d("NATALIA!!! ", "onStartCommand");

	    Message msg = mServiceHandler.obtainMessage();
	    msg.arg1 = startId;
	    if (intent.getAction().equals(SMSBankingGlobalClass.INSERT_DATA)){
	    	msg.what = INSERT_DATA;
	    	msg.obj = intent.getExtras();
	    }else if (intent.getAction().equals(SMSBankingGlobalClass.GET_CARDS_DATA)){
	    	msg.what = GET_CARDS_DATA;
	    	msg.obj = intent.getStringExtra(TransactionData.CARD_NUMBER);
	    }else if (intent.getAction().equals(SMSBankingGlobalClass.GET_TRANSACTION_DATA)){
	    	msg.what = GET_TRANSACTION_DATA;
	    	msg.obj = intent.getStringExtra(SMSBankingGlobalClass.DATA_FILTER);
	    }else if (intent.getAction().equals(SMSBankingGlobalClass.INSERT_CARD_DATA)){
	    	msg.what = INSERT_CARD_DATA;
	    	msg.obj = intent.getBundleExtra(SMSBankingGlobalClass.CARD_DATA);
	    }else if (intent.getAction().equals(SMSBankingGlobalClass.INSERT_TRANSACTION_DATA)){
	    	msg.what = INSERT_TRANSACTION_DATA;
	    	msg.obj = intent.getBundleExtra(SMSBankingGlobalClass.TRANSACTION_DATA);
	    }else if (intent.getAction().equals(SMSBankingGlobalClass.UPDATE_CARD_ALIAS)){
	    	msg.what = UPDATE_CARD_ALIAS;
	    	//It should be a HashMap
	    	msg.obj = intent.getBundleExtra(SMSBankingGlobalClass.CARD_ALIAS_DATA);
	    }else if (intent.getAction().equals(SMSBankingGlobalClass.DELETE_DATA)){
	    	msg.what = DELETE_DATA;
	    	msg.obj = intent.getBooleanExtra(SMSBankingGlobalClass.NEED_TO_LOAD_DATA_FROM_SMS, false);
	    }else if (intent.getAction().equals(SMSBankingGlobalClass.LOAD_DATA_FROM_SMS)){
	    	msg.what = LOAD_DATA_FROM_SMS;
	    }
	    mServiceHandler.sendMessage(msg);
	      
	    return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
	    Message msg = mServiceHandler.obtainMessage();
    	msg.what = OPEN_DATABASE;
    	//msg.obj = intent.getBundleExtra(SMSBankingGlobalClass.CONTEXT);
    	mServiceHandler.sendMessage(msg);
		// TODO Auto-generated method stub
		return myBinder;
	}

	public void setCallbackItem(DatabaseConnectionCallbackInterface callback){
		Log.d("NATALIA!!! ", "Set Callback");
		callbackObject = callback;
	}
	
	public void getTransactionData(String filter){
	    Message msg = mServiceHandler.obtainMessage();
    	msg.what = GET_TRANSACTION_DATA;
    	msg.obj = filter;
    	mServiceHandler.sendMessage(msg);
	}
	
	public void getBalance(String cardNumber){
	    Message msg = mServiceHandler.obtainMessage();
    	msg.what = GET_BALANCE;
    	msg.obj = cardNumber;
    	mServiceHandler.sendMessage(msg);		
	}
	
	public void getCardsData(String cardsNumber){
	    Message msg = mServiceHandler.obtainMessage();
    	msg.what = GET_CARDS_DATA;
    	msg.obj = cardsNumber;
    	mServiceHandler.sendMessage(msg);
	}
	
	public void updateCardAlias(String cardsNumber, String alias){
		HashMap<String, String> cardMap = new HashMap<String, String>();
		cardMap.put(SMSBankingGlobalClass.CARD_DATA, cardsNumber);
		cardMap.put(SMSBankingGlobalClass.CARD_ALIAS_DATA, alias);
	    Message msg = mServiceHandler.obtainMessage();
    	msg.what = UPDATE_CARD_ALIAS;
    	msg.obj = cardMap;
    	mServiceHandler.sendMessage(msg);
	}
	
	public void deleteAllData(boolean loadFromSMS){
	    Message msg = mServiceHandler.obtainMessage();
		if (loadFromSMS){
		    msg.what = DELETE_AND_LOAD_DATA_FROM_SMS;
	    	mServiceHandler.sendMessage(msg);			
		}else{
	    	msg.what = DELETE_DATA;
	    	mServiceHandler.sendMessage(msg);
		}
	}
	
	public void loadDataFromSMS(){
	    Message msg = mServiceHandler.obtainMessage();
		msg.what = LOAD_DATA_FROM_SMS;
	    mServiceHandler.sendMessage(msg);			
	}
}
