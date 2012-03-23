package com.android.smsbanking;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class TestActivity extends Activity{
	
	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.my_progress); 

	}

}
