package com.meryrua.smsbanking;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.HashMap;

import com.meryrua.smsbanking.R;
import com.meryrua.smsbanking.SMSReceiver.SaveTransaction;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SMSBankingActivity extends ListActivity{
	
	private Context context;
	/*
	private Button sendSMSButton;
	private Button viewHistoryButton;
	private Button checkSMSButton;*/
	
	//private MyDBAdapter myDBAdapter;
	//private Cursor transactionCursor;
	private TransactionAdapter transactionAdapter;
	private HashMap<String, String> filterMap;
	private Resources resources;	
	protected static ArrayAdapter<String> cardAdapter;
	private ListView listView;
	private String cardAliasString;
	private String passwordString;
	private static String currentPassword = null;
	private Bundle bundle;
	private Intent viewIntent;
	private static boolean isChecked = false;
	private String balanceValue;
	
	private IntentFilter updateIntentFilter;
	private UpdateReceiver updateReceiver;
	static int numberUpdate = 0;
	private LoadTransactionData loadTask = null;
	private LoadDataFromSMS loadFromSMSTask = null;
	
	
	private static final int ID_FILTER_ACTIVITY = 1;
	private static final int IDM_PREFERENCES = 101;
	private static final int IDM_CARD_FILTER = 102;
	private static final int IDM_OPERATION_FILTER = 103;
	private static final int IDM_OPERATION_FILTER_ALL_OPERATION = 1031;
	private static final int IDM_OPERATION_FILTER_CARD_OPERATION = 1032;
	private static final int IDM_OPERATION_FILTER_INCOMING_OPERATION = 1033;
	private static final int IDM_OPERATION_FILTER_OUTGOING_OPERATION = 1034;
	private static final int IDM_TEST = 104;
	private static final int IDM_DELETE_DATA = 105;
	private static final int DIALOG_SMS_DETAIL = 0;	
	private static final int DIALOG_CARD_FILTER = 1;	
	private static final int DIALOG_CARD_DATA = 2;
	private static final int DIALOG_PASSWORD_CHECKING = 3;
	private static final int DIALOG_WRONG_PASSWORD = 4;
	private static final int DIALOG_LOADING = 5;
	private static final int DIALOG_LOAD_DATA_REQUEST = 6;
	private static final int DIALOG_FIRST_LOAD = 7;
	
	public static final String UPDATE_TRANSACTION_LIST_INTENT = "com.meryrua.smsbanking.UPDATE_TRANSACTION_LIST";
	public static final String VIEW_TRANSACTION_DETAIL_INTENT = "com.meryrua.smsbanking.VIEW_TRANSACTION_DETAIL";
	public static final String INTENT_ACTION = "intent_action";
	public static final String DEFAULT_PASSWORD = "1234";
	public static final String PASSWORD_TEXT = "password";
	
	private static final String NEED_TO_LOAD = "need_to_load";
	
	private TransactionData transactionData;
	
	protected class UpdateReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			showTransactionList();
		}
		
	}
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	    viewIntent = getIntent();
		bundle = viewIntent.getExtras();
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_history);         
	    context = getApplicationContext();
	    resources = context.getResources();
        
	    isChecked = false;
    	//Log.d("NATALIA!!! ", "111 passw " );	    
    	
	   	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
    	boolean usingPassword = settings.getBoolean(resources.getString(R.string.using_password), false);
        filterMap = new HashMap<String, String>();
        filterMap.put(TransactionData.CARD_NUMBER, settings.getString(TransactionData.CARD_NUMBER, resources.getString(R.string.all)));
        filterMap.put(TransactionData.TRANSACTION_PLACE, settings.getString(TransactionData.TRANSACTION_PLACE, resources.getString(R.string.all)));
        if (!settings.contains(NEED_TO_LOAD)){
        	SharedPreferences.Editor editorSettings = settings.edit();
    		editorSettings.putBoolean(NEED_TO_LOAD, true);
    		editorSettings.commit();
    		Log.d("NATALIA!!! ", "NEED_TO_LOAD, true");
        }
       
    	
    	//Log.d("NATALIA!!! ", "passw " + usingPassword);
    
    	if ((usingPassword) && (!isChecked)){
        	if (!settings.contains(PASSWORD_TEXT))
        	{
        		SharedPreferences.Editor editorSettings = settings.edit();
        		editorSettings.putString(PASSWORD_TEXT, DEFAULT_PASSWORD);
        		editorSettings.commit();
        		currentPassword = new String(DEFAULT_PASSWORD);
        		Log.d("NATALIA!!! ", "create PASSWORD");
        	}
        	else
        	{
        		currentPassword = settings.getString(PASSWORD_TEXT, DEFAULT_PASSWORD);
        		Log.d("NATALIA!!! ", "use previous PASSWORD");
        	}
 	    	showDialog(DIALOG_PASSWORD_CHECKING);
    	}
    	else
    	{
    		isChecked = true;
    		//prepareActivity();
    	}
	        
        try{
        backupDb();
        }
       catch (Exception e) {}
        finally{
        }
    }
    
    private void prepareActivity(){
        //transactionDatas = new ArrayList<TransactionData>();
        cardAdapter = new ArrayAdapter<String>(context, android.R.layout.select_dialog_item);
		
		listView = getListView();
		//Log.d("NATALIA!!! ", "listView ");
		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			 @Override
			    public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) {
				 return onListItemLongClick(pos, id);
			    }

		});
		listView.setClickable(true);
		//Log.d("NATALIA!!! ", "listView after");
		
		//What have I do if I've received SMS and load app in the first time?
		//It will show SMS detail info and in the same time try to load data from SMS
	   	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		if (!settings.getBoolean(NEED_TO_LOAD, true)){
			Log.d("NATALIA!!! ", "do not load data");
			showActivityData();
		}else{
			Log.d("NATALIA!!! ", "show dialog load data");
			showDialog(DIALOG_FIRST_LOAD);
		}
    	
    }
    
    protected void showActivityData(){
    	Log.d("NATALIA!!! ", "showActivityData");
		if(bundle != null){
			if (viewIntent.getAction().equals(VIEW_TRANSACTION_DETAIL_INTENT)){
				//Log.d("NATALIA!!! ", "onCreate " + viewIntent.getAction());
				transactionData = new TransactionData(bundle);
				bundle = null;	
				//showDialog(DIALOG_SMS_DETAIL);
				Intent detailIntent = new Intent();
				detailIntent.setClass(context, SMSDetail.class);
				transactionData.fillIntent(detailIntent);
				startActivity(detailIntent);

			}
		}
		showTransactionList();
    }
    
    @Override
    protected void onNewIntent(Intent intent){
    	super.onNewIntent(intent);
    	if (isChecked){
	    	//Log.d("NATALIA!!! ", "onNewIntent " + intent.getAction());
	    	if (intent.getAction().equals(UPDATE_TRANSACTION_LIST_INTENT)){
	    		//Log.d("NATALIA!!! ", "onNewIntent 111" + intent.getAction() + intent.getExtras());
	    		showTransactionList();
	    	}
	    	else
				if(intent.getAction().equals(VIEW_TRANSACTION_DETAIL_INTENT)){
					transactionData = new TransactionData(intent.getExtras());
			    	//Log.d("NATALIA!!! ", "showDialog " + intent.getAction() + intent.getExtras());
					//showDialog(DIALOG_SMS_DETAIL);
					Intent detailIntent = new Intent();
					detailIntent.setClass(context, SMSDetail.class);
					transactionData.fillIntent(detailIntent);
					startActivity(detailIntent);
				}
    	}
   	
    }
    
    private void backupDb() throws IOException {
        File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();

        if (sd.canWrite()) {

            String currentDBPath = "/data/com.meryrua.smsbanking/databases/smsbanking_base";
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
    	menu.add(Menu.NONE, IDM_PREFERENCES, Menu.NONE, resources.getString(R.string.settings));
    	menu.add(Menu.NONE, IDM_CARD_FILTER, Menu.NONE, resources.getString(R.string.card_filter));
    	//menu.add(Menu.NONE, IDM_TEST, Menu.NONE, "TEST");
    	menu.add(Menu.NONE, IDM_DELETE_DATA, Menu.NONE, resources.getString(R.string.delete_data));
    	SubMenu subMenuFilters = menu.addSubMenu(resources.getString(R.string.operation_filter));
    	subMenuFilters.add(Menu.NONE, IDM_OPERATION_FILTER_ALL_OPERATION, Menu.NONE, resources.getString(R.string.all));
    	subMenuFilters.add(Menu.NONE, IDM_OPERATION_FILTER_CARD_OPERATION, Menu.NONE, resources.getString(R.string.card_operations));
    	subMenuFilters.add(Menu.NONE, IDM_OPERATION_FILTER_INCOMING_OPERATION, Menu.NONE, resources.getString(R.string.incoming_operations));
    	subMenuFilters.add(Menu.NONE, IDM_OPERATION_FILTER_OUTGOING_OPERATION, Menu.NONE, resources.getString(R.string.outgoing_operations));
    	subMenuFilters.setIcon(android.R.drawable.ic_menu_more);
    	return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item){
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
    	SharedPreferences.Editor editor = settings.edit();
    	switch(item.getItemId()){
    	case IDM_PREFERENCES:
    		Intent startIntentProcessing = new Intent();
    		startIntentProcessing.setClass(context, Settings.class);
    		startActivity(startIntentProcessing);   
    		return true;
    	case IDM_CARD_FILTER:
    		//showDialog(DIALOG_CARD_FILTER);  
    		new LoadCardDatas().execute("");
    		return true;
       	case IDM_TEST:
       		Intent intent = new Intent();
       		intent.setClass(context, SMSViewingActivity.class);
       		startActivity(intent);
    		return true;
       	case IDM_DELETE_DATA:
       		showDialog(DIALOG_LOAD_DATA_REQUEST);
    		return true;
    	case IDM_OPERATION_FILTER_ALL_OPERATION:
    		filterMap.remove(TransactionData.TRANSACTION_PLACE);
    		filterMap.put(TransactionData.TRANSACTION_PLACE, resources.getString(R.string.all));

    	   	editor.putString(TransactionData.TRANSACTION_PLACE, resources.getString(R.string.all));
    	   	editor.commit();
    	   	
    		showTransactionList();
    		return true;
    	case IDM_OPERATION_FILTER_CARD_OPERATION:
    		filterMap.remove(TransactionData.TRANSACTION_PLACE);
    		filterMap.put(TransactionData.TRANSACTION_PLACE, resources.getString(R.string.card_operations));
    		
    	   	editor.putString(TransactionData.TRANSACTION_PLACE, resources.getString(R.string.card_operations));
    	   	editor.commit();
    	   	
    		showTransactionList();
    		return true;
    	case IDM_OPERATION_FILTER_INCOMING_OPERATION:
    		filterMap.remove(TransactionData.TRANSACTION_PLACE);
    		filterMap.put(TransactionData.TRANSACTION_PLACE, resources.getString(R.string.incoming_operations));

    		editor.putString(TransactionData.TRANSACTION_PLACE, resources.getString(R.string.incoming_operations));
    	   	editor.commit();
    	   	
    		showTransactionList();
        	return true;
    	case IDM_OPERATION_FILTER_OUTGOING_OPERATION:
    		filterMap.remove(TransactionData.TRANSACTION_PLACE);
    		filterMap.put(TransactionData.TRANSACTION_PLACE, resources.getString(R.string.outgoing_operations));
    		
    		editor.putString(TransactionData.TRANSACTION_PLACE, resources.getString(R.string.outgoing_operations));
    	   	editor.commit();
    	   	
    		showTransactionList();
    		return true;
        }

    	return false;
    }
    
    @Override
    protected void onResume(){
    	super.onResume();
	    if (isChecked){
	    	Log.d("NATALIA!!! ", "onResume ");
	    	//showTransactionList();
    	}
    }
    
    @Override
    protected void onPause(){
    	super.onPause();
    	Log.d("NATALIA!!! ", "onPause");
    }
    
    @Override
    protected void onStart(){
     	Log.d("NATALIA!!! ", "onStart");
    	super.onStart();
	    if (isChecked){
	    	Log.d("NATALIA!!! ", "onResume ");
	    	prepareActivity();
	    	//showTransactionList();
    	}
    	updateIntentFilter = new IntentFilter(UPDATE_TRANSACTION_LIST_INTENT);
    	updateIntentFilter.addCategory("android.intent.category.DEFAULT");
    	updateReceiver = new UpdateReceiver();
    	registerReceiver(updateReceiver, updateIntentFilter);
    }
    
    @Override
    protected void onStop(){
    	//super.onStop();
     	Log.d("NATALIA!!! ", "onStop");
		if ((loadTask != null))// && (loadTask.getStatus() == AsyncTask.Status.RUNNING))
		{
			Log.d("NATALIA!!!", "cancel task onStop" + loadTask);
			loadTask.cancel(false);

		}
		if((loadFromSMSTask != null))// && (loadFromSMSTask.getStatus() == AsyncTask.Status.RUNNING))
		{	Log.d("NATALIA!!!", "cancel task loadFromSMSTask" + loadFromSMSTask + (loadFromSMSTask.getStatus() == AsyncTask.Status.RUNNING));
			loadFromSMSTask.cancel(false);
			//loadFromSMSTask.cancel(true);
		}
		//transactionAdapter.getCursor().close();
		unregisterReceiver(updateReceiver);
    	super.onStop();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig){
    	if (isChecked)
    	{
    		prepareActivity();
    	}else{
    		showDialog(DIALOG_PASSWORD_CHECKING);
    	}
    	super.onConfigurationChanged(newConfig);
    }
        
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		transactionData =  new TransactionData((Cursor)getListAdapter().getItem(position));
		//showDialog(DIALOG_SMS_DETAIL);
		Intent detailIntent = new Intent();
		detailIntent.setClass(context, SMSDetail.class);
		transactionData.fillIntent(detailIntent);
		startActivity(detailIntent);
 	}
	
	protected boolean onListItemLongClick(int pos, long id){
		transactionData = new TransactionData((Cursor)getListAdapter().getItem(pos));
		new LoadCardDatas().execute(transactionData.getCardNumber());
		return true;
	}
	
	@Override
	protected Dialog onCreateDialog(int id){
		AlertDialog alertDialog;
		switch (id){
		case DIALOG_SMS_DETAIL:
			AlertDialog.Builder smsDetailDialogBuilder;
			
			LayoutInflater inflaterSMSDetail = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
			View layoutSMSDetail = inflaterSMSDetail.inflate(R.layout.sms_detail, (ViewGroup) findViewById(R.id.sms_detail_layout));
			
			smsDetailDialogBuilder = new AlertDialog.Builder(this);
			smsDetailDialogBuilder.setView(layoutSMSDetail);
			alertDialog = smsDetailDialogBuilder.create();
			//Log.d("NATALIA!!! ", "Dialog DIALOG_SMS_DETAIL create");
			break;
		case DIALOG_LOADING:
			AlertDialog.Builder loadingDialogBuilder;
			
			LayoutInflater inflaterLoading = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
			View layoutLoading = inflaterLoading.inflate(R.layout.my_progress, (ViewGroup) findViewById(R.id.my_progress_layout));
			
			loadingDialogBuilder = new AlertDialog.Builder(this);
			loadingDialogBuilder.setView(layoutLoading);
			alertDialog = loadingDialogBuilder.create();
			break;
		case DIALOG_CARD_FILTER:
			AlertDialog.Builder cardFilterBuilder = new AlertDialog.Builder(this);
			cardFilterBuilder.setAdapter(cardAdapter, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					String newSelection = cardAdapter.getItem(which);
			   		filterMap.remove(TransactionData.CARD_NUMBER);
		    		filterMap.put(TransactionData.CARD_NUMBER, newSelection);
		    		
		    	   	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		    	   	SharedPreferences.Editor editor = settings.edit();
		    	   	editor.putString(TransactionData.CARD_NUMBER, newSelection);
		    	   	editor.commit();
		    	   	
		    		showTransactionList();
		    		removeDialog(DIALOG_CARD_FILTER);
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
		case DIALOG_CARD_DATA:
			AlertDialog.Builder cardDataDialogBuilder;
			
			LayoutInflater inflaterCardData = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
			View layoutCardData = inflaterCardData.inflate(R.layout.card_data, (ViewGroup) findViewById(R.id.card_data_layout));
			
			cardDataDialogBuilder = new AlertDialog.Builder(this);
			cardDataDialogBuilder.setView(layoutCardData);
			cardDataDialogBuilder.setPositiveButton(resources.getString(R.string.save), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					if (cardAliasString != null){
						new UpdateCardAlias().execute();
					}
				}
			});
			cardDataDialogBuilder.setNegativeButton(resources.getString(R.string.close), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.cancel();
				}
			});
			cardDataDialogBuilder.setTitle(R.string.card_description);		
			alertDialog = cardDataDialogBuilder.create();
			break;
		case DIALOG_PASSWORD_CHECKING:
			AlertDialog.Builder passwordDialogBuilder;
			
			LayoutInflater inflaterPassword = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
			View layoutPassword = inflaterPassword.inflate(R.layout.password, (ViewGroup) findViewById(R.id.password_layout));
			
			passwordDialogBuilder = new AlertDialog.Builder(this);
			passwordDialogBuilder.setView(layoutPassword);
			passwordDialogBuilder.setPositiveButton(resources.getString(R.string.save), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
						passwordCheck();
				}
			});
			passwordDialogBuilder.setNegativeButton(resources.getString(R.string.close), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					finish();
				}
			});
			passwordDialogBuilder.setCancelable(false);
			passwordDialogBuilder.setTitle(R.string.input_password);
			alertDialog = passwordDialogBuilder.create();
			break;
		case DIALOG_WRONG_PASSWORD:
			AlertDialog.Builder wrongPasswordDialogBuilder;
			
			wrongPasswordDialogBuilder = new AlertDialog.Builder(this);
			wrongPasswordDialogBuilder.setMessage(resources.getString(R.string.wrong_password));
			alertDialog = wrongPasswordDialogBuilder.create();
			//Log.d("NATALIA!!! ", "Dialog DIALOG_WRONG_PASSWORD create");
			break;
		case DIALOG_LOAD_DATA_REQUEST:
			AlertDialog.Builder loadDataRequestDialog;
			
			loadDataRequestDialog = new AlertDialog.Builder(this);
			loadDataRequestDialog.setMessage(resources.getString(R.string.load_from_sms));
			loadDataRequestDialog.setPositiveButton(resources.getString(R.string.yes), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
				}
			});
			alertDialog = loadDataRequestDialog.create();
			
			break;
		case DIALOG_FIRST_LOAD:
			AlertDialog.Builder firstDataLoadDialog;
			
			firstDataLoadDialog = new AlertDialog.Builder(this);
			firstDataLoadDialog.setMessage(resources.getString(R.string.first_data_load));
			firstDataLoadDialog.setPositiveButton(resources.getString(R.string.yes), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					loadFromSMSTask = (LoadDataFromSMS) new LoadDataFromSMS().execute();
				}
			});
			firstDataLoadDialog.setNegativeButton(resources.getString(R.string.no), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.cancel();
				}
			});
			alertDialog = firstDataLoadDialog.create();
			//Log.d("NATALIA!!! ", "Dialog DIALOG_WRONG_PASSWORD create");
			break;
		default:
			alertDialog = null;
		}
		return alertDialog;
	}

	private void passwordCheck(){
		if (currentPassword.equals(passwordString)){
			isChecked = true;
			prepareActivity();
		}
		else{
			showDialog(DIALOG_WRONG_PASSWORD);
			finish();
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
		        int height = placeText.getMeasuredHeight();
		        //Log.d("NATALIA!!! ", "height " + height);

		        TextView balanceText = (TextView) dialog.findViewById(R.id.balance);
		        String balanceValue = new String(Float.toString(transactionData.getFundValue()).replace(".", ","));
		        balanceValue += transactionData.getFundCurrency();
		        //Log.d("NATALIA!!!", "balance " + balanceValue);
		        balanceText.setText(resources.getString(R.string.operation_balance) + " " + balanceValue);
				break;
			/*case DIALOG_LOADING:

				break;*/
			case DIALOG_CARD_FILTER:
				/*
				cardAdapter.clear();
				getCardsNumber(cardAdapter);
				cardAdapter.setNotifyOnChange(true);*/
				break;
			case DIALOG_CARD_DATA:
				final EditText cardAlias = (EditText) dialog.findViewById(R.id.card_alias);
				cardAlias.addTextChangedListener(new TextWatcher(){
					@Override
					public void afterTextChanged(Editable s) {
						cardAliasString = new String (cardAlias.getText().toString());
					}

					@Override
					public void beforeTextChanged(CharSequence arg0, int arg1,
							int arg2, int arg3) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void onTextChanged(CharSequence s, int start,
							int before, int count) {
						// TODO Auto-generated method stub
						
					}
				});
				cardAlias.setText(cardAliasString);
				break;
			case DIALOG_PASSWORD_CHECKING:
				TextView repeatPassword = (TextView) dialog.findViewById(R.id.repeat_password_field);
				repeatPassword.setVisibility(TextView.GONE);
				TextView repeatPasswordText = (TextView) dialog.findViewById(R.id.repeat_password_text);
				repeatPasswordText.setVisibility(TextView.GONE);

				String passwordText = new String("");
				
				final EditText passwordField = (EditText) dialog.findViewById(R.id.password_field);
				passwordField.addTextChangedListener(new TextWatcher(){
					@Override
					public void afterTextChanged(Editable s) {
						passwordString = new String (passwordField.getText().toString());
					}

					@Override
					public void beforeTextChanged(CharSequence arg0, int arg1,
							int arg2, int arg3) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void onTextChanged(CharSequence s, int start,
							int before, int count) {
						// TODO Auto-generated method stub
						
					}
				});
				passwordField.setText(passwordText);
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
	
	private void showTransactionList(){
		//Log.d("NATALIA!!! ", "thread " + (loadTask) + " context " + context);
		if ((loadTask != null))// && (loadTask.getStatus() == AsyncTask.Status.RUNNING))
		{
			Log.d("NATALIA!!!", "cancel task " + loadTask);
			loadTask.cancel(false);
			loadTask = new LoadTransactionData();
			loadTask.execute();
		}else{
			Log.d("NATALIA!!!", "load new task");
			loadTask = new LoadTransactionData();
			loadTask.execute();
		}

	}

	private void updateTransactionList(){
		TextView curBalance = (TextView) findViewById(R.id.current_balance);
		TextView filterInfo = (TextView) findViewById(R.id.filter_information);
		TextView noDataField = (TextView) findViewById(R.id.no_data_field);
		TextView noDataFieldInfo = (TextView) findViewById(R.id.no_data_field_info);

		transactionAdapter.getCursor().requery();
		
		//Log.d("NATALIA!!! ", "updateTransactionList " + transactionAdapter.getCursor().getCount());
		if (transactionAdapter.getCursor().moveToFirst()){
			listView.setVisibility(ListView.VISIBLE);
			noDataField.setVisibility(TextView.GONE);
			noDataFieldInfo.setVisibility(TextView.GONE);
			//Log.d("NATALIA!!! ", "moveToFirst ");
			if (!filterMap.get(TransactionData.CARD_NUMBER).equals(resources.getString(R.string.all))){
		        String str = new String();
		        /*str += resources.getString(R.string.operation_balance) + " " +
		        	transactionAdapter.getCursor().getFloat(transactionAdapter.getCursor().getColumnIndex(TransactionData.FUND_VALUE)) +
		        	transactionAdapter.getCursor().getString(transactionAdapter.getCursor().getColumnIndex(TransactionData.FUND_CURRENCY));*/
		        str += resources.getString(R.string.operation_balance) + " " + balanceValue;
		        curBalance.setVisibility(TextView.VISIBLE);
		        curBalance.setText(str);
		        String str1 = new String();
		        str1 += resources.getString(R.string.operation_card_number) + filterMap.get(TransactionData.CARD_NUMBER);
		        filterInfo.setVisibility(TextView.VISIBLE);
		        filterInfo.setText(str1);
			}
			else{
				curBalance.setText(null);
				curBalance.setVisibility(TextView.GONE);
				if (!filterMap.get(TransactionData.TRANSACTION_PLACE).equals(resources.getString(R.string.all))){
			        String str1 = new String();
			        str1 += filterMap.get(TransactionData.TRANSACTION_PLACE);
			        filterInfo.setVisibility(TextView.VISIBLE);
			        filterInfo.setText(filterMap.get(TransactionData.TRANSACTION_PLACE));
				}else{
					filterInfo.setText(null);
					filterInfo.setVisibility(TextView.GONE);
				}
			}
		} 
		else {
			/*
			curBalance.setVisibility(TextView.VISIBLE);
			curBalance.setText(resources.getString(R.string.no_data));
			if (!filterMap.get(TransactionData.CARD_NUMBER).equals(resources.getString(R.string.all))){
				String str1 = new String();
		        str1 += resources.getString(R.string.operation_card_number) + filterMap.get(TransactionData.CARD_NUMBER);
		        filterInfo.setVisibility(TextView.VISIBLE);
		        filterInfo.setText(str1);
			}
			else{
				filterInfo.setText(null);
				filterInfo.setVisibility(TextView.GONE);				
			}*/
			curBalance.setVisibility(TextView.GONE);
			filterInfo.setVisibility(TextView.GONE);
			listView.setVisibility(TextView.GONE);
			noDataField.setVisibility(TextView.VISIBLE);
			noDataFieldInfo.setVisibility(TextView.VISIBLE);
		}
		
		transactionAdapter.notifyDataSetChanged();
		//Log.d("NATALIA!!! ", "updateTransactionList end ");
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

	class LoadTransactionData extends AsyncTask<Void, Integer, Cursor>{
		private MyDBAdapter myAdapter;
        private ProgressDialog progressDialog  = new ProgressDialog(SMSBankingActivity.this);
		
		@Override
		protected void onPreExecute(){
			this.progressDialog.setCancelable(false);
			this.progressDialog.setMessage(resources.getText(R.string.loading));
	        this.progressDialog.show();
			//showDialog(DIALOG_LOADING);
		}
		
		@Override
		protected Cursor doInBackground(Void... params) {
			// TODO Auto-generated method stub
			String filterString = getSQLWhereFromFilter();
			myAdapter = new MyDBAdapter(context);
			Log.d("NATALIA123", "Open DB LoadTransactionData doInBackground");
			myAdapter.open();
			
			if (!filterMap.get(TransactionData.CARD_NUMBER).equals(resources.getString(R.string.all))){
				balanceValue = myAdapter.getBalance(filterMap.get(TransactionData.CARD_NUMBER));
				
			}
			//Log.d("NATALIA!!! ", "doInBackground begin " + context);
				
			Cursor cursor = myAdapter.getTransactionWithFilter(filterString);
			//Log.d("NATALIA", "filter " + filterString + " cursor " + cursor.getCount());
			startManagingCursor(cursor);
			for (int i = 0; i <= 10000000; i++);
			if (isCancelled())
			{
				Log.d("NATALIA!!! ", "LoadTransactionData isCancelled " + isCancelled());
				myAdapter.close();
			}

			Log.d("NATALIA!!! ", "doInBackground end " + isCancelled());
				
			return cursor;
		}
			
		@Override
		protected void onCancelled(){
	        if (this.progressDialog.isShowing()) {
	             this.progressDialog.dismiss();
	        }
			//dismissDialog(DIALOG_LOADING);
			if (myAdapter.isDatabaseOpen()){
				myAdapter.close();
			}
		}

		@Override
		protected void onPostExecute(Cursor cursor){
	        if (this.progressDialog.isShowing()) {
	             this.progressDialog.dismiss();
	        }
			//dismissDialog(DIALOG_LOADING);
			//if (openBD)
			{
			transactionAdapter = new TransactionAdapter(context, cursor);

			setListAdapter(transactionAdapter);
			updateTransactionList();
			myAdapter.close();
			//loadTask = null;
			}
		}

	}
		
	class LoadCardDatas extends AsyncTask<String, Void, Cursor>{
		private MyDBAdapter myDBAdapter;
		private boolean selectAllCards;

		@Override
		protected void onPreExecute(){
			cardAdapter.clear();
		}
			
		@Override
		protected Cursor doInBackground(String... params) {
			// TODO Auto-generated method stub
			myDBAdapter = new MyDBAdapter(context);
			myDBAdapter.open();
			Log.d("NATALIA123", "Open DB LoadCardDatas doInBackground");
				
			if (params[0].equals(""))
				selectAllCards = true;
			else
				selectAllCards = false;
				
			Cursor cursor = myDBAdapter.selectCardsNumber(params[0]);
			startManagingCursor(cursor);
				
			return cursor;
		}
			
		@Override
		protected void onPostExecute(Cursor cursor){
			boolean cardsExist = false;
			if (selectAllCards){
				if (cursor.moveToFirst()){
					cardsExist = true;
					cardAdapter.add(context.getResources().getString(R.string.all));
					do{
						cardAdapter.add(cursor.getString(cursor.getColumnIndex(TransactionData.CARD_NUMBER)));
					} while (cursor.moveToNext());
					cursor.close();
				}
				myDBAdapter.close();
				Log.d("NATALIA123", "Close DB LoadCardDatas onPostExecute 1");
				cardAdapter.setNotifyOnChange(true);
				if (cardsExist)
					showDialog(DIALOG_CARD_FILTER);
				else
					Toast.makeText(context, resources.getText(R.string.no_data), 500).show();
			}else{
				if (cursor.moveToFirst()){
					if(cursor.getString(cursor.getColumnIndex(MyDBAdapter.CARD_ALIAS)) != null)
						cardAliasString = new String (cursor.getString(cursor.getColumnIndex(MyDBAdapter.CARD_ALIAS)));
					else
						cardAliasString = new String("");
				}
				else
					cardAliasString = new String("");
				Log.d("NATALIA123", "Close DB LoadCardDatas onPostExecute 2");
				myDBAdapter.close();
				showDialog(DIALOG_CARD_DATA);
			}
		}
	}
		
	class UpdateCardAlias extends AsyncTask<Void, Void, Boolean>{
		private MyDBAdapter myDBAdapter;
		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO Auto-generated method stub
			myDBAdapter = new MyDBAdapter(context);
			myDBAdapter.open();
			Log.d("NATALIA123", "Open DB UpdateCardAlias doInBackground");
				
			Boolean result = myDBAdapter.updateCardAlias(cardAliasString, transactionData.getCardNumber());
			Log.d("NATALIA123", "Close DB UpdateCardAlias doInBackground");
			myDBAdapter.close();

			return result;
		}
			
		@Override
		protected void onPostExecute(Boolean result){
			if (result.booleanValue() != true)
				Toast.makeText(context, "Alias wasn't apdated!", 500).show();
		}
	}
	
	class LoadDataFromSMS extends AsyncTask<Void, Void, Boolean>{
		private MyDBAdapter myDBAdapter;
        private ProgressDialog progressDialog  = new ProgressDialog(SMSBankingActivity.this);
		
		@Override
		protected void onPreExecute(){
			this.progressDialog.setCancelable(false);
			this.progressDialog.setMessage(resources.getText(R.string.loading_sms));
	        this.progressDialog.show();
			//showDialog(DIALOG_LOADING);
		}
		
		@Override
		protected Boolean doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			
			Log.d("NATALIA!!! ", "LoadDataFromSMS doInBackground");
			Uri uriSms = Uri.parse("content://sms/inbox");
			boolean allDone = true;
			String sort_by = new String(SMSViewingAdapter.SMS_DATE_FIELD + " ASC");
			Cursor inboxSMSCursor = context.getContentResolver().query(
					uriSms, 
					new String[] { SMSViewingAdapter.SMS_ID_FIELD,
							SMSViewingAdapter.SMS_DATE_FIELD,
							SMSViewingAdapter.SMS_BODY_FIELD}, 
					null, null, sort_by);

			
			if (isCancelled()){
        		Log.d("NATALIA!!! ", "LoadDataFromSMS is cancelled in Running");
        		allDone = false;
        	}
			if ((inboxSMSCursor.moveToFirst()) && (allDone)){
			myDBAdapter = new MyDBAdapter(context);
			myDBAdapter.open();
			myDBAdapter.beginDatabaseTranzaction();
			for (int j = 0; j < 100000; j++);
			do {
					SMSParcer smsParcer = new SMSParcer(inboxSMSCursor.getString(
							inboxSMSCursor.getColumnIndex(SMSViewingAdapter.SMS_BODY_FIELD)), context);
			        if (smsParcer.isMatch(myDBAdapter)){
			        	transactionData = smsParcer.getTransactionData();
			        	if (!isCancelled()){
			        		myDBAdapter.insertTransaction(transactionData);
			        	}
			        	else{
			        		Log.d("NATALIA!!! ", "LoadDataFromSMS is cancelled in Running");
			        		allDone = false;
			        	}
			        }
				}while ((inboxSMSCursor.moveToNext()) && (allDone));
			if (allDone){
				Log.d("NATALIA!!! ", "LoadDataFromSMS success");
        		myDBAdapter.setSuccesfullTranzaction();
			}
			myDBAdapter.endDatabaseTranzaction();
			myDBAdapter.close();		        
			}

			inboxSMSCursor.close();
			Log.d("NATALIA!!! ", "LoadDataFromSMS end doInBackground allDone " + allDone);			
			return allDone;
		}
		
		@Override
		protected void onCancelled(){
			Log.d("NATALIA!!! ", "LoadDataFromSMS end onCancelled ");			
			//if (myDBAdapter.isDatabaseOpen()){
			//	myDBAdapter.close();
			//}
	        if (this.progressDialog.isShowing()) {
	             this.progressDialog.dismiss();
	        }
		}
		
		@Override
		protected void onPostExecute(Boolean result){
			if (result){//loading data without errors
				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		        SharedPreferences.Editor editorSettings = settings.edit();
		    	editorSettings.putBoolean(NEED_TO_LOAD, false);
		    	editorSettings.commit();
			}else{
				Toast.makeText(context, resources.getString(R.string.data_not_loaded), 500).show();
			}
	        if (this.progressDialog.isShowing()) {
	             this.progressDialog.dismiss();
	        }
	        showActivityData();
		}
		
	}
}