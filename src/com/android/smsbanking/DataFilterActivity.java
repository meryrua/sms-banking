package com.android.smsbanking;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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
		ArrayAdapter<String> adapter = new ArrayAdapter(context, android.R.layout.simple_spinner_item);
		getCardsNumber(adapter);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		cardFilter.setAdapter(adapter);
		cardFilter.setOnItemSelectedListener(new MyOnItemSelectedListener());
		
		myDBAdapter.close();
		
		setFilter = (Button) findViewById(R.id.set_filter);
		setFilter.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				Intent startIntent = new Intent();
        		startIntent.setClass(context, ViewHistoryListActivity.class);
        		startIntent.putExtra(MyDBAdapter.FILTER_VALUE, filterForData);
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
	
	public class MyOnItemSelectedListener implements OnItemSelectedListener {

	    public void onItemSelected(AdapterView<?> parent,
	        View view, int pos, long id) {
	    	if (pos == 0){
	    		filterForData = null;
	    	} else {
	    		filterForData = new String(TransactionData.CARD_NUMBER + "=" + parent.getItemAtPosition(pos).toString());
	    	}
	    }

	    public void onNothingSelected(AdapterView parent) {
	      // Do nothing.
	    }
	}
}
