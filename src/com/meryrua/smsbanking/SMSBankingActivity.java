package com.meryrua.smsbanking;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.HashMap;

import com.meryrua.smsbanking.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.ComponentName;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SMSBankingActivity extends ListActivity {
	private Context context;
	private TransactionAdapter transactionAdapter;
	private HashMap<String, String> filterMap;
	private Resources resources;	
	private static ArrayAdapter<String> cardAdapter;
	private ListView listView;
	private String cardAliasString;
	private String passwordString;
	private static String currentPassword = null;
	private Bundle bundle;
	private Intent viewIntent;
	private static boolean isChecked = false;
	
	private IntentFilter updateIntentFilter;
	private UpdateReceiver updateReceiver;
	static int numberUpdate = 0;
	private boolean firstLoading = false;
	private boolean needToDeleteCard = false;
	private String cardForDelete;
	
	private static final int IDM_PREFERENCES = 101;
	private static final int IDM_CARD_FILTER = 102;
	private static final int IDM_OPERATIONS_FULTER = 103;
	private static final int IDM_OPERATION_FILTER_ALL_OPERATION = 1031;
	private static final int IDM_OPERATION_FILTER_CARD_OPERATION = 1032;
	private static final int IDM_OPERATION_FILTER_INCOMING_OPERATION = 1033;
	private static final int IDM_OPERATION_FILTER_OUTGOING_OPERATION = 1034;
	private static final int IDM_TEST = 104;
	private static final int IDM_DELETE_DATA = 105;
	private static final int IDM_DELETE_CARD = 106;
	private static final int DIALOG_SMS_DETAIL = 0;	
	private static final int DIALOG_CARD_FILTER = 1;	
	private static final int DIALOG_CARD_DATA = 2;
	private static final int DIALOG_PASSWORD_CHECKING = 3;
	private static final int DIALOG_WRONG_PASSWORD = 4;
	private static final int DIALOG_LOADING = 5;
	private static final int DIALOG_LOAD_DATA_REQUEST = 6;
	private static final int DIALOG_NEED_TO_LOAD = 7;
	private static final int DIALOG_DELETE_CARD_DATA = 8;
	private static final int DIALOG_SQLITE_ERROR_HAPPENED = 9;
	private static final int DIALOG_OPERATIONS_FILTER = 10;
	
	public static final String UPDATE_TRANSACTION_LIST_INTENT = "com.meryrua.smsbanking.UPDATE_TRANSACTION_LIST";
	public static final String VIEW_TRANSACTION_DETAIL_INTENT = "com.meryrua.smsbanking.VIEW_TRANSACTION_DETAIL";
	public static final String INTENT_ACTION = "intent_action";
	public static final String DEFAULT_PASSWORD = "1234";
	public static final String PASSWORD_TEXT = "password";
	
	public static final String OPERATION_PATTERN_NUMBER = "operation_pattern_number";
	
	private static final String NEED_TO_LOAD = "need_to_load";
	private static final String LOG_TAG = "SMSBankingActivity";
	
	private ProgressDialog progressDialog;
	private TransactionData transactionData;
	private SMSBankingActivityHandler thisActivityHandler;
	private DatabaseConnectionService connectionService;
	private boolean serviceThreadIsReady = false;
	DatabaseConnectionCallback databaseConnectionCallback = new DatabaseConnectionCallback();

	private ServiceConnection serviceConnection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName className) {
			connectionService = null;
		}

		@Override
		public void onServiceConnected(ComponentName className, IBinder iBinder) {
			connectionService = ((DatabaseConnectionService.MyBinder) iBinder).getService();
			connectionService.setCallbackItem(databaseConnectionCallback);
			DebugLogging.log(context, LOG_TAG + "onServiceConnected");
		}
	};
	
	private class SMSBankingActivityHandler extends Handler {
		static final int SHOW_ALL_CARDS_DATA = 101;
		static final int SHOW_ONE_CARD_DATA = 102;
		static final int NO_ERROR = 103;
		static final int ERROR_HAPPENED = 104;
		static final int SHOW_TRANSACTION_DATA = 1;
		static final int SHOW_CARD_DATA = 2;
		static final int ALIAS_WAS_UPDATED = 3;
		static final int DATA_FROM_SMS_WAS_LOADED = 4;
		static final int DATA_WAS_DELETED = 5;
		static final int SET_BALANCE = 8;
		static final int TREAD_IS_READY = 9;
		static final int CARD_WAS_DELETED = 10;
		static final int SQLITE_EXCEPTION = 11;
		
		public SMSBankingActivityHandler() {
			super();
		}
		
		@Override
		public void handleMessage(Message msg) {
	        DebugLogging.log(getApplicationContext(), (LOG_TAG + " handleMessage " + msg.what));
	    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
	    	SharedPreferences.Editor editor = settings.edit();
			switch(msg.what){
			case SHOW_TRANSACTION_DATA: {
	            if ((progressDialog != null) && (progressDialog.isShowing())) {
	                progressDialog.dismiss();
	            }
				Cursor transactionCursor = (Cursor) msg.obj;
				startManagingCursor(transactionCursor);
				transactionAdapter = new TransactionAdapter(context, transactionCursor);
				setListAdapter(transactionAdapter);
				transactionAdapter.notifyDataSetChanged();
				updateTransactionList();
				break;
			}
			case SET_BALANCE: {
				if (msg.obj != null) {
					setBalance(msg.obj.toString());
				}
				break;
			}
			case SHOW_CARD_DATA: {
				boolean cardsExist = false;
				Cursor cardCursor = (Cursor) msg.obj;
				startManagingCursor(cardCursor);
				if (cardCursor != null) {
					if (msg.arg1 == SHOW_ALL_CARDS_DATA) {
						if (cardCursor.moveToFirst()) {
							cardAdapter.clear();
							cardsExist = true;
							cardAdapter.add(context.getResources().getString(R.string.all));
							do {
								cardAdapter.add(cardCursor.getString(cardCursor.getColumnIndex(TransactionData.CARD_NUMBER)));
							} while (cardCursor.moveToNext());
							cardCursor.close();
						}
						cardAdapter.setNotifyOnChange(true);
						if (cardsExist) {
							showDialog(DIALOG_CARD_FILTER);
						} else {
							Toast.makeText(context, resources.getText(R.string.no_data), 500).show();
						}
					} else {
						if (cardCursor.moveToFirst()) {
							if(cardCursor.getString(cardCursor.getColumnIndex(MyDBAdapter.CARD_ALIAS)) != null) {
								cardAliasString = new String (cardCursor.getString(cardCursor.getColumnIndex(MyDBAdapter.CARD_ALIAS)));
							} else {
								cardAliasString = new String("");
							}
						} else {
							cardAliasString = new String("");
						}
						if (needToDeleteCard) {
                            showDialog(DIALOG_DELETE_CARD_DATA);						    
						} else {
						    showDialog(DIALOG_CARD_DATA);
						}
					}
				} else {
					Toast.makeText(context, resources.getText(R.string.no_data), 500).show();
				}
				break;
			}
			case ALIAS_WAS_UPDATED:
				if (msg.arg1 == ERROR_HAPPENED) {
				    Toast.makeText(context, resources.getText(R.string.error_happened), 500).show();
				}
				break;
			case DATA_FROM_SMS_WAS_LOADED:
	            if ((progressDialog != null) && (progressDialog.isShowing())) {
	                progressDialog.dismiss();
	            }
				if (msg.arg1 == NO_ERROR) {
			    	editor.putBoolean(NEED_TO_LOAD, false);
			    	editor.commit();
				} else {
			    	editor.putBoolean(NEED_TO_LOAD, true);
			    	editor.commit();
				}		
				showTransactionList();
				break;
			case DATA_WAS_DELETED:
				if (msg.arg1 == ERROR_HAPPENED ){
					Toast.makeText(context, resources.getText(R.string.error_happened), 500).show();
				} else {
			   		filterMap.remove(TransactionData.CARD_NUMBER);
		    		filterMap.put(TransactionData.CARD_NUMBER, context.getResources().getString(R.string.all));
		    		filterMap.remove(TransactionData.TRANSACTION_PLACE);
		    		filterMap.put(TransactionData.TRANSACTION_PLACE, resources.getString(R.string.all));

		    	   	editor.putString(TransactionData.CARD_NUMBER, context.getResources().getString(R.string.all));
		    	   	editor.putString(TransactionData.TRANSACTION_PLACE, resources.getString(R.string.all));
		    	   	editor.commit();
				}
				showTransactionList();
				break;
			case TREAD_IS_READY:
				serviceThreadIsReady = true;
				if ((progressDialog != null) && (progressDialog.isShowing())) {
					progressDialog.dismiss();
				}
				showTransactionList();
				break;
			case CARD_WAS_DELETED:
			    showTransactionList();
			    break;
			case SQLITE_EXCEPTION:
			    showDialog(DIALOG_SQLITE_ERROR_HAPPENED);
			    break;
			}
		}
	}
	
	private class UpdateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			showTransactionList();
		}
	}
	
	private class DatabaseConnectionCallback implements DatabaseConnectionCallbackInterface {

		public DatabaseConnectionCallback () {}

		@Override
		public void showTransactionData(Cursor cursor) {
		    DebugLogging.log(getApplicationContext(), (LOG_TAG + " showTransactionData"));
			Message msg = thisActivityHandler.obtainMessage();
			msg.what = SMSBankingActivityHandler.SHOW_TRANSACTION_DATA;
			msg.obj = cursor;
			thisActivityHandler.sendMessage(msg);
		}
		
		@Override
		public void showCardsData(Cursor cursor, String cardNumber) {
		    cardForDelete = cardNumber;
			Message msg = thisActivityHandler.obtainMessage();
			msg.what = SMSBankingActivityHandler.SHOW_CARD_DATA;
			if (cardNumber.equals("")) {
				msg.arg1 = SMSBankingActivityHandler.SHOW_ALL_CARDS_DATA;
			} else {
				msg.arg1 = SMSBankingActivityHandler.SHOW_ONE_CARD_DATA;
			}
			msg.obj = cursor;
			thisActivityHandler.sendMessage(msg);
		}

		@Override
		public void aliasUpdated(boolean result) {
			Message msg = thisActivityHandler.obtainMessage();
			msg.what = SMSBankingActivityHandler.ALIAS_WAS_UPDATED;
			if (!result) {
				msg.arg1 = SMSBankingActivityHandler.ERROR_HAPPENED;
			} else {
				msg.arg1 = SMSBankingActivityHandler.NO_ERROR;
			}
			thisActivityHandler.sendMessage(msg);
		}

		@Override
		public void dataWasLoaded(boolean result) {
			Message msg = thisActivityHandler.obtainMessage();
			msg.what = SMSBankingActivityHandler.DATA_FROM_SMS_WAS_LOADED;
			if (!result) {
				msg.arg1 = SMSBankingActivityHandler.ERROR_HAPPENED;
			} else {
				msg.arg1 = SMSBankingActivityHandler.NO_ERROR;
			}
			thisActivityHandler.sendMessage(msg);
		}

		@Override
		public void dataWasDeleted(boolean result) {
			Message msg = thisActivityHandler.obtainMessage();
			msg.what = SMSBankingActivityHandler.DATA_WAS_DELETED;
			if (!result) {
				msg.arg1 = SMSBankingActivityHandler.ERROR_HAPPENED;
			} else {
				msg.arg1 = SMSBankingActivityHandler.NO_ERROR;
			}
			thisActivityHandler.sendMessage(msg);			
		}

		@Override
		public void setBalance(String balanceValue) {
			Message msg = thisActivityHandler.obtainMessage();
			msg.what = SMSBankingActivityHandler.SET_BALANCE;
			msg.obj = balanceValue;
			thisActivityHandler.sendMessage(msg);			
		}

		@Override
		public void onReady() {
		    DebugLogging.log(context, (LOG_TAG + " onReady  "));
			Message msg = thisActivityHandler.obtainMessage();
			msg.what = SMSBankingActivityHandler.TREAD_IS_READY;
			thisActivityHandler.sendMessage(msg);				
		}

        @Override
        public void cardDataWasDeleted() {
            Message msg = thisActivityHandler.obtainMessage();
            msg.what = SMSBankingActivityHandler.CARD_WAS_DELETED;
            thisActivityHandler.sendMessage(msg);
        }

        @Override
        public void sqlLiteExceptionIsCatched() {
            Message msg = thisActivityHandler.obtainMessage();
            msg.what = SMSBankingActivityHandler.SQLITE_EXCEPTION;
            thisActivityHandler.sendMessage(msg);            
        }

	}
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		DebugLogging.log(getApplicationContext(), (LOG_TAG + " onCreate"));
		
		viewIntent = getIntent();
		bundle = viewIntent.getExtras();
		setContentView(R.layout.view_history);         
	    context = getApplicationContext();
	    resources = context.getResources();
        
	    isChecked = false;
	    thisActivityHandler = new SMSBankingActivityHandler();
	    
	   	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
    	boolean usingPassword = settings.getBoolean(resources.getString(R.string.using_password), false);
        filterMap = new HashMap<String, String>();
        filterMap.put(TransactionData.CARD_NUMBER, settings.getString(TransactionData.CARD_NUMBER, resources.getString(R.string.all)));
        filterMap.put(TransactionData.TRANSACTION_PLACE, settings.getString(TransactionData.TRANSACTION_PLACE, resources.getString(R.string.all)));
        if (!settings.contains(NEED_TO_LOAD)) {
        	SharedPreferences.Editor editorSettings = settings.edit();
    		editorSettings.putBoolean(NEED_TO_LOAD, true);
    		editorSettings.commit();
    		firstLoading = true;
        }
  
    	if ((usingPassword) && (!isChecked)) {
        	if (!settings.contains(PASSWORD_TEXT)) {
        		SharedPreferences.Editor editorSettings = settings.edit();
        		editorSettings.putString(PASSWORD_TEXT, DEFAULT_PASSWORD);
        		editorSettings.commit();
        		currentPassword = new String(DEFAULT_PASSWORD);
        	} else {
        		currentPassword = settings.getString(PASSWORD_TEXT, DEFAULT_PASSWORD);
        	}
 	    	showDialog(DIALOG_PASSWORD_CHECKING);
    	} else {
    		isChecked = true;
    	}
	        
        try {
            backupDb();
        } catch (Exception e) {}
        finally {}
        
        //saveLogFileToSD();
        deleteOldLod();
    }
    
    private void deleteOldLod() {
        String logFile = "/data/data/com.meryrua.smsbanking/log_file";
        File curLog = new File(logFile);
        if (curLog.exists()) {
            curLog.delete();
        }
    }
    
    @SuppressWarnings("unused")
    private void saveLogFileToSD() {
        Date curDate = new Date();
        String strFrmt = "dd-MM-yy";
        String logFile = "/data/data/com.meryrua.smsbanking/log_file";
        File root = new File(Environment.getExternalStorageDirectory(), "temp");
        
        File curLog = new File(logFile);
        File backupLog = new File(root, "/log_file" + DateFormat.format(strFrmt, curDate));

        if (backupLog.exists()) {
            backupLog.delete();
        }

        if (curLog.exists()) {
            makeLogsFolder();
            try {
                copy(curLog, backupLog);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
       }
    }
    
    private void prepareActivity() {
        //cardAdapter = new ArrayAdapter<String>(context, android.R.layout.select_dialog_item);
        cardAdapter = new ArrayAdapter<String>(context, android.R.layout.select_dialog_singlechoice);
		
		listView = getListView();
		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			 @Override
			    public boolean onItemLongClick(AdapterView<?> av, View v, int pos, long id) {
				 return onListItemLongClick(pos, id);
			    }
		});
		listView.setClickable(true);
		
	   	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		if (!settings.getBoolean(NEED_TO_LOAD, true)) {
			showActivityData();
		} else {
			showDialog(DIALOG_NEED_TO_LOAD);
		}
		showTransactionList();
    }
    
    private void showActivityData() {
		if(bundle != null) {
			if (viewIntent.getAction().equals(VIEW_TRANSACTION_DETAIL_INTENT)){
				transactionData = new TransactionData(bundle);
				bundle = null;	
				showDialog(DIALOG_SMS_DETAIL);
			}
		}
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
    	super.onNewIntent(intent);
    	if (isChecked) {
	    	if (intent.getAction().equals(UPDATE_TRANSACTION_LIST_INTENT)){
	    		showTransactionList();
	    	} else
	    	    if(intent.getAction().equals(VIEW_TRANSACTION_DETAIL_INTENT)) {
	    	        transactionData = new TransactionData(intent.getExtras());
	    	        showDialog(DIALOG_SMS_DETAIL);
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
       }
    }
    
    private void copy(File from, File to) throws FileNotFoundException, IOException {
        FileChannel src = null;
        FileChannel dst = null;
        try {
            src = new FileInputStream(from).getChannel();
            dst = new FileOutputStream(to).getChannel();
            dst.transferFrom(src, 0, src.size());
        } catch(FileNotFoundException ex) {
            ex.printStackTrace();
        } catch(IOException ex1) {
            ex1.printStackTrace();
        } finally {
            if (src != null) {
                src.close();
            }
            if (dst != null) {
                dst.close();
            }
        }
    }

    private void makeLogsFolder() {
       try {
           File sdFolder = new File(Environment.getExternalStorageDirectory(), "/temp/");
           sdFolder.mkdirs();
       } catch (Exception e) {}
     }
    

    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(Menu.NONE, IDM_PREFERENCES, Menu.NONE, resources.getString(R.string.settings));
    	menu.add(Menu.NONE, IDM_CARD_FILTER, Menu.NONE, resources.getString(R.string.card_filter));
        menu.add(Menu.NONE, IDM_OPERATIONS_FULTER, Menu.NONE, resources.getString(R.string.operation_filter));
        menu.add(Menu.NONE, IDM_DELETE_CARD, Menu.NONE, resources.getString(R.string.delete_card));
    	menu.add(Menu.NONE, IDM_DELETE_DATA, Menu.NONE, resources.getString(R.string.delete_data));

/*    	SubMenu subMenuFilters = menu.addSubMenu(resources.getString(R.string.operation_filter));
    	subMenuFilters.add(Menu.NONE, IDM_OPERATION_FILTER_ALL_OPERATION, Menu.NONE, resources.getString(R.string.all));
    	subMenuFilters.add(Menu.NONE, IDM_OPERATION_FILTER_CARD_OPERATION, Menu.NONE, resources.getString(R.string.card_operations));
    	subMenuFilters.add(Menu.NONE, IDM_OPERATION_FILTER_INCOMING_OPERATION, Menu.NONE, resources.getString(R.string.incoming_operations));
    	subMenuFilters.add(Menu.NONE, IDM_OPERATION_FILTER_OUTGOING_OPERATION, Menu.NONE, resources.getString(R.string.outgoing_operations));
    	subMenuFilters.setIcon(android.R.drawable.ic_menu_more);*/
    	return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
    	SharedPreferences.Editor editor = settings.edit();
    	switch(item.getItemId()) {
    	case IDM_PREFERENCES: {
    		Intent intentSettings = new Intent();
    		intentSettings.setClass(context, Settings.class);
    		startActivity(intentSettings);   
    		return true;
    	}
    	case IDM_CARD_FILTER:
    	    needToDeleteCard = false;
    		if ((connectionService != null) && (serviceThreadIsReady)){
    		    connectionService.getCardsData("");
    		}
    		return true;
    	case IDM_DELETE_CARD:
    	    needToDeleteCard = true;
    	    if ((connectionService != null) && (serviceThreadIsReady)){
                connectionService.getCardsData("");
            }
    	    return true;
       	case IDM_TEST: {
       		Intent intent = new Intent();
       		intent.setClass(context, SMSViewingActivity.class);
       		startActivity(intent);
    		return true;
       	}
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
    	case IDM_OPERATIONS_FULTER:
            showDialog(DIALOG_OPERATIONS_FILTER);
    	    break;
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
    protected void onResume() {
        DebugLogging.log(getApplicationContext(), (LOG_TAG + " before super onResume"));
    	super.onResume();
        DebugLogging.log(getApplicationContext(), (LOG_TAG + " onResume"));
        bindService(new Intent(context, DatabaseConnectionService.class), serviceConnection, Context.BIND_AUTO_CREATE);
        if (isChecked) {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage(resources.getText(R.string.loading));
            progressDialog.show();
            prepareActivity();
            showTransactionList();
        }
        updateIntentFilter = new IntentFilter(UPDATE_TRANSACTION_LIST_INTENT);
        updateIntentFilter.addCategory("android.intent.category.DEFAULT");
        updateReceiver = new UpdateReceiver();
        registerReceiver(updateReceiver, updateIntentFilter);
    }
    
    @Override
    protected void onPause() {
        DebugLogging.log(getApplicationContext(), (LOG_TAG + " onPause"));
        unbindService(serviceConnection);
        unregisterReceiver(updateReceiver);
        if ((transactionAdapter != null) && (transactionAdapter.getCursor() != null)) {
            stopManagingCursor(transactionAdapter.getCursor()); //this is for working on Android 3.0
        }
        super.onPause();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	if (isChecked) {
    		prepareActivity();
    	} else {
    		showDialog(DIALOG_PASSWORD_CHECKING);
    	}
    	super.onConfigurationChanged(newConfig);
    }
        
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		transactionData =  new TransactionData((Cursor)getListAdapter().getItem(position));
		showDialog(DIALOG_SMS_DETAIL);
 	}
	
	private boolean onListItemLongClick(int pos, long id) {
		transactionData = new TransactionData((Cursor)getListAdapter().getItem(pos));
	   	if ((connectionService != null) && (serviceThreadIsReady)) {
	   	    connectionService.getCardsData(transactionData.getCardNumber());
	   	}
		return true;
	}
	
	private AlertDialog buildSMSDetailDialog() {
        AlertDialog.Builder smsDetailDialogBuilder;
        
        LayoutInflater inflaterSMSDetail = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View layoutSMSDetail = inflaterSMSDetail.inflate(R.layout.sms_detail, (ViewGroup) findViewById(R.id.sms_detail_layout));
        
        smsDetailDialogBuilder = new AlertDialog.Builder(this);
        smsDetailDialogBuilder.setView(layoutSMSDetail);
        smsDetailDialogBuilder.setTitle(resources.getString(R.string.detail_info));
        smsDetailDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return smsDetailDialogBuilder.create();
	}
	
    private AlertDialog buildLoadingDialog() {
        AlertDialog.Builder loadingDialogBuilder;
        
        LayoutInflater inflaterLoading = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View layoutLoading = inflaterLoading.inflate(R.layout.my_progress, (ViewGroup) findViewById(R.id.my_progress_layout));
        
        loadingDialogBuilder = new AlertDialog.Builder(this);
        loadingDialogBuilder.setView(layoutLoading);
        
        return loadingDialogBuilder.create();
    }
	
    private AlertDialog buildCardFilterDialog() {
        AlertDialog.Builder cardFilterBuilder = new AlertDialog.Builder(this);
    
        int selectedCard = cardAdapter.getPosition(filterMap.get(TransactionData.CARD_NUMBER));
        cardFilterBuilder.setTitle(resources.getString(R.string.card_filter));
        /*cardFilterBuilder.setAdapter(cardAdapter, new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newSelection = cardAdapter.getItem(which);
                if (needToDeleteCard) {
                    if ((connectionService != null) && (serviceThreadIsReady)) {
                        connectionService.getCardsData(newSelection);
                    }                       
                } else {
                    filterMap.remove(TransactionData.CARD_NUMBER);
                    filterMap.put(TransactionData.CARD_NUMBER, newSelection);
                    
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(TransactionData.CARD_NUMBER, newSelection);
                    editor.commit();
                    showTransactionList();
                }
                dialog.dismiss();
            }
        });*/
        
        cardFilterBuilder.setNegativeButton(resources.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();                
            }
        });
        
        //for this style of Adapter should be android.R.layout.select_dialog_singlechoice
        cardFilterBuilder.setSingleChoiceItems(cardAdapter, selectedCard, new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newSelection = cardAdapter.getItem(which);
                if (needToDeleteCard) {
                    if ((connectionService != null) && (serviceThreadIsReady)) {
                        connectionService.getCardsData(newSelection);
                    }                       
                } else {
                    filterMap.remove(TransactionData.CARD_NUMBER);
                    filterMap.put(TransactionData.CARD_NUMBER, newSelection);
                    
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(TransactionData.CARD_NUMBER, newSelection);
                    editor.commit();
                    showTransactionList();
                }
                dialog.dismiss();
            }
        });
        
        return cardFilterBuilder.create();
    }
    
    private AlertDialog buildCardDataDialog() {
        AlertDialog.Builder cardDataDialogBuilder;
        
        LayoutInflater inflaterCardData = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View layoutCardData = inflaterCardData.inflate(R.layout.card_data, (ViewGroup) findViewById(R.id.card_data_layout));
        
        cardDataDialogBuilder = new AlertDialog.Builder(this);
        cardDataDialogBuilder.setView(layoutCardData);
        cardDataDialogBuilder.setPositiveButton(resources.getString(R.string.save), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (cardAliasString != null) {
                    if ((connectionService != null) && (serviceThreadIsReady)) {
                        connectionService.updateCardAlias(transactionData.getCardNumber(), cardAliasString);
                    }
                }
            }
        });
        cardDataDialogBuilder.setNegativeButton(resources.getString(R.string.close), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        cardDataDialogBuilder.setTitle(R.string.card_description);      
        return cardDataDialogBuilder.create();
    }
	
    private AlertDialog buildPasswordCheckingDialog() {
        AlertDialog.Builder passwordDialogBuilder;
        
        LayoutInflater inflaterPassword = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View layoutPassword = inflaterPassword.inflate(R.layout.password, (ViewGroup) findViewById(R.id.password_layout));
        
        passwordDialogBuilder = new AlertDialog.Builder(this);
        passwordDialogBuilder.setView(layoutPassword);
        passwordDialogBuilder.setPositiveButton(resources.getString(R.string.ok_string), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                passwordCheck();
            }
        });
        passwordDialogBuilder.setNegativeButton(resources.getString(R.string.close), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        passwordDialogBuilder.setCancelable(false);
        passwordDialogBuilder.setTitle(R.string.input_password);
        return passwordDialogBuilder.create();
    }
    
    private AlertDialog buildLoadDataFromSMSDialog() {
        AlertDialog.Builder loadDataRequestDialog;
        
        loadDataRequestDialog = new AlertDialog.Builder(this);
        loadDataRequestDialog.setMessage(resources.getString(R.string.load_from_sms));
        loadDataRequestDialog.setPositiveButton(resources.getString(R.string.yes), new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if ((connectionService != null) && (serviceThreadIsReady)) {
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    progressDialog = new ProgressDialog(SMSBankingActivity.this);
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage(resources.getText(R.string.loading));
                    progressDialog.show();
                    connectionService.deleteAllData(true);
                }
            }
        });
        loadDataRequestDialog.setNegativeButton(resources.getString(R.string.no), new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if ((connectionService != null) && (serviceThreadIsReady)){
                    connectionService.deleteAllData(false);
                }
            }
        });
        return loadDataRequestDialog.create();
    }
    
    private AlertDialog buildFirstLoadDialog() {
        String message;
        AlertDialog.Builder firstDataLoadDialog;
        
        firstDataLoadDialog = new AlertDialog.Builder(this);
        if (firstLoading) {
            firstLoading = false;
            message = new String(resources.getString(R.string.first_data_load) + " " + resources.getString(R.string.load_from_sms_request));
        } else {
            message = resources.getString(R.string.load_from_sms_request);
        }
        firstDataLoadDialog.setMessage(message);
        firstDataLoadDialog.setPositiveButton(resources.getString(R.string.yes), new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if ((connectionService != null) && (serviceThreadIsReady)) {
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    progressDialog = new ProgressDialog(SMSBankingActivity.this);
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage(resources.getText(R.string.loading));
                    progressDialog.show();
                    connectionService.loadDataFromSMS();
                }
            }
        });
        firstDataLoadDialog.setNegativeButton(resources.getString(R.string.no), new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences settings1 = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor1 = settings1.edit();
                editor1.putBoolean(NEED_TO_LOAD, false);
                editor1.commit();
                dialog.dismiss();
            }
        });
        return firstDataLoadDialog.create();
    }
    
    private AlertDialog buildDeleteCardData() {
        AlertDialog.Builder deleteCardDataDialog = new AlertDialog.Builder(this);
        
        deleteCardDataDialog.setPositiveButton(resources.getString(R.string.yes), new DialogInterface.OnClickListener() {
                
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if ((connectionService != null) && (serviceThreadIsReady)) {
                    connectionService.deleteCardData(cardForDelete);
                }
            }
            });
        deleteCardDataDialog.setNegativeButton(resources.getString(R.string.no), new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        deleteCardDataDialog.setMessage("");
        return deleteCardDataDialog.create();
    }
    
    private AlertDialog buildErrorDialog() {
        AlertDialog.Builder errorDialogBuilder;
        
        errorDialogBuilder = new AlertDialog.Builder(this);
        errorDialogBuilder.setMessage("");
        errorDialogBuilder.setPositiveButton(resources.getText(R.string.ok_string), new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        return errorDialogBuilder.create();
    }
    
	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog alertDialog;
		switch (id) {
		case DIALOG_SMS_DETAIL:
		    alertDialog = buildSMSDetailDialog();
			break;
		case DIALOG_LOADING:
		    alertDialog = buildLoadingDialog();
			break;
		case DIALOG_CARD_FILTER:
		    alertDialog = buildCardFilterDialog();
			break;
		case DIALOG_CARD_DATA:
		    alertDialog = buildCardDataDialog();
			break;
		case DIALOG_PASSWORD_CHECKING:
		    alertDialog = buildPasswordCheckingDialog();
			break;
		case DIALOG_SQLITE_ERROR_HAPPENED:
	        alertDialog = buildErrorDialog();
		    break;
		case DIALOG_WRONG_PASSWORD:
			alertDialog = buildErrorDialog();
			break;
		case DIALOG_LOAD_DATA_REQUEST:
			alertDialog = buildLoadDataFromSMSDialog();
			break;
		case DIALOG_NEED_TO_LOAD:
			alertDialog = buildFirstLoadDialog();
			break;
		case DIALOG_DELETE_CARD_DATA:
		    alertDialog = buildDeleteCardData();
		    break;
		case DIALOG_OPERATIONS_FILTER:
		    alertDialog = buildOperationsFilterDialog();
		    break;
		default:
			alertDialog = null;
		}
		return alertDialog;
	}

	private AlertDialog buildOperationsFilterDialog() {
	    AlertDialog.Builder operationsFilterBuilder = new AlertDialog.Builder(this);
	    final ArrayAdapter<String> operationsArray = new ArrayAdapter<String>(context, 
	            android.R.layout.select_dialog_singlechoice, new String []{resources.getString(R.string.all),
	            resources.getString(R.string.card_operations), resources.getString(R.string.incoming_operations),
	            resources.getString(R.string.outgoing_operations)});
   
	    int selectedOperation = operationsArray.getPosition(filterMap.get(TransactionData.TRANSACTION_PLACE));
	    operationsFilterBuilder.setTitle(resources.getString(R.string.card_operations));
	        
	    operationsFilterBuilder.setNegativeButton(resources.getString(R.string.cancel), new DialogInterface.OnClickListener() {
	            
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	            dialog.dismiss();                
	        }
	    });

	    operationsFilterBuilder.setSingleChoiceItems(operationsArray, selectedOperation, new DialogInterface.OnClickListener() {
	            
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
	            SharedPreferences.Editor editor = settings.edit();
	            String newSelection = operationsArray.getItem(which);
	            filterMap.remove(TransactionData.TRANSACTION_PLACE);
	            filterMap.put(TransactionData.TRANSACTION_PLACE, newSelection);
	                
	            editor.putString(TransactionData.TRANSACTION_PLACE, newSelection);
	            editor.commit();
	            showTransactionList();
	            dialog.dismiss();
	        }
	    });
	        
	    return operationsFilterBuilder.create();
    }

    private void passwordCheck() {
		if (currentPassword.equals(passwordString)) {
			isChecked = true;
			prepareActivity();
		} else {
			showDialog(DIALOG_WRONG_PASSWORD);
			finish();
		}
	}
	
	private void prepareSMSDetailDialog(Dialog dialog) {
        TextView cardNumberText = (TextView) dialog.findViewById(R.id.card_number);
        cardNumberText.setText(transactionData.getCardNumber());
        
        TextView dateText = (TextView) dialog.findViewById(R.id.date);
        dateText.setText(transactionData.getTransactionDate());

        TextView amountText = (TextView) dialog.findViewById(R.id.amount);
        String tranzValue = new String(Float.toString(transactionData.getTransactionValue()).replace(".", ","));
        tranzValue += transactionData.getTransactionCurrency();
        amountText.setText(tranzValue);
        
        TextView placeText = (TextView) dialog.findViewById(R.id.place);
        TextView placeOperationText = (TextView) dialog.findViewById(R.id.operation_place_name);
        String placeOrOperation = transactionData.getTransactionPlace();
        if (placeOrOperation.equals(TransactionData.INCOMING_BANK_OPERATION)) {
            placeOperationText.setText(resources.getString(R.string.operation_name));
            placeText.setText(resources.getString(R.string.operation_incoming_name));
        } else 
            if (placeOrOperation.equals(TransactionData.OUTGOING_BANK_OPERATION)) {
                placeOperationText.setText(resources.getString(R.string.operation_name));
                placeText.setText(resources.getString(R.string.operation_outgoing_name));
            } else {
                placeOperationText.setText(resources.getString(R.string.operation_place));
                placeText.setText(transactionData.getTransactionPlace());
            }

        TextView balanceText = (TextView) dialog.findViewById(R.id.balance);
        String balanceValue = new String(Float.toString(transactionData.getFundValue()).replace(".", ","));
        balanceValue += transactionData.getFundCurrency();
        balanceText.setText(balanceValue);
	}
	
	private void prepareCardDataDialog(Dialog dialog) {
        final EditText cardAlias = (EditText) dialog.findViewById(R.id.card_alias);
        cardAlias.addTextChangedListener(new TextWatcher(){
            @Override
            public void afterTextChanged(Editable s) {
                cardAliasString = new String (cardAlias.getText().toString());
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                    int arg2, int arg3) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                    int before, int count) {
            }
        });
        cardAlias.setText(cardAliasString);
	}
	
	private void preparePasswordCheckingDialog(Dialog dialog) {
        TextView repeatPassword = (TextView) dialog.findViewById(R.id.repeat_password_field);
        repeatPassword.setVisibility(TextView.GONE);
        TextView repeatPasswordText = (TextView) dialog.findViewById(R.id.repeat_password_text);
        repeatPasswordText.setVisibility(TextView.GONE);

        String passwordText = new String("");
        
        final EditText passwordField = (EditText) dialog.findViewById(R.id.password_field);
        passwordField.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                passwordString = new String (passwordField.getText().toString());
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                    int arg2, int arg3) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                    int before, int count) {
            }
        });
        passwordField.setText(passwordText);
	}
	
	private void prepareErrorDialog(Dialog dialog, String msg) {
	    ((AlertDialog) dialog).setMessage(msg);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
			case DIALOG_SMS_DETAIL:
			    prepareSMSDetailDialog(dialog);
				break;
			case DIALOG_CARD_FILTER:
			    /*if (!filterMap.get(TransactionData.CARD_NUMBER).equals(resources.getString(R.string.all))){
			        int i = cardAdapter.getPosition(filterMap.get(TransactionData.CARD_NUMBER));
			        dialog.findViewById(i).setPressed(true);
			    }*/
				break;
			case DIALOG_CARD_DATA:
			    prepareCardDataDialog(dialog);
				break;
			case DIALOG_PASSWORD_CHECKING:
			    preparePasswordCheckingDialog(dialog);
				break;
			case DIALOG_DELETE_CARD_DATA:
			    String aliasString = new String("");
		        aliasString += (cardAliasString.equals("")?(""):("(" + cardAliasString + ")"));
		        String dialogMessage = new String (resources.getString(R.string.delete_card_request) +
		                    " " + cardForDelete + " " + aliasString);
		        ((AlertDialog) dialog).setMessage(dialogMessage);
			    break;
		    case DIALOG_SQLITE_ERROR_HAPPENED:
                prepareErrorDialog(dialog, resources.getString(R.string.sqlite_ecxeption));
		        break;
		    case DIALOG_WRONG_PASSWORD:
		        prepareErrorDialog(dialog, resources.getString(R.string.wrong_password));
		        break;

		}
	}
	
	private String getSQLWhereFromFilter() {
		String sqlString = new String();
		if (!filterMap.get(TransactionData.CARD_NUMBER).equals(resources.getString(R.string.all))) {
			sqlString += TransactionData.CARD_NUMBER + "='" + filterMap.get(TransactionData.CARD_NUMBER) + "'";
		}
		if (!filterMap.get(TransactionData.TRANSACTION_PLACE).equals(resources.getString(R.string.all))) {
			if (sqlString.length() != 0) {
			    sqlString += " AND ";
			}
	    	if (filterMap.get(TransactionData.TRANSACTION_PLACE).equals(resources.getString(R.string.card_operations))){
	    		sqlString += "(" + TransactionData.TRANSACTION_PLACE + "<>'" + TransactionData.INCOMING_BANK_OPERATION + "') AND (" + TransactionData.TRANSACTION_PLACE + "<>'" + TransactionData.OUTGOING_BANK_OPERATION + "')";
	    	} else
	    	    if (filterMap.get(TransactionData.TRANSACTION_PLACE).equals(resources.getString(R.string.incoming_operations))) {
	    	        sqlString += TransactionData.TRANSACTION_PLACE + "='" + TransactionData.INCOMING_BANK_OPERATION + "'";
	    	    } else {
	    	        sqlString += TransactionData.TRANSACTION_PLACE + "='" + TransactionData.OUTGOING_BANK_OPERATION + "'";
	    	    }
		}
		return sqlString;
	}
	
	private void showTransactionList() {
		if (isChecked) {
			if ((connectionService != null) && (serviceThreadIsReady)) {
		   		connectionService.getTransactionData(getSQLWhereFromFilter());
				if (!filterMap.get(TransactionData.CARD_NUMBER).equals(resources.getString(R.string.all))) {
					connectionService.getBalance(filterMap.get(TransactionData.CARD_NUMBER));
				} else {
					hideBalance();
				}
		   	} else {
		   	    updateTransactionList();
		   	}
		}
	}
	
	private void setBalance(String balance) {
		TextView curBalance = (TextView) findViewById(R.id.current_balance);
		TextView filterInfo = (TextView) findViewById(R.id.filter_information);
        String str = new String();
        if (balance != null) {
	        str += resources.getString(R.string.operation_balance) + " " + balance;
	        curBalance.setVisibility(TextView.VISIBLE);
	        curBalance.setText(str);
        }
        String str1 = new String();
        str1 += resources.getString(R.string.operation_card_number) + filterMap.get(TransactionData.CARD_NUMBER);
        filterInfo.setVisibility(TextView.VISIBLE);
        filterInfo.setText(str1);
	}
	
	private void  hideBalance() {
		TextView curBalance = (TextView) findViewById(R.id.current_balance);
		TextView filterInfo = (TextView) findViewById(R.id.filter_information);
		if (!filterMap.get(TransactionData.TRANSACTION_PLACE).equals(resources.getString(R.string.all))) {
			curBalance.setText(null);
			curBalance.setVisibility(TextView.GONE);
		    String str1 = new String();
		    str1 += filterMap.get(TransactionData.TRANSACTION_PLACE);
		    filterInfo.setVisibility(TextView.VISIBLE);
		    filterInfo.setText(filterMap.get(TransactionData.TRANSACTION_PLACE));			
		} else {
			curBalance.setText(null);
			curBalance.setVisibility(TextView.GONE);
			filterInfo.setText(null);
			filterInfo.setVisibility(TextView.GONE);
		}
	}
	
	private void updateTransactionList() {
        DebugLogging.log(getApplicationContext(), (LOG_TAG + " updateTransactionList"));
		TextView filterInfo = (TextView) findViewById(R.id.filter_information);
		TextView noDataField = (TextView) findViewById(R.id.no_data_field);
		TextView noDataFieldInfo = (TextView) findViewById(R.id.no_data_field_info);
		TextView curBalance = (TextView) findViewById(R.id.current_balance);
		
	
		if (transactionAdapter != null) {
			if (transactionAdapter.getCursor() != null) {
			    transactionAdapter.getCursor().requery();
			}
			
			if ((transactionAdapter.getCursor() != null) && (transactionAdapter.getCursor().moveToFirst())) {
				listView.setVisibility(ListView.VISIBLE);
				noDataField.setVisibility(TextView.GONE);
				noDataFieldInfo.setVisibility(TextView.GONE);
			} else {
				filterInfo.setVisibility(TextView.GONE);
				listView.setVisibility(TextView.GONE);
				noDataField.setVisibility(TextView.VISIBLE);
				noDataFieldInfo.setVisibility(TextView.VISIBLE);
			}
			transactionAdapter.notifyDataSetChanged();
		} else {
			curBalance.setText(null);
			curBalance.setVisibility(TextView.GONE);
			filterInfo.setVisibility(TextView.GONE);
			listView.setVisibility(TextView.GONE);
			noDataField.setVisibility(TextView.VISIBLE);
			noDataFieldInfo.setVisibility(TextView.VISIBLE);			
		}

	}
}