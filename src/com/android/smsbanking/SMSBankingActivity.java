package com.android.smsbanking;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;

import com.android.smsbanking.DataFilterActivity.MyOnOperationSelectedListener;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;


public class SMSBankingActivity extends ListActivity{
	
	private Context context;
	/*
	private Button sendSMSButton;
	private Button viewHistoryButton;
	private Button checkSMSButton;*/
	
	private TextView curBalance;

	private MyDBAdapter myDBAdapter;
	private Cursor transactionCursor;
	private ArrayList<TransactionData> transactionDatas;
	private TransactionAdapter transactionAdapter;
	private String filter;
	private HashMap<String, String> filterMap;
	
	private static final int ID_FILTER_ACTIVITY = 1;
	
	private static final int IDM_SMS_PROCESSING = 101;
	private static final int IDM_SET_FILTERS = 102;
	private static final int IDM_SET_CARD_FILTER = 103;
	
	/*private static final String BANK_ADDRESS = "5556";*/
	
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_history);
        
        context = getApplicationContext();
        filterMap = new HashMap<String, String>();
        filterMap.put(TransactionData.CARD_NUMBER, context.getResources().getString(R.string.all));
        filterMap.put(TransactionData.TRANSACTION_PLACE, context.getResources().getString(R.string.all));
        
        curBalance = (TextView) findViewById(R.id.current_balance);
  
        
        /*sendSMSButton = (Button) findViewById(R.id.send_sms);
        sendSMSButton.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		Intent intent = new Intent(SMSReceiver.SEND_SMS_ACTION); 
        		sendBroadcast(intent);    	
        	}
        });*/
        
        /*viewHistoryButton = (Button) findViewById(R.id.view_transaction_history);
        if (viewHistoryButton != null) {
        viewHistoryButton.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		Intent startIntent = new Intent();
        		startIntent.setClass(context, ViewHistoryListActivity.class);
        		startActivity(startIntent);
        	}
        });
        }*/
        
        /*checkSMSButton = (Button) findViewById(R.id.check_sms);
        checkSMSButton.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
            	Uri uriSMSURI = Uri.parse("content://sms/inbox");
                Cursor cur = getContentResolver().query(uriSMSURI, null, null, null,null);
                String sms = "";
                while (cur.moveToNext()) {
                    sms += "From :" + cur.getString(2) + " : " + cur.getString(11)+"\n";   
                    Log.d("NATALIA!!! ", sms);
                }        		
        	}
        });*/
                
        /*Intent intent = new Intent(SMSReceiver.BANK_ADDRESS_ACTION); 
        intent.putExtra(SMSReceiver.TYPE, BANK_ADDRESS); 
        sendBroadcast(intent);*/
        
        
		transactionDatas = new ArrayList<TransactionData>();
		int resId = R.layout.list_item;
		transactionAdapter = new TransactionAdapter(context, resId, transactionDatas);
		setListAdapter(transactionAdapter);
		
		showTransactionList();
		
		
        /*TextView curBalance = (TextView) findViewById(R.id.current_balance);
        String str = new String();
        str += context.getResources().getString(R.string.operation_balance) + " " + TransactionData.getBalance() + TransactionData.getBalanceCurrency();
        curBalance.setText(str);*/
        
       // try{
        //backupDb();
        //}
       // /catch (Exception e) {}
        //finally{
       // 	Log.d("NATALIA!!!", "IOException");
        //}
    }
    
    
    private void backupDb() throws IOException {
        File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();

        if (sd.canWrite()) {

            String currentDBPath = "/data/com.android.smsbanking/databases/smsbanking_base";
            String backupDBPath = "/temp/smsbanking_base";

            File currentDB = new File(data, currentDBPath);
            File backupDB = new File(sd, backupDBPath);

            if (backupDB.exists())
                backupDB.delete();

            if (currentDB.exists()) {
                makeLogsFolder();

                copy(currentDB, backupDB);
           }

            //dbFilePath = backupDB.getAbsolutePath();
       }
    }
    
    private void copy(File from, File to) throws FileNotFoundException, IOException {
        FileChannel src = null;
        FileChannel dst = null;
        try {
            src = new FileInputStream(from).getChannel();
            dst = new FileOutputStream(to).getChannel();
            dst.transferFrom(src, 0, src.size());
        }
        finally {
            if (src != null)
                src.close();
            if (dst != null)
                dst.close();
        }
    }

    private void makeLogsFolder() {
       try {
           File sdFolder = new File(Environment.getExternalStorageDirectory(), "/temp/");
           sdFolder.mkdirs();
       }
       catch (Exception e) {}
     }
    
    public boolean onCreateOptionsMenu(Menu menu){
    	menu.add(Menu.NONE, IDM_SMS_PROCESSING, Menu.NONE, context.getResources().getString(R.string.sms_process));
    	menu.add(Menu.NONE, IDM_SET_FILTERS, Menu.NONE, context.getResources().getString(R.string.set_filters));
    	return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item){
    	switch(item.getItemId()){
    	case IDM_SMS_PROCESSING:
    		Intent startIntentProcessing = new Intent();
    		startIntentProcessing.setClass(context, Settings.class);
    		startActivity(startIntentProcessing);   
    		return true;
    	case IDM_SET_FILTERS:
    		Intent startIntentFilters = new Intent();
    		startIntentFilters.setClass(context, DataFilterActivity.class);
    		startIntentFilters.putExtra(TransactionData.CARD_NUMBER, filterMap.get(TransactionData.CARD_NUMBER));
    		startIntentFilters.putExtra(TransactionData.TRANSACTION_PLACE, filterMap.get(TransactionData.TRANSACTION_PLACE));
    		startActivityForResult(startIntentFilters, ID_FILTER_ACTIVITY);   
    		return true;
    	}
    	return false;
    }
    
    @Override
    public void onPause(){
    	super.onPause();
    	Log.d("NATALIA!!! ", "onPause ");
		if((transactionCursor != null) && (!transactionCursor.isClosed())){
			transactionCursor.close();
		}
		myDBAdapter.close();
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	Log.d("NATALIA!!! ", "onResume ");
    	showTransactionList();
    }
    
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		TransactionData transactionData = (TransactionData) getListAdapter().getItem(position);
		  
		Intent startIntent = new Intent();
		startIntent.setClass(context, SMSDetailActivity.class);
		SMSReceiver.fillIntent(startIntent, transactionData);
		startActivity(startIntent);
	}

	private String getSQLWhereFromFilter(){
		String sqlString = new String();
		if (!filterMap.get(TransactionData.CARD_NUMBER).equals(context.getResources().getString(R.string.all))){
			sqlString += TransactionData.CARD_NUMBER + "='" + filterMap.get(TransactionData.CARD_NUMBER) + "'";
		}
		if (!filterMap.get(TransactionData.TRANSACTION_PLACE).equals(context.getResources().getString(R.string.all))){
			if (sqlString.length() != 0)
				sqlString += " AND ";
			
	    	if (filterMap.get(TransactionData.TRANSACTION_PLACE).equals(context.getResources().getString(R.string.card_operations))){
	    		sqlString += "(" + TransactionData.TRANSACTION_PLACE + "<>'" + TransactionData.INCOMING_BANK_OPERATION + "') AND (" + TransactionData.TRANSACTION_PLACE + "<>'" + TransactionData.OUTGOING_BANK_OPERATION + "')";
	    	} else if (filterMap.get(TransactionData.TRANSACTION_PLACE).equals(context.getResources().getString(R.string.incoming_operations))) {
	    		sqlString += TransactionData.TRANSACTION_PLACE + "='" + TransactionData.INCOMING_BANK_OPERATION + "'";
 	    	} else {
	    		sqlString += TransactionData.TRANSACTION_PLACE + "='" + TransactionData.OUTGOING_BANK_OPERATION + "'";
 	    	}
		}
		return sqlString;
	}
	
	//We have to close connection to DB
	private void showTransactionList(){
		myDBAdapter = new MyDBAdapter(context);
		myDBAdapter.open();

		String filterString = getSQLWhereFromFilter();
		transactionCursor = myDBAdapter.getTransactionWithFilter(filterString);
		startManagingCursor(transactionCursor);
		updateTransactionList();
		if((transactionCursor != null) && (!transactionCursor.isClosed())){
			transactionCursor.close();
		}
		try{
            backupDb();
            }
            catch (Exception e) {}
            finally{
            	Log.d("NATALIA!!!", "IOException");
            }
        myDBAdapter.close();
    
	}

	private void updateTransactionList(){
		transactionCursor.requery();
		
		int i = 0;
		
		transactionDatas.clear();
		
		if (transactionCursor.moveToFirst()){
			do {
				TransactionData transactionData = myDBAdapter.getTransactionFromCursor(transactionCursor);
				transactionDatas.add(0, transactionData);
				i++;
			} while (transactionCursor.moveToNext());
			if (!filterMap.get(TransactionData.CARD_NUMBER).equals(context.getResources().getString(R.string.all))){
		        String str = new String();
		        str += context.getResources().getString(R.string.operation_balance) + " " + transactionDatas.get(i - 1).getFundValue() + transactionDatas.get(i - 1).getFundCurrency();
		        curBalance.setText(str);
			}
			else{
				curBalance.setText(null);
			}
		} 
		else {
			Toast.makeText(context, "No data from bank.", Toast.LENGTH_LONG).show();
			finish();
		}
		
		transactionAdapter.notifyDataSetChanged();
	}

	  @Override
	  public void onDestroy() {
	    super.onDestroy();
	      
	    // Close the database
		if((transactionCursor != null) && (!transactionCursor.isClosed())){
			transactionCursor.close();
		}
	    myDBAdapter.close();
	  }

	    @Override
	    protected void onActivityResult(int requestCode, int resultCode, Intent data){
	    	if (requestCode == ID_FILTER_ACTIVITY){
	    		if (data != null){
	    			filterMap.clear();
	    			filterMap.put(TransactionData.CARD_NUMBER, data.getStringExtra(TransactionData.CARD_NUMBER));
	    			filterMap.put(TransactionData.TRANSACTION_PLACE, data.getStringExtra(TransactionData.TRANSACTION_PLACE));
	    			showTransactionList();
	    		}
	    	}
	    }
		public class MyOnOperationSelectedListener implements OnItemSelectedListener {

		    public void onItemSelected(AdapterView<?> parent,
		        View view, int pos, long id) {
		    	/*filterMap.remove(TransactionData.TRANSACTION_PLACE);
		    	if (pos == 0){
		    		filterMap.put(TransactionData.TRANSACTION_PLACE, context.getResources().getString(R.string.all));
		    	} else if (pos == 1){
		    		filterMap.put(TransactionData.TRANSACTION_PLACE, context.getResources().getString(R.string.card_operations));
		    	} else if (pos == 2) {
		    		filterMap.put(TransactionData.TRANSACTION_PLACE, context.getResources().getString(R.string.incoming_operations));
	 	    	} else {
		    		filterMap.put(TransactionData.TRANSACTION_PLACE, context.getResources().getString(R.string.outgoing_operations));
	 	    	}*/
		    }

		    public void onNothingSelected(AdapterView parent) {
		      // Do nothing.
		    }
		}

}