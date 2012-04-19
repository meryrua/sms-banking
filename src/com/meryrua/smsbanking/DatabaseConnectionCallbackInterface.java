package com.meryrua.smsbanking;

import android.database.Cursor;

public interface DatabaseConnectionCallbackInterface {
	void showTransactionData(Cursor cursor);
	void showCardsData(Cursor cursor, String cardNumber);
	void aliasUpdated(boolean result);
	void dataWasLoaded(boolean result);
	void dataWasDeleted(boolean result);
	void setBalance(String balanceValue);
	void onReady();
	void cardDataWasDeleted();
}
