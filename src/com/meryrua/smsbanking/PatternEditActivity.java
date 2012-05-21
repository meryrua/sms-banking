package com.meryrua.smsbanking;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

public class PatternEditActivity extends Activity {
    public static final String MESSAGE_STRING = "message_string";
    private Context context;
    private String messageString;
    private int currentStep;
    
    private static final int STEP_CARD = 1;
    private static final int STEP_DATE = 2;
    private static final int STEP_AMOUNT_VALUE = 3;
    private static final int STEP_AMOUNT_CURRENCY = 4;
    private static final int STEP_BALANCE_VALUE = 5;
    private static final int STEP_BALANCE_CURRENCY = 6;
    private static final int STEP_BANK_NAME = 7;
    private static final int STEP_PLACE = 8;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        
        
    }
    
    private void showCurrentStep(int step) {
        switch (step) {
        case STEP_CARD:
            break;
        case STEP_DATE:
            break;
        case STEP_AMOUNT_VALUE:
            break;
        case STEP_AMOUNT_CURRENCY:
            break;
        case STEP_BALANCE_VALUE:
            break;
        case STEP_BALANCE_CURRENCY:
            break;
        case STEP_BANK_NAME:
            break;
        case STEP_PLACE:
            break;
        }
    }
}
