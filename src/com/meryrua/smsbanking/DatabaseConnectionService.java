package com.meryrua.smsbanking;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

public class DatabaseConnectionService extends Service{
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;
	private MyDBAdapter myDBAdapter;
	private IBinder myBinder = new MyBinder();
	
	protected static final int GET_TRANSACTION_DATA = 1;
	protected static final int GET_CARDS_DATA = 2;
	protected static final int INSERT_TRANSACTION_DATA = 3;
	protected static final int INSERT_CARD_DATA = 4;
	protected static final int UPDATE_CARD_ALIAS = 5;
	protected static final int DELETE_DATA = 6;
	protected static final int LOAD_DATA_FROM_SMS = 7;

	private final class ServiceHandler extends Handler {
				
	    public ServiceHandler(Looper looper) {
	        super(looper);
	    }
	    
	    @Override
	    public void handleMessage(Message msg) {
	    	switch(msg.what){
	    	case GET_TRANSACTION_DATA:
	    		break;
	    	case GET_CARDS_DATA:
	    		break;
	    	case INSERT_TRANSACTION_DATA:
	    		break;
	    	case INSERT_CARD_DATA:
	    		break;
	    	case UPDATE_CARD_ALIAS:
	    		break;	 
	    	case DELETE_DATA:
	    		break;	 
	    	case LOAD_DATA_FROM_SMS:
	    		break;	 
	    	default:
	    		break;
	    	}
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
	public int onStartCommand(Intent intent, int flags, int startId) {
	    Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

	    Message msg = mServiceHandler.obtainMessage();
	    msg.arg1 = startId;
	    if (intent.getAction().equals(SMSBankingGlobalClass.GET_CARDS_DATA)){
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
		// TODO Auto-generated method stub
		return myBinder;
	}

	public void setCallbackItem(){
		Log.d("NATALIA!!! ", "Set Callback");
		
	}
}
