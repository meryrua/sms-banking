package com.android.smsbanking;

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

	private static final int DB_VERSION = 1;
	private static final String DB_NAME = "smsbanking_base";
	private static final String TRANSACTION_TABLE_NAME = "tranzaction_data";
	private static final String ID = "_id";
	private static final String CREATE_TRANSACTION_TABLE = "create table " + TRANSACTION_TABLE_NAME + 
		" (" + ID + " integer primary key autoincrement, " + TransactionData.CARD_NUMBER + " TEXT, " + 
		TransactionData.TRANSACTION_DATE + " long, " + TransactionData.TRANSACTION_PLACE + " TEXT, " + 
		TransactionData.TRANSACTION_VALUE + " REAL, " + TransactionData.TRANSACTION_CURRENCY + " TEXT, " + 
		TransactionData.FUND_VALUE + " REAL, " + TransactionData.FUND_CURRENCY + " TEXT);";
	
	private static final String SELECT_ALL_TRANSACTION = "select all from " + TRANSACTION_TABLE_NAME + ";";
	private static final String[] allColumnsName = new String[] {ID, TransactionData.CARD_NUMBER, 
		TransactionData.TRANSACTION_DATE, TransactionData.TRANSACTION_PLACE, TransactionData.TRANSACTION_VALUE,
		TransactionData.TRANSACTION_CURRENCY, TransactionData.FUND_VALUE, TransactionData.FUND_CURRENCY};
	private static final String[] dataForList = new String[] {ID, TransactionData.CARD_NUMBER, 
		TransactionData.TRANSACTION_DATE, TransactionData.TRANSACTION_VALUE, TransactionData.TRANSACTION_CURRENCY};

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
	
	public long insertTransaction(TransactionData transactionData){
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
	
	public Cursor getAllTransaction(){
		return db.query(TRANSACTION_TABLE_NAME, allColumnsName, null, null, null, null, null);
	}

	public Cursor getTransactionWithFilter(String filter){
		return db.query(TRANSACTION_TABLE_NAME, allColumnsName, filter, null, null, null, null);
	}

	public boolean removeTransaction(long transactionId){
		return (db.delete(TRANSACTION_TABLE_NAME, ID + " = " + transactionId, null) > 0);
	}
	
	public Cursor getCardsNumber (){
		return db.query(TRANSACTION_TABLE_NAME, new String[]{ID, TransactionData.CARD_NUMBER}, null, null, TransactionData.CARD_NUMBER, null, null);
	}
	
	private class DbHelper extends SQLiteOpenHelper{
			
		public DbHelper(Context context, String name, CursorFactory factory, int version){
			super(context, name, factory, version);
		}
		
		@Override
		public void onCreate(SQLiteDatabase sqLiteDatabase){
			sqLiteDatabase.execSQL(CREATE_TRANSACTION_TABLE);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1){
		}
	}
}
