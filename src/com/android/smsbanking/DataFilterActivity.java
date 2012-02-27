package com.android.smsbanking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	private static Button setFilter;
	
	private ArrayList<String> cardsNumbers;
	private MyDBAdapter myDBAdapter;
	private static Cursor cursor;
	
	private static HashMap<String, String> filterMap = new HashMap<String, String>(); 
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.transaction_filter);
		
		filterMap.put(TransactionData.CARD_NUMBER, getIntent().getStringExtra(TransactionData.CARD_NUMBER));
		filterMap.put(TransactionData.TRANSACTION_PLACE, getIntent().getStringExtra(TransactionData.TRANSACTION_PLACE));
		
		context = getApplicationContext();
		cardsNumbers = new ArrayList<String>();
		
		myDBAdapter = new MyDBAdapter(context);
		myDBAdapter.open();
		
		Spinner cardFilter = (Spinner) findViewById(R.id.card_number);
		ArrayAdapter<String> cardAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item);
		int selectedCard = getCardsNumber(cardAdapter);
		cardAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		cardFilter.setAdapter(cardAdapter);
		cardFilter.setSelection(selectedCard);
		cardFilter.setOnItemSelectedListener(new MyOnCardSelectedListener());
		
		myDBAdapter.close();	
		
		Spinner operationFilter = (Spinner) findViewById(R.id.operation);
		ArrayAdapter<String> operationAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, context.getResources().getStringArray(R.array.array_for_operation_filter));
		int selectedOperation = 0;
		for (int i = 0; i < operationAdapter.getCount(); i++){
			if (operationAdapter.getItem(i).equals(filterMap.get(TransactionData.TRANSACTION_PLACE)))
				selectedOperation = i;
		}
		operationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		operationFilter.setAdapter(operationAdapter);
		operationFilter.setSelection(selectedOperation);
		operationFilter.setOnItemSelectedListener(new MyOnOperationSelectedListener());
		
		Spinner dateFilter = (Spinner) findViewById(R.id.date_filter);
		ArrayAdapter<String> dateAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, context.getResources().getStringArray(R.array.array_for_date_period));
		dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		dateFilter.setAdapter(dateAdapter);
		dateFilter.setOnItemSelectedListener(new MyOnDateSelectedListener());
		
		setFilter = (Button) findViewById(R.id.set_filter);
		setFilter.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				Intent startIntent = new Intent();
				startIntent.putExtra(TransactionData.CARD_NUMBER, filterMap.get(TransactionData.CARD_NUMBER));
				startIntent.putExtra(TransactionData.TRANSACTION_PLACE, filterMap.get(TransactionData.TRANSACTION_PLACE));
        		setResult(RESULT_OK, startIntent);
        		finish();
			}
		});
	}
	
	private int getCardsNumber(ArrayAdapter<String> adapter){
		int i = 0;
		int j = 0;
		cursor = myDBAdapter.getCardsNumber();
		
		cardsNumbers.clear();
		
		if (cursor.moveToFirst()){
			adapter.add(context.getResources().getString(R.string.all));
			j++;
			do{
				if (filterMap.get(TransactionData.CARD_NUMBER) != null){
					if (filterMap.get(TransactionData.CARD_NUMBER).equals(cursor.getString(cursor.getColumnIndex(TransactionData.CARD_NUMBER))))
						i = j;
				}
				cardsNumbers.add(0, cursor.getString(cursor.getColumnIndex(TransactionData.CARD_NUMBER)));
				adapter.add(cursor.getString(cursor.getColumnIndex(TransactionData.CARD_NUMBER)));
				j++;
			} while (cursor.moveToNext());
			cursor.close();
			return i;
		}
		else {
			Toast.makeText(context, "No data from bank.", Toast.LENGTH_LONG).show();
			finish();
			return 0;
		}
	}
	
	public class MyOnCardSelectedListener implements OnItemSelectedListener {

	    public void onItemSelected(AdapterView<?> parent,
	        View view, int pos, long id) {
	    	filterMap.remove(TransactionData.CARD_NUMBER);
	    	if (pos == 0){
	    		filterMap.put(TransactionData.CARD_NUMBER, context.getResources().getString(R.string.all));
	    	} else {
	    		filterMap.put(TransactionData.CARD_NUMBER, parent.getItemAtPosition(pos).toString());
	    	}
	    }

	    public void onNothingSelected(AdapterView parent) {
	      // Do nothing.
	    }
	}
	
	public class MyOnOperationSelectedListener implements OnItemSelectedListener {

	    public void onItemSelected(AdapterView<?> parent,
	        View view, int pos, long id) {
	    	filterMap.remove(TransactionData.TRANSACTION_PLACE);
	    	if (pos == 0){
	    		filterMap.put(TransactionData.TRANSACTION_PLACE, context.getResources().getString(R.string.all));
	    	} else if (pos == 1){
	    		filterMap.put(TransactionData.TRANSACTION_PLACE, context.getResources().getString(R.string.card_operations));
	    	} else if (pos == 2) {
	    		filterMap.put(TransactionData.TRANSACTION_PLACE, context.getResources().getString(R.string.incoming_operations));
 	    	} else {
	    		filterMap.put(TransactionData.TRANSACTION_PLACE, context.getResources().getString(R.string.outgoing_operations));
 	    	}
	    }

	    public void onNothingSelected(AdapterView parent) {
	      // Do nothing.
	    }
	}
	
	public class MyOnDateSelectedListener implements OnItemSelectedListener{
		
		public void onItemSelected(AdapterView<?> parent,
				View view, int pos, long id) {
			
		}
		
	    public void onNothingSelected(AdapterView parent) {
		      // Do nothing.
		}
	}

}
