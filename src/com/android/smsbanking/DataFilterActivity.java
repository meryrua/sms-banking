package com.android.smsbanking;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class DataFilterActivity extends Activity{

	private Context context;
	private static String filterForCard = null;
	private static String filterForOperation = null;
	private static String filterForData = null;
	private static Button setFilter;
	
	private ArrayList<String> cardsNumbers;
	private MyDBAdapter myDBAdapter;
	private static Cursor cursor;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.transaction_filter);
		
		context = getApplicationContext();
		cardsNumbers = new ArrayList<String>();
		
		myDBAdapter = new MyDBAdapter(context);
		myDBAdapter.open();
		
		Spinner cardFilter = (Spinner) findViewById(R.id.card_number);
		ArrayAdapter<String> cardAdapter = new ArrayAdapter(context, android.R.layout.simple_spinner_item);
		getCardsNumber(cardAdapter);
		cardAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		cardFilter.setAdapter(cardAdapter);
		cardFilter.setOnItemSelectedListener(new MyOnCardSelectedListener());
		
		myDBAdapter.close();	
		
		Spinner operationFilter = (Spinner) findViewById(R.id.operation);
		ArrayAdapter<String> operationAdapter = new ArrayAdapter(context, android.R.layout.simple_spinner_item);
		setFilterOperation(operationAdapter);
		operationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		operationFilter.setAdapter(operationAdapter);
		operationFilter.setOnItemSelectedListener(new MyOnOperationSelectedListener());
		
		setFilter = (Button) findViewById(R.id.set_filter);
		setFilter.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				Intent startIntent = new Intent();
        		startIntent.setClass(context, ViewHistoryListActivity.class);
        		if ((filterForCard != null) && (filterForOperation != null)){
            		filterForData = new String (filterForCard + " AND " + filterForOperation);        			
        		}else if (filterForCard != null){
        			filterForData = new String(filterForCard);
        		}else if (filterForOperation != null){
        			filterForData = new String(filterForOperation);
        		}
        		startIntent.putExtra(MyDBAdapter.FILTER_VALUE, filterForData);
        		Log.d("NATALIA!!!", "filterForData:" + filterForData);
        		startActivity(startIntent);
        		finish();
			}
		});
	}
	
	private void getCardsNumber(ArrayAdapter<String> adapter){
		cursor = myDBAdapter.getCardsNumber();
		
		cardsNumbers.clear();
		
		if (cursor.moveToFirst()){
			adapter.add("All");
			do{
				cardsNumbers.add(0, cursor.getString(cursor.getColumnIndex(TransactionData.CARD_NUMBER)));
				adapter.add(cursor.getString(cursor.getColumnIndex(TransactionData.CARD_NUMBER)));
			} while (cursor.moveToNext());
		}
		else {
			Toast.makeText(context, "No data from bank.", Toast.LENGTH_LONG).show();
			finish();
		}
	}
	
	public class MyOnCardSelectedListener implements OnItemSelectedListener {

	    public void onItemSelected(AdapterView<?> parent,
	        View view, int pos, long id) {
	    	if (pos == 0){
	    		filterForCard = null;
	    	} else {
	    		filterForCard = new String(TransactionData.CARD_NUMBER + "=" + parent.getItemAtPosition(pos).toString());
	    	}
	    }

	    public void onNothingSelected(AdapterView parent) {
	      // Do nothing.
	    }
	}
	
	private void setFilterOperation(ArrayAdapter<String> adapter){
			adapter.add("All");
			adapter.add("Card operations");
			adapter.add("Incoming fund operations");
			adapter.add("Outgoing fund operations");
	}
	
	public class MyOnOperationSelectedListener implements OnItemSelectedListener {

	    public void onItemSelected(AdapterView<?> parent,
	        View view, int pos, long id) {
	    	if (pos == 0){
	    		filterForOperation = null;
	    	} else if (pos == 1){
	    		filterForOperation = new String("(" + TransactionData.TRANSACTION_PLACE + "<>'" + TransactionData.INCOMING_BANK_OPERATION + "') AND (" + TransactionData.TRANSACTION_PLACE + "<>'" + TransactionData.OUTGOING_BANK_OPERATION + "')");
	    	} else if (pos == 2) {
	    		filterForOperation = new String(TransactionData.TRANSACTION_PLACE + "='" + TransactionData.INCOMING_BANK_OPERATION + "'");
 	    	} else {
	    		filterForOperation = new String(TransactionData.TRANSACTION_PLACE + "='" + TransactionData.OUTGOING_BANK_OPERATION + "'");
 	    	}
	    }

	    public void onNothingSelected(AdapterView parent) {
	      // Do nothing.
	    }
	}

}
