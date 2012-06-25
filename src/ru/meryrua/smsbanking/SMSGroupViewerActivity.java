package ru.meryrua.smsbanking;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SMSGroupViewerActivity extends ListActivity {
    private static final String LOG_TAG = "com.meryrua.smsbanking:SMSGroupViewerActivity";
    private Context context;
    
    private ArrayList<SMSGroup> smsGroup;
    
    
    private class SMSGroupViewerAdapter extends ArrayAdapter<SMSGroup> {
        private ArrayList<SMSGroup> smsGroupAdapter;
        private Context context;
        private int textViewId;

        public SMSGroupViewerAdapter(
                Context context,
                int textViewResourceId,
                ArrayList<SMSGroup> objects) {
            super(context, textViewResourceId, objects);
            this.context = context;
            smsGroupAdapter = objects;
            textViewId = textViewResourceId;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(textViewId, null);
            }

            SMSGroup item = smsGroupAdapter.get(position);
            if (item!= null) {
                TextView itemAddress = (TextView) view.findViewById(R.id.sms_address_item);
                itemAddress.setText(item.getAddress());
                TextView itemMessage = (TextView) view.findViewById(R.id.sms_message_item);
                itemMessage.setText(item.getMessage());
             }

            return view;
        }
    }
    
    public static class SMSGroup {
        private String address;
        private String message; 
        
        SMSGroup(String address, String message) {
            this.address = address;
            this.message = message;
        }
        
        String getAddress() {
            return address;
        }
        
        String getMessage() {
            return message;
        }
    }
    
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.sms_view);
        String sort_by = new String(SMSViewingAdapter.SMS_ADDRESS_FIELD + " ASC");
        
        context = getApplicationContext();
        //resources = context.getResources();
        Uri uriSms = Uri.parse("content://sms/inbox");
        Cursor inboxSMSCursor = context.getContentResolver().query(uriSms, 
                new String[] {SMSViewingAdapter.SMS_ADDRESS_FIELD, SMSViewingAdapter.SMS_DATE_FIELD,
                            SMSViewingAdapter.SMS_BODY_FIELD},
                null, null, sort_by);
        
        smsGroup = new ArrayList<SMSGroup>();
        
        getSMSGroupList(inboxSMSCursor);

        SMSGroupViewerAdapter adapterSMSGroup = new SMSGroupViewerAdapter(context, R.layout.sms_group_item_view, smsGroup);
        
        setListAdapter(adapterSMSGroup);
    }

    private void getSMSGroupList(Cursor inboxSMSCursor) {
        String newAddress;
        String oldAddress;
        if (inboxSMSCursor != null) {
            if (inboxSMSCursor.moveToFirst()) {
                newAddress = inboxSMSCursor.getString(inboxSMSCursor.getColumnIndex(SMSViewingAdapter.SMS_ADDRESS_FIELD));
                oldAddress = newAddress.toString();
                smsGroup.add(new SMSGroup (newAddress, 
                        inboxSMSCursor.getString(inboxSMSCursor.getColumnIndex(SMSViewingAdapter.SMS_BODY_FIELD))));
                while (inboxSMSCursor.moveToNext()) {
                    newAddress = inboxSMSCursor.getString(inboxSMSCursor.getColumnIndex(SMSViewingAdapter.SMS_ADDRESS_FIELD));
                    if (!newAddress.equals(oldAddress)) {
                        smsGroup.add(new SMSGroup (newAddress, 
                                inboxSMSCursor.getString(inboxSMSCursor.getColumnIndex(SMSViewingAdapter.SMS_BODY_FIELD))));
                        oldAddress = newAddress.toString();
                    }
                }
            }
        }
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        v.setPressed(true);
        SMSGroup item = (SMSGroup) l.getAdapter().getItem(position);
        
        Intent startSMSOneGroup = new Intent(context, SMSViewingActivity.class);
        startSMSOneGroup.putExtra(SMSViewingActivity.ADDRESS_FILTER, item.getAddress());
        startActivity(startSMSOneGroup);
    }
    
}
