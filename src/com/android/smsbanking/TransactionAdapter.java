package com.android.smsbanking;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TransactionAdapter extends ArrayAdapter<TransactionData>{
	int viewResourceId;

	public TransactionAdapter(Context context, int resourceId,
			List<TransactionData> objects) {
		super(context, resourceId, objects);
		viewResourceId = resourceId;
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		LinearLayout transactionView;

		TransactionData item = getItem(position);

	    String date = item.getTransactionDate();
	    String place = item.getTransactionPlace();
	    String amount = Float.toString(item.getTransactionValue()) + item.getTransactionCurrency();
	    String textForList = date + " byla provedena tranzaccija na summu " + amount;

	    if (convertView == null) {
	    	transactionView = new LinearLayout(getContext());
	    	String inflater = Context.LAYOUT_INFLATER_SERVICE;
	    	LayoutInflater vi = (LayoutInflater)getContext().getSystemService(inflater);
	    	vi.inflate(viewResourceId, transactionView, true);
	    } else {
	    	transactionView = (LinearLayout) convertView;
	    }
	    
	    TextView textView = (TextView) transactionView.findViewById(R.id.transaction_item);
	    textView.setText(textForList);
	    
	    return transactionView;
		
	}



}
