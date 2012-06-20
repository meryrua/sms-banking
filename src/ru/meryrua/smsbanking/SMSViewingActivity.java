package ru.meryrua.smsbanking;

import ru.meryrua.smsbanking.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SMSViewingActivity extends ListActivity {
    private static final String LOG_TAG = "com.meryrua.smsbanking:SMSViewingActivity";
	private Context context;
	private Resources resources;
	    
	private String messageString;
	private static final int DIALOG_PATTERN_TYPE = 0;
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.sms_view);
		String sort_by = new String(SMSViewingAdapter.SMS_DATE_FIELD + " DESC");
		
		context = getApplicationContext();
	    resources = context.getResources();
		Uri uriSms = Uri.parse("content://sms/inbox");
		Cursor inboxSMSCursor = context.getContentResolver().query(
				uriSms, 
				new String[] { SMSViewingAdapter.SMS_ID_FIELD,
						SMSViewingAdapter.SMS_DATE_FIELD,
						SMSViewingAdapter.SMS_BODY_FIELD}, 
				null, null, sort_by);
		
		SMSViewingAdapter adapterSMS = new SMSViewingAdapter(context, inboxSMSCursor);
		
		if (inboxSMSCursor.moveToFirst()) {
			do {
				//String str = inboxSMSCursor.getString(inboxSMSCursor.getColumnIndex(SMSViewingAdapter.SMS_BODY_FIELD));
			} while (inboxSMSCursor.moveToNext());
		}
		setListAdapter(adapterSMS);
	}

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        DebugLogging.log(getApplicationContext(), (LOG_TAG + " onListItemClick " + id));
        v.setPressed(true);
        Cursor cursor = (Cursor) l.getAdapter().getItem(position);
        messageString = new String(cursor.getString(cursor.getColumnIndex(SMSViewingAdapter.SMS_BODY_FIELD)));
        DebugLogging.log(getApplicationContext(), (LOG_TAG + " onListItemClick " + messageString));
        showDialog(DIALOG_PATTERN_TYPE);
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog alertDialog;
        switch (id) {
        case DIALOG_PATTERN_TYPE:
            alertDialog = patternTypeDialogBuilder();
            break;
        default:
            alertDialog = null;
            break;
        }
        return alertDialog;
    }

    private AlertDialog patternTypeDialogBuilder() {
        AlertDialog.Builder patternTypeDialogBuilder = new AlertDialog.Builder(this);
        final ArrayAdapter<String> operationsArray = new ArrayAdapter<String>(context, 
                android.R.layout.select_dialog_singlechoice, 
                new String []{resources.getString(R.string.card_operations), 
                resources.getString(R.string.incoming_operations),
                resources.getString(R.string.outgoing_operations)});
   
        patternTypeDialogBuilder.setTitle(resources.getString(R.string.operation_filter));
            
        patternTypeDialogBuilder.setNegativeButton(resources.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();                
            }
        });
        
        patternTypeDialogBuilder.setItems(new String []{resources.getString(R.string.card_operations), 
                resources.getString(R.string.incoming_operations),
                resources.getString(R.string.outgoing_operations)}, new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedItem = operationsArray.getItem(which);
                Intent startEditPattern = new Intent(context, PatternEditActivity.class);
                startEditPattern.putExtra(TransactionData.OPERATION_NAME, selectedItem);
                startEditPattern.putExtra(PatternEditActivity.MESSAGE_STRING, messageString);
                startActivity(startEditPattern);
            }
        });

        /*patternTypeDialogBuilder.setAdapter(operationsArray, new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedItem = operationsArray.getItem(which);
                Intent startEditPattern = new Intent(context, PatternEditActivity.class);
                startEditPattern.putExtra(TransactionData.OPERATION_NAME, selectedItem);
                startEditPattern.putExtra(PatternEditActivity.MESSAGE_STRING, messageString);
                startActivity(startEditPattern);
            }
        });*/
            
        return patternTypeDialogBuilder.create();

    }

}
