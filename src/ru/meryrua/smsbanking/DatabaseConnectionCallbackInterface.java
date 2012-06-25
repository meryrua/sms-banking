package ru.meryrua.smsbanking;

import android.database.Cursor;

public interface DatabaseConnectionCallbackInterface {
	abstract void showTransactionData(Cursor cursor);
	abstract void showCardsData(Cursor cursor, String cardNumber);
	abstract void aliasUpdated(boolean result);
	abstract void dataWasLoaded(boolean result);
	abstract void dataWasDeleted(boolean result);
	abstract void setBalance(String balanceValue);
	abstract void onReady();
	abstract void cardDataWasDeleted();
	abstract void sqlLiteExceptionIsCatched();
}
