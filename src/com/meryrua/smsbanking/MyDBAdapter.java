package com.meryrua.smsbanking;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

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
	public static final String ID = "_id";
	private static final String CREATE_TRANSACTION_TABLE = "create table " + TRANSACTION_TABLE_NAME +
	            " (" + ID + " integer primary key autoincrement, " + TransactionData.CARD_NUMBER + " TEXT, " +
	            TransactionData.TRANSACTION_DATE + " long, " + TransactionData.TRANSACTION_PLACE + " TEXT, " +
	            TransactionData.TRANSACTION_VALUE + " REAL, " + TransactionData.TRANSACTION_CURRENCY + " TEXT, " +
	            TransactionData.FUND_VALUE + " REAL, " + TransactionData.FUND_CURRENCY + " TEXT, " +
	            TransactionData.BANK_NAME + " TEXT);";
	
	private static final String CREATE_CARD_TABLE = "create table " + CARD_TABLE_NAME + " (" + ID + 
	            " integer primary key autoincrement, " + TransactionData.CARD_NUMBER + " TEXT, " + 
	            TransactionData.FUND_VALUE + " REAL, " + TransactionData.FUND_CURRENCY + " TEXT, " + 
	            CARD_ALIAS + " TEXT, " + TransactionData.BANK_NAME + " TEXT);";
	
	private static final String[] ALL_TRANSACTION_COLUMNS_NAME = new String[] {ID, TransactionData.CARD_NUMBER, 
		TransactionData.TRANSACTION_DATE, TransactionData.TRANSACTION_PLACE, TransactionData.TRANSACTION_VALUE,
		TransactionData.TRANSACTION_CURRENCY, TransactionData.FUND_VALUE, TransactionData.FUND_CURRENCY, TransactionData.BANK_NAME};
	
	private static final String[] ALL_CARDS_NUMBER_COLUMNS_NAME = new String[] {ID, TransactionData.CARD_NUMBER, 
		TransactionData.FUND_VALUE, TransactionData.FUND_CURRENCY, CARD_ALIAS, TransactionData.BANK_NAME};

	private Context context;
	private DbHelper dbHelper;
	private SQLiteDatabase db;
	private boolean databaseOpened = false;
	
	public static final String FILTER_VALUE = "filter_value";
	
	@SuppressWarnings("unused")
    private static final String LOG_TAG = "com.meryrua.smsbanking:MyDBAdapter";
	
	public MyDBAdapter(Context cont) {
		context = cont;
		dbHelper = new DbHelper(context, DB_NAME, null, DB_VERSION);
		}

	public MyDBAdapter open() {
		if (!isDatabaseOpen()) {
			try {
				db = dbHelper.getWritableDatabase();
				databaseOpened = true;
			} catch (SQLiteException ex) {
				try {
					db = dbHelper.getReadableDatabase();
					databaseOpened = true;
				} catch(SQLiteException ex1) {
					databaseOpened = false;
				}
			}
		}
		return this;
	}
	
	public MyDBAdapter openToRead() {
		if (!isDatabaseOpen()) {
			try {
				db = dbHelper.getReadableDatabase();	
				databaseOpened = true;
			} catch(SQLiteException ex) {
				databaseOpened = false;
			}
		}
		return this;
	}
	
	public void close() {
		if (isDatabaseOpen()) {
			db.close();
		}
		databaseOpened = false;
	}
	
	public boolean insertTransaction(TransactionData transactionData) {
		long i, j ;
		beginDatabaseTranzaction();
		{
    		i = insertTransactionData(transactionData);
    		if (isExistCard(transactionData.getCardNumber())) {
    			j = updateCardBalance(transactionData);
    		} else {
    			j = insertCardNumber(transactionData.getCardNumber(), transactionData.getFundValue(), transactionData.getFundCurrency(), transactionData.getBankName());
    		}
    		if ((i != -1) && (j != -1)) {
    		    setSuccesfullTranzaction();
    		}
		}
		endDatabaseTranzaction();
		return ((i != -1) && (j != -1));
}
	
	public long insertTransactionData(TransactionData transactionData) {
		long rowIndex = -1;
		if (isDatabaseOpen()) {
			ContentValues cv = new ContentValues();
			cv.put(TransactionData.CARD_NUMBER, transactionData.getCardNumber());
			cv.put(TransactionData.TRANSACTION_DATE, transactionData.getTransactionDate());
			cv.put(TransactionData.TRANSACTION_PLACE, transactionData.getTransactionPlace());
			cv.put(TransactionData.TRANSACTION_VALUE, Float.valueOf(transactionData.getTransactionValue()));
			cv.put(TransactionData.TRANSACTION_CURRENCY, transactionData.getTransactionCurrency());
			cv.put(TransactionData.FUND_VALUE, Float.valueOf(transactionData.getFundValue()));
			cv.put(TransactionData.FUND_CURRENCY, transactionData.getFundCurrency());
			cv.put(TransactionData.BANK_NAME, transactionData.getBankName());
			rowIndex = db.insert(TRANSACTION_TABLE_NAME, null, cv);
		}
		return rowIndex;
	}
	
	public void beginDatabaseTranzaction() {
		if (isDatabaseOpen()) {
		    db.beginTransaction();
		}
	}
	
	public void endDatabaseTranzaction() {
		if (isDatabaseOpen()) {
		    db.endTransaction();
		}
	}
	
	public void setSuccesfullTranzaction() {
		if (isDatabaseOpen()) {
		    db.setTransactionSuccessful();
		}
	}
	
	public boolean isDatabaseOpen() {
		return databaseOpened;
	}
	
	public TransactionData getTransactionFromCursor(Cursor transactionCursor) {
		TransactionData transactionData = new TransactionData();
		
		transactionData.setCardNumber(transactionCursor.getString(transactionCursor.getColumnIndex(TransactionData.CARD_NUMBER)));
		transactionData.setFundCurrency(transactionCursor.getString(transactionCursor.getColumnIndex(TransactionData.FUND_CURRENCY)));
		transactionData.setTransactionCurrency(transactionCursor.getString(transactionCursor.getColumnIndex(TransactionData.TRANSACTION_CURRENCY)));
		transactionData.setTransactionDate(transactionCursor.getString(transactionCursor.getColumnIndex(TransactionData.TRANSACTION_DATE)));
		transactionData.setTransactionPlace(transactionCursor.getString(transactionCursor.getColumnIndex(TransactionData.TRANSACTION_PLACE)));
		transactionData.setFundValue(transactionCursor.getFloat(transactionCursor.getColumnIndex(TransactionData.FUND_VALUE)));
		transactionData.setTransactionValue(transactionCursor.getFloat(transactionCursor.getColumnIndex(TransactionData.TRANSACTION_VALUE)));
		transactionData.setBankName(transactionCursor.getString(transactionCursor.getColumnIndex(TransactionData.BANK_NAME)));
		
		return transactionData;
	}

	
	public Cursor getAllTransaction() {
		if (isDatabaseOpen()) {
			return db.query(TRANSACTION_TABLE_NAME, ALL_TRANSACTION_COLUMNS_NAME, null, null, null, null, null);
		} else {
		    return null;
		}
	}

	public Cursor getTransactionWithFilter(String filter) {
		if (isDatabaseOpen()) {
		    //db.execSQL("sekect trhfg");
			return db.query(TRANSACTION_TABLE_NAME, ALL_TRANSACTION_COLUMNS_NAME, filter, null, null, null, new String(ID + " DESC"));
		} else {
		    return null;
		}
	}
	
	public Cursor selectCardsNumber (String cardNumber) {
		if (isDatabaseOpen()) {
			if (!cardNumber.equals("")) {
                return db.query(CARD_TABLE_NAME, ALL_CARDS_NUMBER_COLUMNS_NAME, 
                        String.format(TransactionData.CARD_NUMBER + "='%s'", cardNumber),
                        null, TransactionData.CARD_NUMBER, null, null);
			} else {
				return db.query(CARD_TABLE_NAME, ALL_CARDS_NUMBER_COLUMNS_NAME, null, null,
				        TransactionData.CARD_NUMBER, null, null);
			} 
		} else {
		    return null;	
		}
	}
	
	public boolean updateCardAlias(String cardAlias, String cardNumber) {
		long rowIndex = 0;
		
		ContentValues cv = new ContentValues();
		cv.put(CARD_ALIAS, cardAlias);
		if (isDatabaseOpen()) {
			rowIndex = db.update(CARD_TABLE_NAME, cv, 
			        String.format(TransactionData.CARD_NUMBER + "='%s'", cardNumber), null);
		}
		return (rowIndex > 0);
	}
	
	public long insertCardNumber(String cardNamber, float cardBalance, String cardCurrency, String bankName) {
		long rowIndex = -1;
		
		ContentValues cv = new ContentValues();
		cv.put(TransactionData.CARD_NUMBER, cardNamber);
		cv.put(TransactionData.FUND_VALUE, Float.valueOf(cardBalance));
		cv.put(TransactionData.FUND_CURRENCY, cardCurrency);
		cv.put(TransactionData.BANK_NAME, bankName);
		cv.put(CARD_ALIAS, "");
		if (isDatabaseOpen()) {
			rowIndex = db.insert(CARD_TABLE_NAME, null, cv);
		}
		return rowIndex;
	}
	
	public long updateCardBalance(TransactionData transactionData) {
		long rowIndex = 0;
		
		ContentValues cv = new ContentValues();
		cv.put(TransactionData.FUND_VALUE, Float.valueOf(transactionData.getFundValue()));
		cv.put(TransactionData.FUND_CURRENCY, transactionData.getFundCurrency());
		if (isDatabaseOpen()) {
			rowIndex = db.update(CARD_TABLE_NAME, cv, 
			        String.format(TransactionData.CARD_NUMBER + "='%s'",
			                transactionData.getCardNumber()), null);
		}
		return rowIndex;
	}
	
	public boolean isExistCard(String cardNumber) {
		boolean existing = false;
		if (isDatabaseOpen()) {
			Cursor cursor = db.query(CARD_TABLE_NAME, new String[]{ID, TransactionData.CARD_NUMBER},
			        String.format(TransactionData.CARD_NUMBER + "='%s'", cardNumber),
			        null, TransactionData.CARD_NUMBER, null, null);
			if (cursor.getCount() > 0) {
			    existing = true;
			}
			cursor.close();
		}
		return existing;
	}
	
	public Cursor getOperationPattern() {
		if (isDatabaseOpen()) {
			return db.query(CARD_OPERATION_PATTERN_TABLE_NAME, new String[] {ID, TRANSACTION_PATTERN_STRING,
			        INCOMING_OPERATION_PATTERN_STRING, OUTGOING_OPERATION_PATTERN_STRING}, null, 
			        null, null, null, null);
		} else {
			return null;
		}
	}
	
	public String getBalance(String cardNumber) {
		String balance = null;
		if (isDatabaseOpen()) {
			Cursor cursor = selectCardsNumber(cardNumber);
			if (cursor.moveToFirst()) {
				balance = cursor.getString(cursor.getColumnIndex(TransactionData.FUND_VALUE)).toString() +
				    cursor.getString(cursor.getColumnIndex(TransactionData.FUND_CURRENCY)).toString();
			} else {
				balance = "0.00 RUB";
			}
			cursor.close();
		}
		return balance;
	}
	
	public boolean deleteAllTransactions() {
		if (isDatabaseOpen()) {
			db.delete(TRANSACTION_TABLE_NAME, null, null);
			//db.execSQL("sekect trhfg");
			db.execSQL("drop table " + TRANSACTION_TABLE_NAME + ";");
			db.execSQL(CREATE_TRANSACTION_TABLE);
			return true;
		} else {
		    return false;
		}
	}
	
	public boolean deleteAllCards() {
		if (isDatabaseOpen()) {
			db.delete(CARD_TABLE_NAME, null, null);
			db.execSQL("drop table " + CARD_TABLE_NAME + ";");
			db.execSQL(CREATE_CARD_TABLE);
			return true;
		} else {
		    return false;
		}
	}
	
	
	public boolean deleteData() {
		return (deleteAllTransactions() && deleteAllCards());
	}
	
	public boolean deleteCardData(String cardNumber) {
	    boolean result = false;
	    if (isDatabaseOpen()) {
	        db.delete(CARD_TABLE_NAME, 
	                String.format(TransactionData.CARD_NUMBER + "='%s'", cardNumber), null);
	        db.delete(TRANSACTION_TABLE_NAME,
	                String.format(TransactionData.CARD_NUMBER + "='%s'", cardNumber), null);
	        result = true;
	    }
	    return result;
	}
	
	private class DbHelper extends SQLiteOpenHelper {
			
		public DbHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
		}
		
		@Override
		public void onCreate(SQLiteDatabase sqLiteDatabase) {
			sqLiteDatabase.execSQL(CREATE_TRANSACTION_TABLE);
			sqLiteDatabase.execSQL(CREATE_CARD_TABLE);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
			sqLiteDatabase.execSQL("drop table " + TRANSACTION_TABLE_NAME + ";");
			sqLiteDatabase.execSQL(CREATE_TRANSACTION_TABLE);
			sqLiteDatabase.execSQL(CREATE_CARD_TABLE);
		}
	}
}
