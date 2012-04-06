package com.meryrua.smsbanking;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.meryrua.smsbanking.R;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TransactionAdapter extends CursorAdapter{
	//int viewResourceId;
	private Context myContext;
	private Resources resources;
	
	public TransactionAdapter(Context context, Cursor cursor) {
		super(context, cursor);
		//viewResourceId = resourceId;
		myContext = context;
		resources = myContext.getResources();
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void bindView(View view, Context viewContext, Cursor cursor) {
		// TODO Auto-generated method stub
		ImageView operationImage;

		TransactionData item = new TransactionData(cursor);
	    
	    operationImage = (ImageView) view.findViewById(R.id.operation_icon);
	    
	    String date = item.getTransactionDate();
	    String place = item.getTransactionPlace();
	    String amount = Float.toString(item.getTransactionValue()) + item.getTransactionCurrency();
	    String textForList = new String (); //Is it correct???
	    if (place.equals(TransactionData.INCOMING_BANK_OPERATION)){
	    	textForList += date + " " + resources.getString(R.string.string_incoming_operation) + " " + amount;
	    	operationImage.setImageDrawable(resources.getDrawable(R.drawable.ic_list_green));
	    } else if (place.equals(TransactionData.OUTGOING_BANK_OPERATION)){
	    	textForList += date + " " + resources.getString(R.string.string_outgoing_operation) + " " + amount;	    	
	    	operationImage.setImageDrawable(resources.getDrawable(R.drawable.ic_list_red));
	    } else {
	    	textForList += date + " " + resources.getString(R.string.string_transaction) + " " + amount;
	    	operationImage.setImageDrawable(resources.getDrawable(R.drawable.ic_list_red));
	    }
	    
	    TextView textView = (TextView) view.findViewById(R.id.transaction_item);
	    textView.setText(textForList);
	    	
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		// TODO Auto-generated method stub
		Log.d("NATALIA!!! ", "newView  " + cursor.getString(cursor.getColumnIndex(MyDBAdapter.ID)));
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.list_item, parent, false);
		bindView(v, context, cursor);
		return v;

	}
	
	static class ViewHolder{
		TextView text;
		ImageView image;
	}



}