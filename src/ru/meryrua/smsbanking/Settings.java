package ru.meryrua.smsbanking;

import ru.meryrua.smsbanking.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

public class Settings extends PreferenceActivity {

	private Context context;
	private static String passwordString;
	private static String repeatPasswordString;
	private final static int DIALOG_SET_PASSWORD = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		context = getApplicationContext();
		addPreferencesFromResource(R.xml.preferences);
	}
	
	@Override 
	protected void onStart() {
		super.onStart();
		
		CheckBoxPreference usePassword = (CheckBoxPreference) findPreference(context.getResources().getString(R.string.using_password));
		usePassword.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){

			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				if (newValue.equals(new Boolean(true))) {
				    showDialog(DIALOG_SET_PASSWORD);
				}
				return true;
			}
			
		});
		
		Preference passwordPref = (Preference) findPreference(context.getResources().getString(R.string.change_password));
		passwordPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				showDialog(DIALOG_SET_PASSWORD);
				return false;
			}
			
		});
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog alertDialog;
		switch (id) {
		case DIALOG_SET_PASSWORD: {
			passwordString = null;
			repeatPasswordString = null;
			
			AlertDialog.Builder setPasswordDialogBuilder = new AlertDialog.Builder(this);
			
			LayoutInflater inflaterPassword = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
			View layoutPassword = inflaterPassword.inflate(R.layout.password, (ViewGroup) findViewById(R.id.password_layout));
			setPasswordDialogBuilder.setTitle(R.string.input_password);
			setPasswordDialogBuilder.setView(layoutPassword);
			
			setPasswordDialogBuilder.setPositiveButton(context.getResources().getString(R.string.save), new  DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (!repeatPasswordString.equals(passwordString)){
						passwordString = new String(SMSBankingActivity.DEFAULT_PASSWORD);
				        Toast.makeText(Settings.this,
				                 "Passwords do not match", Toast.LENGTH_SHORT).show();
					}
					savePassword();

				}
				
			});

			setPasswordDialogBuilder.setNegativeButton(context.getResources().getString(R.string.close), new  DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					
				}
			});
			alertDialog = setPasswordDialogBuilder.create();
			break;
		}
		default:
			alertDialog = null;
		}
		return alertDialog;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case DIALOG_SET_PASSWORD: {
			final EditText firstPassword = (EditText) dialog.findViewById(R.id.password_field);
			firstPassword.addTextChangedListener(new TextWatcher() {

				@Override
				public void afterTextChanged(Editable s) {
					passwordString = new String(firstPassword.getText().toString());
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
				}

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
				}
				
			});
			firstPassword.setText("");
			
			final EditText secondPassword = (EditText) dialog.findViewById(R.id.repeat_password_field);
			secondPassword.addTextChangedListener(new TextWatcher() {

				@Override
				public void afterTextChanged(Editable s) {
					repeatPasswordString = new String(secondPassword.getText().toString());
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
				}

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
				}
				
			});
			secondPassword.setText("");
			break;
		}
		default:
		    //ERROR!!!
		}
	}
	
	private void savePassword() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = settings.edit();

		editor.putString(SMSBankingActivity.PASSWORD_TEXT, passwordString);
		editor.commit();
	}
}
