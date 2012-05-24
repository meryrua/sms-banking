package com.meryrua.smsbanking;

import com.meryrua.smsbanking.R;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TransactionAdapter extends CursorAdapter {
	//int viewResourceId;
	private Context myContext;
	private Resources resources;
	
	//private static final String LOG_TAG = "com.meryrua.smsbanking:TransactionAdapter";
	
	public TransactionAdapter(Context context, Cursor cursor) {
		super(context, cursor);
		myContext = context;
		resources = myContext.getResources();

	}
	
	@Override
	public void bindView(View view, Context viewContext, Cursor cursor) {
		ImageView operationImage;

		TransactionData item = new TransactionData(cursor);
	    
	    operationImage = (ImageView) view.findViewById(R.id.operation_icon);
	    
	    String date = item.getTransactionDate();
	    String operation = item.getOperation();
	    String amount = Float.toString(item.getTransactionValue()) + item.getTransactionCurrency();
	    String textForList = null;
	    if (operation.equals(TransactionData.INCOMING_BANK_OPERATION)) {
	    	textForList = date + " " + resources.getString(R.string.string_incoming_operation) + " " + amount;
	    	operationImage.setImageDrawable(resources.getDrawable(R.drawable.ic_list_green));
	    } else
	        if (operation.equals(TransactionData.OUTGOING_BANK_OPERATION)) {
	            textForList = date + " " + resources.getString(R.string.string_outgoing_operation) + " " + amount;	    	
	            operationImage.setImageDrawable(resources.getDrawable(R.drawable.ic_list_red));
	        } else {
	            textForList = date + " " + resources.getString(R.string.string_transaction) + " " + amount;  
	            operationImage.setImageDrawable(resources.getDrawable(R.drawable.ic_list_red));
	        }
	    
	    TextView textView = (TextView) view.findViewById(R.id.transaction_item);
	    textView.setText(textForList);
	    	
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.list_item, parent, false);
		bindView(v, context, cursor);
		return v;
	}
}
