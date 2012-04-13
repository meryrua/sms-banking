package com.meryrua.smsbanking;

import android.database.Cursor;

public interface DatabaseConnectionCallbackInterface {
	void showTransactionData(Cursor cursor);
	void showCardData(Cursor cursor);
	void showAllCardsData(Cursor cursor);

}
