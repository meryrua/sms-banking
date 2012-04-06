package com.meryrua.smsbanking;

import com.meryrua.smsbanking.R;

import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class SMSViewingActivity extends ListActivity{
	
	private Context context;
	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		setContentView(R.layout.sms_view);
		String sort_by = new String(SMSViewingAdapter.SMS_DATE_FIELD + " DESC");
		
		context = getApplicationContext();
		Uri uriSms = Uri.parse("content://sms/inbox");
		Cursor inboxSMSCursor = context.getContentResolver().query(
				uriSms, 
				new String[] { SMSViewingAdapter.SMS_ID_FIELD,
						SMSViewingAdapter.SMS_DATE_FIELD,
						SMSViewingAdapter.SMS_BODY_FIELD}, 
				null, null, sort_by);
		
		SMSViewingAdapter adapterSMS = new SMSViewingAdapter(context, inboxSMSCursor);
		
		if (inboxSMSCursor.moveToFirst()){
			do{
				String str = inboxSMSCursor.getString(inboxSMSCursor.getColumnIndex(SMSViewingAdapter.SMS_BODY_FIELD));
				Log.d("NATALIA!!! ", "sms " + str);
			}while (inboxSMSCursor.moveToNext());
		}
		setListAdapter(adapterSMS);
		
		TextView chooseText = (TextView) findViewById(R.id.choose_text);
		int j = chooseText.getBottom();
		
	}

}