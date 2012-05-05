package com.meryrua.smsbanking;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SMSBankingApplication extends Application {
    
    //private static final String LOG_TAG = "com.meryrua.smsbanking:SMSBankingApplication";
    public static HashMap<String, ArrayList<String>> operationPatterns;
    public static final String FIRST_LOADING = "first_loading";

	@Override
	public void onCreate() {
		super.onCreate();
		CrashReporter crashReporter = new CrashReporter();
		crashReporter.init(this);
		
	    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	    if (!settings.contains(FIRST_LOADING)) {
	        SharedPreferences.Editor editor = settings.edit();
	        editor.putBoolean(FIRST_LOADING, false);
	        editor.commit();
	        setDefaultPatterns();
	    } else {
	        getOperationPatterns(getApplicationContext());	        
	    }
	    //getOperationParrensBase(getApplicationContext());   
	}
	
    private void setDefaultPatterns() {
        XMLParcerSerializer xmlSerializer = new XMLParcerSerializer();
        operationPatterns = new HashMap<String, ArrayList<String>>();
        operationPatterns.put(XMLParcerSerializer.TRANSACTION_TAG, new ArrayList<String>());
        operationPatterns.get(XMLParcerSerializer.TRANSACTION_TAG).add(SMSParcer.DEFAULT_TRANSACTION_PATTERN);
        operationPatterns.put(XMLParcerSerializer.INCOMING_TAG, new ArrayList<String>());
        operationPatterns.get(XMLParcerSerializer.INCOMING_TAG).add(SMSParcer.DEFAULT_INCOMING_PATTERN);
        operationPatterns.put(XMLParcerSerializer.OUTGOING_TAG, new ArrayList<String>());
        operationPatterns.get(XMLParcerSerializer.OUTGOING_TAG).add(SMSParcer.DEFAULT_OUTGOING_PATTERN);
        xmlSerializer.serializePatterns(operationPatterns, getApplicationContext());
    }

	private void getOperationPatterns(Context context) {
	    XMLParcerSerializer xmlSerializer = new XMLParcerSerializer();
	    operationPatterns = xmlSerializer.parcePatterns(context);
	    if (!operationPatterns.containsKey(XMLParcerSerializer.TRANSACTION_TAG)) {
	        operationPatterns.put(XMLParcerSerializer.TRANSACTION_TAG, new ArrayList<String>());
	        operationPatterns.get(XMLParcerSerializer.TRANSACTION_TAG).add(SMSParcer.DEFAULT_TRANSACTION_PATTERN);
	    }
	    if (!operationPatterns.containsKey(XMLParcerSerializer.INCOMING_TAG)) {
	        operationPatterns.put(XMLParcerSerializer.INCOMING_TAG, new ArrayList<String>());
	        operationPatterns.get(XMLParcerSerializer.INCOMING_TAG).add(SMSParcer.DEFAULT_INCOMING_PATTERN);
	    }
	    if (!operationPatterns.containsKey(XMLParcerSerializer.OUTGOING_TAG)) {
	        operationPatterns.put(XMLParcerSerializer.OUTGOING_TAG, new ArrayList<String>());
	        operationPatterns.get(XMLParcerSerializer.OUTGOING_TAG).add(SMSParcer.DEFAULT_OUTGOING_PATTERN);
	    }
	}
	
	/*private void getOperationParrensBase(Context context){
	    Log.d(LOG_TAG, "start getting patterns");
	    operationPatterns = new HashMap<String, ArrayList<String>>();
	    MyDBAdapter myDBAdapter = new MyDBAdapter(getApplicationContext());
	    myDBAdapter.openToRead();
	    Cursor cursor = myDBAdapter.getOperationPattern();
	    if (cursor != null){
	        if (cursor.moveToFirst()){
    	        do{
    	            if (cursor.getString(cursor.getColumnIndex(MyDBAdapter.TRANSACTION_PATTERN_STRING)) != null){
            	        if (!operationPatterns.containsKey(XMLParcerSerializer.TRANSACTION_TAG)){
            	            operationPatterns.put(XMLParcerSerializer.TRANSACTION_TAG, new ArrayList<String>());
            	        }
            	        operationPatterns.get(XMLParcerSerializer.TRANSACTION_TAG).add(cursor.getString(cursor.getColumnIndex(MyDBAdapter.TRANSACTION_PATTERN_STRING)));
    	            }
    	            if (cursor.getString(cursor.getColumnIndex(MyDBAdapter.INCOMING_OPERATION_PATTERN_STRING)) != null){
                        if (!operationPatterns.containsKey(XMLParcerSerializer.INCOMING_TAG)){
                            operationPatterns.put(XMLParcerSerializer.INCOMING_TAG, new ArrayList<String>());
                        }
                        operationPatterns.get(XMLParcerSerializer.INCOMING_TAG).add(cursor.getString(cursor.getColumnIndex(MyDBAdapter.INCOMING_OPERATION_PATTERN_STRING)));
                    }
    	            if (cursor.getString(cursor.getColumnIndex(MyDBAdapter.OUTGOING_OPERATION_PATTERN_STRING)) != null){
                        if (!operationPatterns.containsKey(XMLParcerSerializer.OUTGOING_TAG)){
                            operationPatterns.put(XMLParcerSerializer.OUTGOING_TAG, new ArrayList<String>());
                        }
                        operationPatterns.get(XMLParcerSerializer.OUTGOING_TAG).add(cursor.getString(cursor.getColumnIndex(MyDBAdapter.OUTGOING_OPERATION_PATTERN_STRING)));
                    }
        	    }while (cursor.moveToNext());
    	    }
    	    cursor.close();
	    }
	    Log.d(LOG_TAG, "end getting patterns");
	    if (!operationPatterns.containsKey(XMLParcerSerializer.TRANSACTION_TAG)){
	        operationPatterns.put(XMLParcerSerializer.TRANSACTION_TAG, new ArrayList<String>());
	        operationPatterns.get(XMLParcerSerializer.TRANSACTION_TAG).add(SMSParcer.DEFAULT_TRANSACTION_PATTERN);
	    }
	    if (!operationPatterns.containsKey(XMLParcerSerializer.INCOMING_TAG)){
	        operationPatterns.put(XMLParcerSerializer.INCOMING_TAG, new ArrayList<String>());
	        operationPatterns.get(XMLParcerSerializer.INCOMING_TAG).add(SMSParcer.DEFAULT_INCOMING_PATTERN);
	    }
	    if (!operationPatterns.containsKey(XMLParcerSerializer.OUTGOING_TAG)){
	        operationPatterns.put(XMLParcerSerializer.OUTGOING_TAG, new ArrayList<String>());
	        operationPatterns.get(XMLParcerSerializer.OUTGOING_TAG).add(SMSParcer.DEFAULT_OUTGOING_PATTERN);
	    }
	}*/
}
