package com.android.smsbanking;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ViewHistoryListActivity extends ListActivity {
	
	private Context context;
	
	private MyDBAdapter myDBAdapter;
	private Cursor transactionCursor;
	private ArrayList<TransactionData> transactionDatas;
	private TransactionAdapter transactionAdapter;
	private String filter;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
        setContentView(R.layout.view_history);
        
		context = getApplicationContext();
		filter = getIntent().getStringExtra(MyDBAdapter.FILTER_VALUE);
		Log.d("NATALIA!!! ViewHistoryListActivity", "filterForData:" + filter);

		transactionDatas = new ArrayList<TransactionData>();
		int resId = R.layout.list_item;
		transactionAdapter = new TransactionAdapter(context, resId, transactionDatas);
		setListAdapter(transactionAdapter);
		
/*		ListView list = getListView();
		list.setOnItemClickListener(new OnItemClickListener(){
			  @Override
			  protected void onListItemClick(ListView l, View v, int position, long id) {
				  TransactionData transactionData = (TransactionData) getListAdapter().getItem(position);
				  
			  }
			
		})*/
		

		myDBAdapter = new MyDBAdapter(context);
		myDBAdapter.open();
		
		showTransactionList();
        TextView curBalance = (TextView) findViewById(R.id.current_balance);
        String str = new String();
        str += "Balance:" + TransactionData.getBalance() + TransactionData.getBalanceCurrency();
        curBalance.setText(str);
    }
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		  TransactionData transactionData = (TransactionData) getListAdapter().getItem(position);
		  
		  Intent startIntent = new Intent();
		  startIntent.setClass(context, SMSDetailActivity.class);
		  SMSReceiver.fillIntent(startIntent, transactionData);
		  startActivity(startIntent);
		  finish();
	  }

	private void showTransactionList(){
		//transactionCursor = myDBAdapter.getDataByQuery();
		transactionCursor = myDBAdapter.getTransactionWithFilter(filter);
		startManagingCursor(transactionCursor);
		updateTransactionList();
	}
	
	private void getTransactionFromCursor(TransactionData transactionData){
		
		transactionData.setBankName(TransactionData.DEFAULT_BANK_NAME);
		transactionData.setCardNumber(transactionCursor.getString(transactionCursor.getColumnIndex(TransactionData.CARD_NUMBER)));
		transactionData.setFundCurrency(transactionCursor.getString(transactionCursor.getColumnIndex(TransactionData.FUND_CURRENCY)));
		transactionData.setTransactionCurrency(transactionCursor.getString(transactionCursor.getColumnIndex(TransactionData.TRANSACTION_CURRENCY)));
		transactionData.setTransactionDate(transactionCursor.getString(transactionCursor.getColumnIndex(TransactionData.TRANSACTION_DATE)));
		transactionData.setTransactionPlace(transactionCursor.getString(transactionCursor.getColumnIndex(TransactionData.TRANSACTION_PLACE)));
		transactionData.setFundValue(transactionCursor.getFloat(transactionCursor.getColumnIndex(TransactionData.FUND_VALUE)));
		transactionData.setTransactionValue(transactionCursor.getFloat(transactionCursor.getColumnIndex(TransactionData.TRANSACTION_VALUE)));
		
		Log.d("NATALIA new elem", "address " + transactionData + " cursor " + transactionCursor + " " + transactionData.getTransactionValue());
	}
	
	private void updateTransactionList(){
		transactionCursor.requery();
		
		int i = 0;
		
		transactionDatas.clear();
		
		if (transactionCursor.moveToFirst()){
			do {
				TransactionData transactionData = new TransactionData();
				getTransactionFromCursor(transactionData);
				transactionDatas.add(0, transactionData);
				for (i = 0; i < transactionDatas.size(); i++){
					Log.d("NATALIA elems", "Number " + i + " " + transactionDatas.get(i).getTransactionValue() + " " + transactionDatas);
				}
			} while (transactionCursor.moveToNext());
			TransactionData.setBalance(transactionDatas.get(0).getFundValue());
			TransactionData.setBalanceCurrency(transactionDatas.get(0).getFundCurrency());
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
	    myDBAdapter.close();
	  }
	  
}

