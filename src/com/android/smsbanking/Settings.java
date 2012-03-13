package com.android.smsbanking;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;




public class Settings extends PreferenceActivity{

	private Context context;
	private final static int DIALOG_SET_PASSWORD = 0;
	
	private static String passwordString;
	private static String repeatPasswordString;
	private static boolean passwordEquals = false;
	
	public final static String PASSWORD_FILE_NAME = "password_file.txt";
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		context = getApplicationContext();
		addPreferencesFromResource(R.xml.preferences);

	}
	
	@Override 
	protected void onStart(){
		super.onStart();
		
		Preference passwordPref = (Preference) findPreference(context.getResources().getString(R.string.change_password));
		passwordPref.setOnPreferenceClickListener(new OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				// TODO Auto-generated method stub
				showDialog(DIALOG_SET_PASSWORD);
				return false;
			}
			
		});
	}
	
	@Override
	protected Dialog onCreateDialog(int id){
		AlertDialog alertDialog;
		switch (id){
		case DIALOG_SET_PASSWORD:
			passwordString = null;
			repeatPasswordString = null;
			passwordEquals = false;
			
			AlertDialog.Builder setPasswordDialogBuilder = new AlertDialog.Builder(this);
			
			LayoutInflater inflaterPassword = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
			View layoutPassword = inflaterPassword.inflate(R.layout.password, (ViewGroup) findViewById(R.id.password_layout));
			setPasswordDialogBuilder.setTitle(R.string.input_password);
			setPasswordDialogBuilder.setView(layoutPassword);
			
			setPasswordDialogBuilder.setPositiveButton(context.getResources().getString(R.string.checking), new  DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					if (!repeatPasswordString.equals(passwordString)){
						passwordString = new String(SMSBankingActivity.DEFAULT_PASSWORD);
				        Toast.makeText(Settings.this,
				                 "Passwords do not match", Toast.LENGTH_SHORT).show();
					}
					savePassword();

				}
				
			});

			setPasswordDialogBuilder.setNegativeButton(context.getResources().getString(R.string.close), new  DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					
				}
				
			});
			alertDialog = setPasswordDialogBuilder.create();
			break;
		default:
			alertDialog = null;
		}
		return alertDialog;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog){
		switch (id){
		case DIALOG_SET_PASSWORD:
			final EditText firstPassword = (EditText) dialog.findViewById(R.id.password_field);
			firstPassword.addTextChangedListener(new TextWatcher(){

				@Override
				public void afterTextChanged(Editable s) {
					// TODO Auto-generated method stub
					passwordString = new String(firstPassword.getText().toString());
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
					// TODO Auto-generated method stub
					
				}
				
			});
			firstPassword.setText("");
			
			final EditText secondPassword = (EditText) dialog.findViewById(R.id.repeat_password_field);
			secondPassword.addTextChangedListener(new TextWatcher(){

				@Override
				public void afterTextChanged(Editable s) {
					// TODO Auto-generated method stub
					repeatPasswordString = new String(secondPassword.getText().toString());
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
					// TODO Auto-generated method stub
					
				}
				
			});
			secondPassword.setText("");
			break;

		}
	}
	
	private void savePassword(){
		FileOutputStream fos;
		try {
			fos = openFileOutput(PASSWORD_FILE_NAME, Context.MODE_PRIVATE);
		    OutputStreamWriter osw = new OutputStreamWriter(fos);
		    osw.write(passwordString);
		    osw.flush();
		    osw.close();
	    } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
