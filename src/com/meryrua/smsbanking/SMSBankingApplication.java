package com.meryrua.smsbanking;

import android.app.Application;

public class SMSBankingApplication extends Application{

	@Override
	public void onCreate(){
		super.onCreate();
		CrashReporter crashReporter = new CrashReporter();
		crashReporter.init(this);
		

	}
}
