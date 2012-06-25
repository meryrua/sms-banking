package ru.meryrua.smsbanking;

import java.util.Arrays;
import java.util.Comparator;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class PatternEditActivity extends Activity {
    public static final String MESSAGE_STRING = "message_string";
    private Context context;
    private String messageString;
    private String operationString;
    private String resultRegexp;
    private String tempResult;
    private int currentStep;
    private Resources resources;
    private boolean showResult = false;
    private boolean patternReady = false; 
    private int[] groupMap = new int[TransactionPattern.GROUP_NUMBER];
    private Button prevStepButton;
    private Button nextStepButton;
    private TextView messageView;
    
    private PatternElement[] patternElements = new PatternElement[TransactionPattern.GROUP_NUMBER];
    private String[] regexpPattern = {"(\\d+)", "(\\d+/\\d+/\\d+)", "([\\w+\\W+\\s*]+)", "(\\d+,\\d+)", "([\\wà-ÿÀ-ß]+)",
            "(\\d+,\\d+)", "([\\wà-ÿÀ-ß]+)", "(\\w+)"};
    
    private static class PatternElement {
        boolean set;
        int group;
        int startPosition;
        int endPosition;
        
        PatternElement() {
            set = false;
            group = -1;
            startPosition = 0;
            endPosition = 0; 
        }
        
        @SuppressWarnings("unused")
        PatternElement(PatternElement pE) {
            set = pE.set;
            group = pE.group;
            startPosition = pE.startPosition;
            endPosition = pE.endPosition;            
        }
        
        void setElement(int start, int end) {
            set = true;
            startPosition = start;
            endPosition = end;
        }     
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patter_editor);
        
        context = getApplicationContext();
        resources = context.getResources();
        currentStep = TransactionPattern.GROUP_CARD_NUMBER;
        
        Bundle startBundle = getIntent().getExtras();
        messageString = new String(startBundle.getString(MESSAGE_STRING));
        operationString = new String(startBundle.getString(TransactionData.OPERATION_NAME));
        
        initValues();
        showCurrentStep(currentStep);
        
        DebugLogging.log(context, "messageString " + messageString + " operationString " + operationString);
    }
    
    private void initValues() {
        messageView = (TextView) findViewById(R.id.sms_text);
        messageView.setText(messageString);
        
        String temp = new String(messageString);
        temp = temp.replace("*", "\\*");
        DebugLogging.log(context, "messageString " + messageString + " " + messageString.length() + " temp " + temp + " " + temp.length());
        
        TextView operationView = (TextView) findViewById(R.id.pattern_operation);
        operationView.setText(operationString);
        
        for (int i = 0; i < TransactionPattern.GROUP_NUMBER; i++) {
            patternElements[i] = new PatternElement();
            patternElements[i].group = i;
        }
        
        prevStepButton = (Button) findViewById(R.id.previous);
        prevStepButton.setEnabled(false);
        prevStepButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                markSelection(currentStep);
                if (currentStep != TransactionPattern.GROUP_CARD_NUMBER) {
                    currentStep--;
                }
                showCurrentStep(currentStep);
            }
        });
        
        nextStepButton = (Button) findViewById(R.id.next);
        nextStepButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                markSelection(currentStep);
                if (currentStep != TransactionPattern.GROUP_BANK_NUMBER) {
                    currentStep++;
                    showCurrentStep(currentStep);
                } else 
                    if (showResult) {
                        makePattern();
                        currentStep++;
                        showResultRegexp();
                    } 
            }
        });
        
        Button markButton = (Button) findViewById(R.id.select);
        markButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                markSelection(currentStep);
                showCurrentStep(currentStep);
            }
        });
        
        Button saveButton = (Button) findViewById(R.id.ok);
        saveButton.setOnClickListener(new OnClickListener() {
           @Override
           public void onClick(View arg0) {
               makePattern();
               savePattern();
               //here i will add serialize one pattern
               finish();
           }
        });
    
        Button cancelButton = (Button) findViewById(R.id.cancel);
        if (cancelButton != null) {
            cancelButton.setOnClickListener(new OnClickListener() {
               @Override
               public void onClick(View arg0) {
                   finish();
               }
            });
        }
        
        CheckBox checkForShowing = (CheckBox) findViewById(R.id.show_regexp);
        if (checkForShowing != null) {
            checkForShowing.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                    DebugLogging.log(context, "onCheckedChanged " + arg1);
                    showResult = arg1;
                }
            });
        }
    }
    
    private void markSelection(int step) {
        if (messageView.hasSelection()) {
            patternElements[step].setElement(messageView.getSelectionStart(), messageView.getSelectionEnd());
            DebugLogging.log(context, "start" + messageView.getSelectionStart() + " end " + messageView.getSelectionEnd());
            messageView.setSelected(false);
        }
    }
    private void showCurrentStep(int step) {
        TextView currentStepView = (TextView) findViewById(R.id.current_step_action);
        prevStepButton.setEnabled(true);
        nextStepButton.setEnabled(true);
        switch (step) {
        case TransactionPattern.GROUP_CARD_NUMBER:
            currentStepView.setText(R.string.select_card_number);
            prevStepButton.setEnabled(false);
            break;
        case TransactionPattern.GROUP_DATE_NUMBER:
            currentStepView.setText(R.string.select_date);
            break;
        case TransactionPattern.GROUP_TRANSACTION_VALUE_NUMBER:
            currentStepView.setText(R.string.select_amount);
            break;
        case TransactionPattern.GROUP_TRANSACTION_CURRENCY_NUMBER:
            currentStepView.setText(R.string.select_amount_currency);
            break;
        case TransactionPattern.GROUP_FUND_VALUE_NUMBER:
            currentStepView.setText(R.string.select_fund_balace);
            break;
        case TransactionPattern.GROUP_FUND_CURRENCY_NUMBER:
            currentStepView.setText(R.string.select_fund_currency);
            break;
        case TransactionPattern.GROUP_BANK_NUMBER:
            currentStepView.setText(R.string.select_bank);
            if (!showResult)
                nextStepButton.setEnabled(false);
            break;
        case TransactionPattern.GROUP_PLACE_NUMBER:
            currentStepView.setText(R.string.select_place);
            break;
        }
        showReadyPatternElements();
    }
    
    private void showResultRegexp() {
        messageView.setText(resultRegexp);
        nextStepButton.setEnabled(false);
    }
    
    private void showReadyPatternElements() {
        final SpannableStringBuilder sb = new SpannableStringBuilder(messageString);
        for (int i = 0; i < TransactionPattern.GROUP_NUMBER; i++) {
             if (patternElements[i].set) {
                sb.setSpan(new BackgroundColorSpan(Color.rgb(0, 255, 0)), patternElements[i].startPosition, patternElements[i].endPosition, Spannable.SPAN_INCLUSIVE_INCLUSIVE); // Set the text color for first 4 characters
                sb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), patternElements[i].startPosition, patternElements[i].endPosition, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            }
        }
        messageView.setText(sb);
    }
    
    private void makePattern() {
        DebugLogging.log("patternReady " + patternReady);
        if (patternReady) {
            return;
        }
        tempResult = messageString.replace("*", "\\*");
        resultRegexp = new String (tempResult);
        int symbolIndex = 0;
        int offset = 1;
        for (int i = 0; i < TransactionPattern.GROUP_NUMBER; i++) {
            DebugLogging.log(context, "makePattern  patternElements " + patternElements[i].set + " " +
                    patternElements[i].startPosition + " " + patternElements[i].endPosition);
        }
        do { 
            symbolIndex = messageString.indexOf("*", symbolIndex);
            if (symbolIndex != -1) {
                for (int i = 0; i < TransactionPattern.GROUP_NUMBER; i++) {
                     if (patternElements[i].set) {
                        if (patternElements[i].startPosition >= symbolIndex) {
                            patternElements[i].startPosition = patternElements[i].startPosition + offset;
                        }
                        if (patternElements[i].endPosition >= symbolIndex) {
                            patternElements[i].endPosition = patternElements[i].endPosition + offset;
                        }
                    }
                }
            }
        } while (symbolIndex != messageString.lastIndexOf("*", symbolIndex));
        for (int i = 0; i < TransactionPattern.GROUP_NUMBER; i++) {
             DebugLogging.log(context, "makePattern  patternElements " + patternElements[i].set + " " +
                            patternElements[i].startPosition + " " + patternElements[i].endPosition);
        }
        DebugLogging.log(context, "makePattern tempResult " + tempResult);
        for (int i = 0; i < TransactionPattern.GROUP_NUMBER; i++) {
            if (patternElements[i].set) {
                DebugLogging.log(context, "makePattern " + regexpPattern[i]);
                resultRegexp = resultRegexp.replace(tempResult.subSequence(patternElements[i].startPosition, 
                        patternElements[i].endPosition), regexpPattern[i]);
            }
        }
        resultRegexp = resultRegexp.replaceAll(" ", "\\\\s*");
        DebugLogging.log(context, "makePattern resultRegexp " + resultRegexp);
        patternReady = true;
    }
    
    private void getGroupMap() {
        Arrays.sort(patternElements, new Comparator<PatternElement>() {
            @Override
            public int compare(PatternElement arg0, PatternElement arg1) {
                return ((arg0.startPosition < arg1.startPosition) ? -1 : ((arg0.startPosition == arg1.startPosition) ? 0 : 1));
            }
        });
        for (int i = 0; i < TransactionPattern.GROUP_NUMBER; i++) {
            DebugLogging.log(context, "makePattern  patternElements " + patternElements[i].set + " " +
                    patternElements[i].startPosition + " " + patternElements[i].endPosition);
        }
        int number = 0;
        for (int i = 0; i < TransactionPattern.GROUP_NUMBER; i++) {
            if (patternElements[i].set) {
                number ++;
                DebugLogging.log(context, "getGroupMap patternElements.get(i).group " + patternElements[i].group +
                        " i " + i);
                groupMap[patternElements[i].group] = number;
            } else {
                groupMap[patternElements[i].group] = 0;
            }
        }
        for (int i = 0; i < TransactionPattern.GROUP_NUMBER; i++) {
            DebugLogging.log(context, "makePattern  groupMap i " + i + " " + groupMap[i]);
          }
    }
    
    private boolean savePattern() {
        boolean result = false;
        getGroupMap();
        String operation;
        if (operationString.equals(resources.getString(R.string.card_operations))) {
            operation = XMLParcerSerializer.CARD_OPERATION_TAG;
        } else 
            if (operationString.equals(resources.getString(R.string.incoming_operations))) {
                operation = XMLParcerSerializer.INCOMING_TAG;
            } else {
                operation = XMLParcerSerializer.OUTGOING_TAG;
            }
        TransactionPattern transactionPattern = new TransactionPattern(resultRegexp, groupMap);
        XMLParcerSerializer.addNewPattern(context, operation, transactionPattern);
        return result;
    }
}
