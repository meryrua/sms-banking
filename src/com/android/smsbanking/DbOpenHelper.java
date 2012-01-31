package com.android.smsbanking;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbOpenHelper extends SQLiteOpenHelper{
	
	private static final int DB_VERSION = 1;
	private static final String DB_NAME = "smsbanking_base";
	
	public static final String TRANZACTION_TABLE_NAME = "tranzaction_data";
	
	private static final String CREATE_TRANZACTION_TABLE = "create table " + TRANZACTION_TABLE_NAME + 
		" (_id integer primary key autoincrement, " + TranzactionData.CARD_NUMBER + " TEXT, " + 
		TranzactionData.TRANZACTION_DATE + " TEXT, " + TranzactionData.TRANZACTION_PLACE + " TEXT, " + 
		TranzactionData.TRANZACTION_VALUE + " REAL, " + TranzactionData.TRANZACTION_CURRENCY + " TEXT, " + 
		TranzactionData.FUND_VALUE + " REAL, " + TranzactionData.FUND_CURRENCY + " TEXT);";
		
	public DbOpenHelper(Context context){
		super(context, DB_NAME, null, DB_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase){
		sqLiteDatabase.execSQL(CREATE_TRANZACTION_TABLE);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1){
	}
}
