package com.android.smsbanking;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;


public class SMSBankingActivity extends ListActivity{
	
	private Context context;
	/*
	private Button sendSMSButton;
	private Button viewHistoryButton;
	private Button checkSMSButton;*/
	
	private TextView curBalance;

	private MyDBAdapter myDBAdapter;
	private Cursor transactionCursor;
	private ArrayList<TransactionData> transactionDatas;
	private TransactionAdapter transactionAdapter;
	private String filter;
	private HashMap<String, String> filterMap;
	private Resources resources;	
	private static ArrayAdapter<String> cardAdapter;
	
	private static final int ID_FILTER_ACTIVITY = 1;
	private static final int IDM_PREFERENCES = 101;
	private static final int IDM_CARD_FILTER = 102;
	private static final int IDM_OPERATION_FILTER = 103;
	private static final int IDM_OPERATION_FILTER_ALL_OPERATION = 1031;
	private static final int IDM_OPERATION_FILTER_CARD_OPERATION = 1032;
	private static final int IDM_OPERATION_FILTER_INCOMING_OPERATION = 1033;
	private static final int IDM_OPERATION_FILTER_OUTGOING_OPERATION = 1034;
	private static final int DIALOG_SMS_DETAIL = 0;	
	private static final int DIALOG_CARD_FILTER = 1;		
	
	public static final String VIEW_TRANSACTION_LIST_INTENT = "com.android.smsbanking.VIEW_TRANSACTION_LIST";
	
	private TransactionData transactionData;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_history);
        
        context = getApplicationContext();
        
        resources = context.getResources();
        
        filterMap = new HashMap<String, String>();
        filterMap.put(TransactionData.CARD_NUMBER, resources.getString(R.string.all));
        filterMap.put(TransactionData.TRANSACTION_PLACE, resources.getString(R.string.all));
        
        curBalance = (TextView) findViewById(R.id.current_balance);

        transactionDatas = new ArrayList<TransactionData>();
		int resId = R.layout.list_item;
		transactionAdapter = new TransactionAdapter(context, resId, transactionDatas);
		setListAdapter(transactionAdapter);
		
		Intent viewIntent = getIntent();
		Log.d("NATALIA!!! ", "onNewIntent " + viewIntent.getAction());
		if(viewIntent.getExtras() != null){
			transactionData = new TransactionData(viewIntent.getExtras());
			showDialog(DIALOG_SMS_DETAIL);
		}
		
		showTransactionList();
		

        
       // try{
        //backupDb();
        //}
       // /catch (Exception e) {}
        //finally{
       // 	Log.d("NATALIA!!!", "IOException");
        //}
    }
    
    @Override
    protected void onNewIntent(Intent intent){
    	super.onNewIntent(intent);
    	Log.d("NATALIA!!! ", "onNewIntent " + intent.getAction() + intent.getExtras());
		if(intent.getExtras() != null){
			transactionData = new TransactionData(intent.getExtras());
	    	Log.d("NATALIA!!! ", "showDialog " + intent.getAction() + intent.getExtras());
			showDialog(DIALOG_SMS_DETAIL);
		}
   	
    }
    
    private void backupDb() throws IOException {
        File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();

        if (sd.canWrite()) {

            String currentDBPath = "/data/com.android.smsbanking/databases/smsbanking_base";
            String backupDBPath = "/temp/smsbanking_base";

            File currentDB = new File(data, currentDBPath);
            File backupDB = new File(sd, backupDBPath);

            if (backupDB.exists())
                backupDB.delete();

            if (currentDB.exists()) {
                makeLogsFolder();

                copy(currentDB, backupDB);
           }

            //dbFilePath = backupDB.getAbsolutePath();
       }
    }
    
    private void copy(File from, File to) throws FileNotFoundException, IOException {
        FileChannel src = null;
        FileChannel dst = null;
        try {
            src = new FileInputStream(from).getChannel();
            dst = new FileOutputStream(to).getChannel();
            dst.transferFrom(src, 0, src.size());
        }
        finally {
            if (src != null)
                src.close();
            if (dst != null)
                dst.close();
        }
    }

    private void makeLogsFolder() {
       try {
           File sdFolder = new File(Environment.getExternalStorageDirectory(), "/temp/");
           sdFolder.mkdirs();
       }
       catch (Exception e) {}
     }
    

    public boolean onCreateOptionsMenu(Menu menu){
    	menu.add(Menu.NONE, IDM_PREFERENCES, Menu.NONE, resources.getString(R.string.sms_process));
    	menu.add(Menu.NONE, IDM_CARD_FILTER, Menu.NONE, resources.getString(R.string.card_filter));
    	SubMenu subMenuFilters = menu.addSubMenu(resources.getString(R.string.operation_filter));
    	subMenuFilters.add(Menu.NONE, IDM_OPERATION_FILTER_ALL_OPERATION, Menu.NONE, resources.getString(R.string.all));
    	subMenuFilters.add(Menu.NONE, IDM_OPERATION_FILTER_CARD_OPERATION, Menu.NONE, resources.getString(R.string.card_operations));
    	subMenuFilters.add(Menu.NONE, IDM_OPERATION_FILTER_INCOMING_OPERATION, Menu.NONE, resources.getString(R.string.incoming_operations));
    	subMenuFilters.add(Menu.NONE, IDM_OPERATION_FILTER_OUTGOING_OPERATION, Menu.NONE, resources.getString(R.string.outgoing_operations));
    	subMenuFilters.setIcon(android.R.drawable.ic_menu_more);
    	return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item){
    	switch(item.getItemId()){
    	case IDM_PREFERENCES:
    		Intent startIntentProcessing = new Intent();
    		startIntentProcessing.setClass(context, Settings.class);
    		startActivity(startIntentProcessing);   
    		return true;
    	case IDM_CARD_FILTER:
    		showDialog(DIALOG_CARD_FILTER);  
    		return true;
    	case IDM_OPERATION_FILTER_ALL_OPERATION:
    		filterMap.remove(TransactionData.TRANSACTION_PLACE);
    		filterMap.put(TransactionData.TRANSACTION_PLACE, resources.getString(R.string.all));
    		showTransactionList();
    		return true;
    	case IDM_OPERATION_FILTER_CARD_OPERATION:
    		filterMap.remove(TransactionData.TRANSACTION_PLACE);
    		filterMap.put(TransactionData.TRANSACTION_PLACE, resources.getString(R.string.card_operations));
    		showTransactionList();
    		return true;
    	case IDM_OPERATION_FILTER_INCOMING_OPERATION:
    		filterMap.remove(TransactionData.TRANSACTION_PLACE);
    		filterMap.put(TransactionData.TRANSACTION_PLACE, resources.getString(R.string.incoming_operations));
    		showTransactionList();
        	return true;
    	case IDM_OPERATION_FILTER_OUTGOING_OPERATION:
    		filterMap.remove(TransactionData.TRANSACTION_PLACE);
    		filterMap.put(TransactionData.TRANSACTION_PLACE, resources.getString(R.string.outgoing_operations));
    		showTransactionList();
    		return true;
        }

    	return false;
    }
    
    @Override
    public void onPause(){
    	super.onPause();
    	Log.d("NATALIA!!! ", "onPause ");
		if((transactionCursor != null) && (!transactionCursor.isClosed())){
			transactionCursor.close();
		}
		myDBAdapter.close();
    }
    
    @Override
    public void onResume(){
    	super.onResume();
		/*Intent viewIntent = getIntent();
		if ((viewIntent.getAction().equals(Intent.ACTION_MAIN)) && (viewIntent.getExtras() != null)){
			transactionData = new TransactionData(viewIntent.getExtras());
			showDialog(DIALOG_SMS_DETAIL);
		}*/		
    	showTransactionList();
    }
    
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		transactionData = (TransactionData) getListAdapter().getItem(position);
		  
		showDialog(DIALOG_SMS_DETAIL);

		Log.d("NATALIA!!! ", "After Dialog");
 	}
	
	@Override
	protected Dialog onCreateDialog(int id){
		AlertDialog alertDialog;
		switch (id){
		case DIALOG_SMS_DETAIL:
			AlertDialog.Builder smsDetailDialogBuilder;
			
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.sms_detail, (ViewGroup) findViewById(R.id.sms_detail_layout));
			
			smsDetailDialogBuilder = new AlertDialog.Builder(this);
			smsDetailDialogBuilder.setView(layout);
			alertDialog = smsDetailDialogBuilder.create();
			Log.d("NATALIA!!! ", "Dialog create");
			break;
		case DIALOG_CARD_FILTER:
			cardAdapter = new ArrayAdapter<String>(context, android.R.layout.select_dialog_item);
			int selectedCard = getCardsNumber(cardAdapter);
			AlertDialog.Builder cardFilterBuilder = new AlertDialog.Builder(this);
			//setAdapter do not allow set default value
			cardFilterBuilder.setAdapter(cardAdapter, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					String newSelection = cardAdapter.getItem(which);
			   		filterMap.remove(TransactionData.CARD_NUMBER);
		    		filterMap.put(TransactionData.CARD_NUMBER, newSelection);
		    		showTransactionList();
				}
			});
			//for this style of Adapter should be android.R.layout.select_dialog_singlechoice
			/*cardFilterBuilder.setSingleChoiceItems(cardAdapter, selectedCard, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					String newSelection = cardAdapter.getItem(which);
			   		filterMap.remove(TransactionData.CARD_NUMBER);
		    		filterMap.put(TransactionData.CARD_NUMBER, newSelection);
		    		showTransactionList();
				}
			});*/
			alertDialog = cardFilterBuilder.create();
			break;
		default:
			alertDialog = null;
		}
		return alertDialog;
	}

	private int getCardsNumber(ArrayAdapter<String> adapter){
		int i = 0;
		int j = 0;
		myDBAdapter = new MyDBAdapter(context);
		myDBAdapter.open();

		startManagingCursor(transactionCursor);
		transactionCursor = myDBAdapter.getCardsNumber();
		
		if (transactionCursor.moveToFirst()){
			adapter.add(context.getResources().getString(R.string.all));
			j++;
			do{
				if (filterMap.get(TransactionData.CARD_NUMBER) != null){
					if (filterMap.get(TransactionData.CARD_NUMBER).equals(transactionCursor.getString(transactionCursor.getColumnIndex(TransactionData.CARD_NUMBER))))
						i = j;
				}
				adapter.add(transactionCursor.getString(transactionCursor.getColumnIndex(TransactionData.CARD_NUMBER)));
				j++;
			} while (transactionCursor.moveToNext());
			transactionCursor.close();
			myDBAdapter.close();
			return i;
		}
		else {
			myDBAdapter.close();
			return 0;
		}

	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog){
		switch (id){
			case DIALOG_SMS_DETAIL:
			
				TextView cardNumberText = (TextView) dialog.findViewById(R.id.card_number);
		        cardNumberText.setText(resources.getString(R.string.operation_card_number) + transactionData.getCardNumber());
		        
		        TextView dateText = (TextView) dialog.findViewById(R.id.date);
		        dateText.setText(resources.getString(R.string.operation_date) + " " + transactionData.getTransactionDate());

		        TextView amountText = (TextView) dialog.findViewById(R.id.amount);
		        String tranzValue = new String(Float.toString(transactionData.getTransactionValue()).replace(".", ","));
		        tranzValue += transactionData.getTransactionCurrency();
		        amountText.setText(resources.getString(R.string.operation_amount) + " " + tranzValue);
		        
		        TextView placeText = (TextView) dialog.findViewById(R.id.place);
		        String placeOrOperation = transactionData.getTransactionPlace();
		        if (placeOrOperation.equals(TransactionData.INCOMING_BANK_OPERATION)){
		        	placeText.setText(resources.getString(R.string.operation_name) + " " + resources.getString(R.string.operation_incoming_name));
		        }else if (placeOrOperation.equals(TransactionData.OUTGOING_BANK_OPERATION)){
		        	placeText.setText(resources.getString(R.string.operation_name) + " " + resources.getString(R.string.operation_outgoing_name));
		        }else {
		        	placeText.setText(resources.getString(R.string.operation_place) + " " + transactionData.getTransactionPlace());
		        }

		        TextView balanceText = (TextView) dialog.findViewById(R.id.balance);
		        String balanceValue = new String(Float.toString(transactionData.getFundValue()).replace(".", ","));
		        balanceValue += transactionData.getFundCurrency();
		        balanceText.setText(resources.getString(R.string.operation_balance) + " " + balanceValue);
				break;
			default:
		}
	}
	
	private String getSQLWhereFromFilter(){
		String sqlString = new String();
		if (!filterMap.get(TransactionData.CARD_NUMBER).equals(resources.getString(R.string.all))){
			sqlString += TransactionData.CARD_NUMBER + "='" + filterMap.get(TransactionData.CARD_NUMBER) + "'";
		}
		if (!filterMap.get(TransactionData.TRANSACTION_PLACE).equals(resources.getString(R.string.all))){
			if (sqlString.length() != 0)
				sqlString += " AND ";
			
	    	if (filterMap.get(TransactionData.TRANSACTION_PLACE).equals(resources.getString(R.string.card_operations))){
	    		sqlString += "(" + TransactionData.TRANSACTION_PLACE + "<>'" + TransactionData.INCOMING_BANK_OPERATION + "') AND (" + TransactionData.TRANSACTION_PLACE + "<>'" + TransactionData.OUTGOING_BANK_OPERATION + "')";
	    	} else if (filterMap.get(TransactionData.TRANSACTION_PLACE).equals(resources.getString(R.string.incoming_operations))) {
	    		sqlString += TransactionData.TRANSACTION_PLACE + "='" + TransactionData.INCOMING_BANK_OPERATION + "'";
 	    	} else {
	    		sqlString += TransactionData.TRANSACTION_PLACE + "='" + TransactionData.OUTGOING_BANK_OPERATION + "'";
 	    	}
		}
		return sqlString;
	}
	
	//We have to close connection to DB
	private void showTransactionList(){
		myDBAdapter = new MyDBAdapter(context);
		myDBAdapter.open();

		String filterString = getSQLWhereFromFilter();
		transactionCursor = myDBAdapter.getTransactionWithFilter(filterString);
		startManagingCursor(transactionCursor);
		updateTransactionList();
		if((transactionCursor != null) && (!transactionCursor.isClosed())){
			transactionCursor.close();
		}
		try{
            backupDb();
            }
            catch (Exception e) {}
            finally{
            	
            }
        myDBAdapter.close();
    
	}

	private void updateTransactionList(){
		transactionCursor.requery();
		
		int i = 0;
		
		transactionDatas.clear();
		
		if (transactionCursor.moveToFirst()){
			do {
				TransactionData transactionData = myDBAdapter.getTransactionFromCursor(transactionCursor);
				transactionDatas.add(0, transactionData);
				i++;
			} while (transactionCursor.moveToNext());
			if (!filterMap.get(TransactionData.CARD_NUMBER).equals(resources.getString(R.string.all))){
		        String str = new String();
		        str += resources.getString(R.string.operation_balance) + " " + transactionDatas.get(0).getFundValue() + transactionDatas.get(0).getFundCurrency();
		        curBalance.setVisibility(TextView.VISIBLE);
		        curBalance.setText(str);
			}
			else{
				curBalance.setText(null);
				curBalance.setVisibility(TextView.GONE);
			}
		} 
		else {
			curBalance.setVisibility(TextView.VISIBLE);
			curBalance.setText(resources.getString(R.string.no_data));
		}
		
		transactionAdapter.notifyDataSetChanged();
	}

	  @Override
	  public void onDestroy() {
	    super.onDestroy();
	      
	    // Close the database
		if((transactionCursor != null) && (!transactionCursor.isClosed())){
			transactionCursor.close();
		}
	    myDBAdapter.close();
	  }

	    @Override
	    protected void onActivityResult(int requestCode, int resultCode, Intent data){
	    	if (requestCode == ID_FILTER_ACTIVITY){
	    		if (data != null){
	    			filterMap.clear();
	    			filterMap.put(TransactionData.CARD_NUMBER, data.getStringExtra(TransactionData.CARD_NUMBER));
	    			filterMap.put(TransactionData.TRANSACTION_PLACE, data.getStringExtra(TransactionData.TRANSACTION_PLACE));
	    			showTransactionList();
	    		}
	    	}
	    }
		public class MyOnOperationSelectedListener implements OnItemSelectedListener {

		    public void onItemSelected(AdapterView<?> parent,
		        View view, int pos, long id) {
		    	/*filterMap.remove(TransactionData.TRANSACTION_PLACE);
		    	if (pos == 0){
		    		filterMap.put(TransactionData.TRANSACTION_PLACE, resources.getString(R.string.all));
		    	} else if (pos == 1){
		    		filterMap.put(TransactionData.TRANSACTION_PLACE, resources.getString(R.string.card_operations));
		    	} else if (pos == 2) {
		    		filterMap.put(TransactionData.TRANSACTION_PLACE, resources.getString(R.string.incoming_operations));
	 	    	} else {
		    		filterMap.put(TransactionData.TRANSACTION_PLACE, resources.getString(R.string.outgoing_operations));
	 	    	}*/
		    }

		    public void onNothingSelected(AdapterView parent) {
		      // Do nothing.
		    }
		}

}