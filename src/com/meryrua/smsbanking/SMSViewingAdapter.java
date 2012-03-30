package com.meryrua.smsbanking;

import com.meryrua.smsbanking.R;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class SMSViewingAdapter extends CursorAdapter{
	
	public final static String SMS_ID_FIELD = "_id";
	public final static String SMS_DATE_FIELD = "date";
	public final static String SMS_BODY_FIELD = "body";

	public SMSViewingAdapter(Context context, Cursor c) {
		super(context, c);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// TODO Auto-generated method stub
		TextView messageSMS = (TextView) view.findViewById(R.id.sms_list_item);
		messageSMS.setText(cursor.getString(cursor.getColumnIndex(SMS_BODY_FIELD)));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		// TODO Auto-generated method stub
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.sms_item_view, parent, false);
		bindView(v, context, cursor);
		return v;
	}

}
