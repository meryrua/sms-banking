package com.android.smsbanking;

import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class MyDBAdapter {

	private static final int DB_VERSION = 2;
	private static final String DB_NAME = "smsbanking_base";
	private static final String TRANSACTION_TABLE_NAME = "transaction_data";
	private static final String CARD_TABLE_NAME = "card_table";
	private static final String CARD_OPERATION_PATTERN_TABLE_NAME = "card_operation_tbl";
	public static final String CARD_ALIAS = "card_alias";
	public static final String TRANSACTION_PATTERN_STRING = "transaction_pattern";
	public static final String INCOMING_OPERATION_PATTERN_STRING = "incoming_pattern";
	public static final String OUTGOING_OPERATION_PATTERN_STRING = "outgoing_pattern";
	private static final String ID = "_id";
	private static final String CREATE_TRANSACTION_TABLE = "create table " + TRANSACTION_TABLE_NAME + 
		" (" + ID + " integer primary key autoincrement, " + TransactionData.CARD_NUMBER + " TEXT, " + 
		TransactionData.TRANSACTION_DATE + " long, " + TransactionData.TRANSACTION_PLACE + " TEXT, " + 
		TransactionData.TRANSACTION_VALUE + " REAL, " + TransactionData.TRANSACTION_CURRENCY + " TEXT, " + 
		TransactionData.FUND_VALUE + " REAL, " + TransactionData.FUND_CURRENCY + " TEXT);";
	
	private static final String CREATE_TRANSACTION_TABLE_HELP = "create table " + TRANSACTION_TABLE_NAME +"_help" + 
	" (" + ID + " integer primary key autoincrement, " + TransactionData.CARD_NUMBER + " TEXT, " + 
	TransactionData.TRANSACTION_DATE + " long, " + TransactionData.TRANSACTION_PLACE + " TEXT, " + 
	TransactionData.TRANSACTION_VALUE + " REAL, " + TransactionData.TRANSACTION_CURRENCY + " TEXT, " + 
	TransactionData.FUND_VALUE + " REAL, " + TransactionData.FUND_CURRENCY + " TEXT);";
	
	private static final String CREATE_CARD_TABLE = "create table " + CARD_TABLE_NAME + 
	" (" + ID + " integer primary key autoincrement, " + TransactionData.CARD_NUMBER + " TEXT, " + 
		TransactionData.FUND_VALUE + " REAL, " + TransactionData.FUND_CURRENCY + " TEXT, " +
		CARD_ALIAS + " TEXT, " + TransactionData.BANK_NAME + " TEXT);";
	
	private static final String CREATE_PATTERN_TABLE = "create table " + CARD_OPERATION_PATTERN_TABLE_NAME + 
	" (" + ID + " integer primary key autoincrement, " + TRANSACTION_PATTERN_STRING + " TEXT, " + 
		INCOMING_OPERATION_PATTERN_STRING  + " TEXT, " + OUTGOING_OPERATION_PATTERN_STRING + " TEXT, " + 
		TransactionData.BANK_NAME + " TEXT);";
	
	private static final String[] ALL_TRANSACTION_COLUMNS_NAME = new String[] {ID, TransactionData.CARD_NUMBER, 
		TransactionData.TRANSACTION_DATE, TransactionData.TRANSACTION_PLACE, TransactionData.TRANSACTION_VALUE,
		TransactionData.TRANSACTION_CURRENCY, TransactionData.FUND_VALUE, TransactionData.FUND_CURRENCY};
	private static final String[] ALL_CARDS_NUMBER_COLUMNS_NAME = new String[] {ID, TransactionData.CARD_NUMBER, 
		TransactionData.FUND_VALUE, TransactionData.FUND_CURRENCY, CARD_ALIAS, TransactionData.BANK_NAME};

	private Context context;
	private DbHelper dbHelper;
	private SQLiteDatabase db;
	
	public static final String FILTER_VALUE = "filter_value";
	
	public MyDBAdapter(Context cont) {
		context = cont;
		dbHelper = new DbHelper(context, DB_NAME, null, DB_VERSION);
		}
	
	//нужен флаг, что база будет открыта только на чтение???
	public MyDBAdapter open() throws SQLiteException {
		try{
			db = dbHelper.getWritableDatabase();
		}
		catch (SQLiteException ex){
			db = dbHelper.getReadableDatabase();			
		}
		return this;
	}
	
	public void close(){
		db.close();
	}
	
	public void insertTransaction(TransactionData transactionData){
		if (isExistCard(transactionData.getCardNumber())){
			long i = insertTransactionData(transactionData);
			long j = updateCardBalance(transactionData);
		}
		else
		{
			long i = insertCardNumber(transactionData.getCardNumber(), transactionData.getFundValue(), transactionData.getFundCurrency(), transactionData.getBankName());
			long j = insertTransactionData(transactionData);
		}
	}
	
	public long insertTransactionData(TransactionData transactionData){
		long rowIndex = 0;
		
		ContentValues cv = new ContentValues();
		cv.put(TransactionData.CARD_NUMBER, transactionData.getCardNumber());
		cv.put(TransactionData.TRANSACTION_DATE, transactionData.getTransactionDate());
		cv.put(TransactionData.TRANSACTION_PLACE, transactionData.getTransactionPlace());
		cv.put(TransactionData.TRANSACTION_VALUE, Float.valueOf(transactionData.getTransactionValue()));
		cv.put(TransactionData.TRANSACTION_CURRENCY, transactionData.getTransactionCurrency());
		cv.put(TransactionData.FUND_VALUE, Float.valueOf(transactionData.getFundValue()));
		cv.put(TransactionData.FUND_CURRENCY, transactionData.getFundCurrency());
		rowIndex = db.insert(TRANSACTION_TABLE_NAME, null, cv);
		if (rowIndex <= 0)
			 Log.d("NATALIA!!!", "Error");
		
		return rowIndex;
	}
	
	public TransactionData getTransactionFromCursor(Cursor transactionCursor){
		TransactionData transactionData = new TransactionData();
		
		transactionData.setCardNumber(transactionCursor.getString(transactionCursor.getColumnIndex(TransactionData.CARD_NUMBER)));
		transactionData.setFundCurrency(transactionCursor.getString(transactionCursor.getColumnIndex(TransactionData.FUND_CURRENCY)));
		transactionData.setTransactionCurrency(transactionCursor.getString(transactionCursor.getColumnIndex(TransactionData.TRANSACTION_CURRENCY)));
		transactionData.setTransactionDate(transactionCursor.getString(transactionCursor.getColumnIndex(TransactionData.TRANSACTION_DATE)));
		transactionData.setTransactionPlace(transactionCursor.getString(transactionCursor.getColumnIndex(TransactionData.TRANSACTION_PLACE)));
		transactionData.setFundValue(transactionCursor.getFloat(transactionCursor.getColumnIndex(TransactionData.FUND_VALUE)));
		transactionData.setTransactionValue(transactionCursor.getFloat(transactionCursor.getColumnIndex(TransactionData.TRANSACTION_VALUE)));
		
		return transactionData;
	}

	
	public Cursor getAllTransaction(){
		return db.query(TRANSACTION_TABLE_NAME, ALL_TRANSACTION_COLUMNS_NAME, null, null, null, null, null);
	}

	public Cursor getTransactionWithFilter(String filter){
		return db.query(TRANSACTION_TABLE_NAME, ALL_TRANSACTION_COLUMNS_NAME, filter, null, null, null, new String(ID + " DESC"));
	}

	public boolean removeTransaction(long transactionId){
		return (db.delete(TRANSACTION_TABLE_NAME, ID + " = " + transactionId, null) > 0);
	}
	
	public Cursor selectCardsNumber (String cardNumber){
		Log.d("NATALIA!!! ", "request " + cardNumber);
		if (!cardNumber.equals(""))
			return db.query(CARD_TABLE_NAME, ALL_CARDS_NUMBER_COLUMNS_NAME, new String(TransactionData.CARD_NUMBER + "='" + cardNumber + "'"), null, TransactionData.CARD_NUMBER, null, null);
		else
			return db.query(CARD_TABLE_NAME, ALL_CARDS_NUMBER_COLUMNS_NAME, null, null, TransactionData.CARD_NUMBER, null, null);
			
	}
	
	public boolean updateCardAlias(String cardAlias, String cardNumber){
		long rowIndex = 0;
		
		ContentValues cv = new ContentValues();
		cv.put(CARD_ALIAS, cardAlias);
		rowIndex = db.update(CARD_TABLE_NAME, cv, new String(TransactionData.CARD_NUMBER + "='" + cardNumber +"'"), null);
		if (rowIndex <= 0)
			Log.d("NATALIA!!! ", "Update Error");
		return true;
	}
	
	public long insertCardNumber(String cardNamber, float cardBalance, String cardCurrency, String bankName){
		long rowIndex = 0;
		
		ContentValues cv = new ContentValues();
		cv.put(TransactionData.CARD_NUMBER, cardNamber);
		cv.put(TransactionData.FUND_VALUE, Float.valueOf(cardBalance));
		cv.put(TransactionData.FUND_CURRENCY, cardCurrency);
		cv.put(TransactionData.BANK_NAME, bankName);
		cv.put(CARD_ALIAS, "");
		rowIndex = db.insert(CARD_TABLE_NAME, null, cv);
		if (rowIndex <= 0)
			 Log.d("NATALIA!!!", "Error");
		
		return rowIndex;
	}
	
	public long updateCardBalance(TransactionData transactionData){
		long rowIndex = 0;
		
		ContentValues cv = new ContentValues();
		cv.put(TransactionData.FUND_VALUE, Float.valueOf(transactionData.getFundValue()));
		cv.put(TransactionData.FUND_CURRENCY, transactionData.getFundCurrency());
		rowIndex = db.update(CARD_TABLE_NAME, cv, new String(TransactionData.CARD_NUMBER + "='" + transactionData.getCardNumber() +"'"), null);
		if (rowIndex <= 0)
			Log.d("NATALIA!!! ", "Update Error");
		return rowIndex;
	}
	
	public boolean isExistCard(String cardNumber){
		boolean existing = false;
		Cursor cursor = db.query(CARD_TABLE_NAME, new String[]{ID, TransactionData.CARD_NUMBER}, new String(TransactionData.CARD_NUMBER + "='" + cardNumber + "'"), null, TransactionData.CARD_NUMBER, null, null);
		if (cursor.getCount() > 0)
			existing = true;
		cursor.close();
		return existing;
	}
	
	public Cursor getOperationPattern(){
		return db.query(CARD_OPERATION_PATTERN_TABLE_NAME, new String[] {ID, TRANSACTION_PATTERN_STRING, INCOMING_OPERATION_PATTERN_STRING, OUTGOING_OPERATION_PATTERN_STRING}, null, null, null, null, null);
	}
	
	public String getBalance(String cardNumber){
		String balance = new String();
		/*
		Cursor cursor = getTransactionWithFilter(TransactionData.CARD_NUMBER + "='" + cardNumber + "';");
		if (cursor.moveToLast()){
			balance += cursor.getString(cursor.getColumnIndex(TransactionData.FUND_VALUE)).toString() + " " +
			cursor.getString(cursor.getColumnIndex(TransactionData.FUND_CURRENCY)).toString();
		}*/
		Cursor cursor = selectCardsNumber(cardNumber);
		if (cursor.moveToFirst()){
			balance += cursor.getString(cursor.getColumnIndex(TransactionData.FUND_VALUE)).toString() + " " +
			cursor.getString(cursor.getColumnIndex(TransactionData.FUND_CURRENCY)).toString();
		}else{
			balance += "0.00 RUB";
		}
		cursor.close();
		return balance;
	}
	
	private class DbHelper extends SQLiteOpenHelper{
			
		public DbHelper(Context context, String name, CursorFactory factory, int version){
			super(context, name, factory, version);
		}
		
		@Override
		public void onCreate(SQLiteDatabase sqLiteDatabase){
			sqLiteDatabase.execSQL(CREATE_TRANSACTION_TABLE);
			sqLiteDatabase.execSQL(CREATE_CARD_TABLE);
			
			sqLiteDatabase.execSQL(CREATE_PATTERN_TABLE);
			sqLiteDatabase.execSQL("INSERT INTO " + CARD_OPERATION_PATTERN_TABLE_NAME + " (" + TRANSACTION_PATTERN_STRING + ", " + INCOMING_OPERATION_PATTERN_STRING + ", " + OUTGOING_OPERATION_PATTERN_STRING + ") VALUES ('" + SMSParcer.DEFAULT_TRANSACTION_PATTERN + "', '" + SMSParcer.DEFAULT_INCOMING_PATTERN + "', '" + SMSParcer.DEFAULT_OUTGOING_PATTERN + "');");

		}
		
		@Override
		public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1){
			/*sqLiteDatabase.execSQL("DROP TABLE " + TRANSACTION_TABLE_NAME + ";");
			sqLiteDatabase.execSQL(CREATE_TRANSACTION_TABLE);
			sqLiteDatabase.execSQL(CREATE_CARD_TABLE);
			
			sqLiteDatabase.execSQL(CREATE_PATTERN_TABLE);
			sqLiteDatabase.execSQL("INSERT INTO " + CARD_OPERATION_PATTERN_TABLE_NAME + " (" + TRANSACTION_PATTERN_STRING + ", " + INCOMING_OPERATION_PATTERN_STRING + ", " + OUTGOING_OPERATION_PATTERN_STRING + ") VALUES ('" + SMSParcer.DEFAULT_TRANSACTION_PATTERN + "', '" + SMSParcer.DEFAULT_INCOMING_PATTERN + "', '" + SMSParcer.DEFAULT_OUTGOING_PATTERN + "');");*/

			Log.d("NATALIA", "versions " + i + ", " + i1);
			if ((i == 1) &&(i1 == 2))
			{
				Log.d("NATALIA", "versions " + i + ", " + i1);
				sqLiteDatabase.execSQL(CREATE_CARD_TABLE);
				sqLiteDatabase.execSQL("INSERT INTO " + CARD_TABLE_NAME + "(" + TransactionData.CARD_NUMBER + ") SELECT DISTINCT " + TransactionData.CARD_NUMBER + " FROM " + TRANSACTION_TABLE_NAME + ";");
				
				sqLiteDatabase.execSQL(CREATE_TRANSACTION_TABLE_HELP);
				sqLiteDatabase.execSQL("INSERT INTO " + TRANSACTION_TABLE_NAME + "_help(" + TransactionData.CARD_NUMBER + ", " + 
						TransactionData.TRANSACTION_DATE + ", " + TransactionData.TRANSACTION_PLACE + ", " + 
						TransactionData.TRANSACTION_VALUE + ", " + TransactionData.TRANSACTION_CURRENCY + ", " + 
						TransactionData.FUND_VALUE + ", " + TransactionData.FUND_CURRENCY + ") SELECT (" + TransactionData.CARD_NUMBER + ", " + 
						TransactionData.TRANSACTION_DATE + ", " + TransactionData.TRANSACTION_PLACE + ", " + 
						TransactionData.TRANSACTION_VALUE + ", " + TransactionData.TRANSACTION_CURRENCY + ", " + 
						TransactionData.FUND_VALUE + ", " + TransactionData.FUND_CURRENCY + ") FROM " + TRANSACTION_TABLE_NAME + ";");
				
				sqLiteDatabase.execSQL("DROP TABLE " + TRANSACTION_TABLE_NAME + ";");
				sqLiteDatabase.execSQL(CREATE_TRANSACTION_TABLE);
				sqLiteDatabase.execSQL("INSERT INTO " + TRANSACTION_TABLE_NAME + "(" + TransactionData.CARD_NUMBER + ", " + 
						TransactionData.TRANSACTION_DATE + ", " + TransactionData.TRANSACTION_PLACE + ", " + 
						TransactionData.TRANSACTION_VALUE + ", " + TransactionData.TRANSACTION_CURRENCY + ", " + 
						TransactionData.FUND_VALUE + ", " + TransactionData.FUND_CURRENCY + ") SELECT (" + TransactionData.CARD_NUMBER + ", " + 
						TransactionData.TRANSACTION_DATE + ", " + TransactionData.TRANSACTION_PLACE + ", " + 
						TransactionData.TRANSACTION_VALUE + ", " + TransactionData.TRANSACTION_CURRENCY + ", " + 
						TransactionData.FUND_VALUE + ", " + TransactionData.FUND_CURRENCY + ") FROM " + TRANSACTION_TABLE_NAME + "_help;");
				
				sqLiteDatabase.execSQL(CREATE_PATTERN_TABLE);
				sqLiteDatabase.execSQL("INSERT INTO " + CARD_OPERATION_PATTERN_TABLE_NAME + " (" + TRANSACTION_PATTERN_STRING + ", " + INCOMING_OPERATION_PATTERN_STRING + ", " + OUTGOING_OPERATION_PATTERN_STRING + ") VALUES ('" + SMSParcer.DEFAULT_TRANSACTION_PATTERN + "', '" + SMSParcer.DEFAULT_INCOMING_PATTERN + "', '" + SMSParcer.DEFAULT_OUTGOING_PATTERN + "');");
				
			}
		}
	}
}
