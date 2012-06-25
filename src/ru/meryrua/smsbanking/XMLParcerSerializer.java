package ru.meryrua.smsbanking;

import java.io.FileInputStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.util.Xml;

public class XMLParcerSerializer {
    private static final String FILE_NAME = "operations_pattern.xml";
    private static final String PATTERNS_TAG = "patterns";
    private static final String PATTERN_STRING_TAG = "string";
    private static final String PATTERN_GROUP_TAG = "group";
    private static final String TRANSACTION_PATTERN = "transaction"; 
    
    public static final String CARD_OPERATION_TAG = "card";
    public static final String INCOMING_TAG = "incoming";
    public static final String OUTGOING_TAG = "outgoing";
    

    private static final int NULL_INT = 48;
    
    //private static final String LOG_TAG = "com.meryrua.smsbanking:XMLParcerSerializer";
    
    static public void removeXMLFile() {
        String logFile = "/data/data/com.meryrua.smsbanking/files/" + FILE_NAME;
        File curLog = new File(logFile);
        if (curLog.exists()) {
            curLog.delete();
        }
    }
    
    static public boolean isExist() {
        String logFile = "/data/data/com.meryrua.smsbanking/files/" + FILE_NAME;
        File curLog = new File(logFile);
        if (curLog.exists()) {
            return true;
        } else {
            return false;
        }
    }
    
    static public void serializePatterns(HashMap<String, ArrayList<TransactionPattern>> patternMap, Context context) {
        StringBuffer buffer;
        try {
            FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            
            XmlSerializer serializer = Xml.newSerializer();
            try {
                serializer.setOutput(fos, "UTF-8");
                serializer.startDocument(null, Boolean.valueOf(true));
                
                serializer.startTag(null, PATTERNS_TAG);
                if (patternMap.containsKey(CARD_OPERATION_TAG)) {
                    serializer.startTag(null, CARD_OPERATION_TAG);
                    for (int i = 0; i < patternMap.get(CARD_OPERATION_TAG).size(); i++) {
                        serializer.startTag(null, TRANSACTION_PATTERN);
                        //pattern string
                        serializer.startTag(null, PATTERN_STRING_TAG);
                        serializer.text(patternMap.get(CARD_OPERATION_TAG).get(i).getPattern()); 
                        serializer.endTag(null, PATTERN_STRING_TAG);
                        
                        //pattern group
                        serializer.startTag(null, PATTERN_GROUP_TAG);
                        buffer = new StringBuffer();
                        for (int j = 0; j < TransactionPattern.GROUP_NUMBER; j++) {
                            buffer.append(patternMap.get(CARD_OPERATION_TAG).get(i).getValue(j));
                        }
                        serializer.text(buffer.toString()); 
                        serializer.endTag(null, PATTERN_GROUP_TAG);
                        serializer.endTag(null, TRANSACTION_PATTERN);
                    }
                    serializer.endTag(null, CARD_OPERATION_TAG);
                }
                if (patternMap.containsKey(INCOMING_TAG)) {
                    serializer.startTag(null, INCOMING_TAG);
                    for (int i = 0; i < patternMap.get(INCOMING_TAG).size(); i++) {
                        serializer.startTag(null, TRANSACTION_PATTERN);
                        //pattern string
                        serializer.startTag(null, PATTERN_STRING_TAG);
                        serializer.text(patternMap.get(INCOMING_TAG).get(i).getPattern()); 
                        serializer.endTag(null, PATTERN_STRING_TAG);
                        
                        //pattern group
                        serializer.startTag(null, PATTERN_GROUP_TAG);
                        buffer = new StringBuffer();
                        for (int j = 0; j < TransactionPattern.GROUP_NUMBER; j++) {
                            buffer.append(patternMap.get(INCOMING_TAG).get(i).getValue(j));
                        }
                        serializer.text(buffer.toString()); 
                        serializer.endTag(null, PATTERN_GROUP_TAG);
                        serializer.endTag(null, TRANSACTION_PATTERN);
                    }
                    serializer.endTag(null, INCOMING_TAG);
                }
                if (patternMap.containsKey(OUTGOING_TAG)) {
                    serializer.startTag(null, OUTGOING_TAG);
                    for (int i = 0; i < patternMap.get(OUTGOING_TAG).size(); i++) {
                        serializer.startTag(null, TRANSACTION_PATTERN);
                      //pattern string
                        serializer.startTag(null, PATTERN_STRING_TAG);
                        serializer.text(patternMap.get(OUTGOING_TAG).get(i).getPattern()); 
                        serializer.endTag(null, PATTERN_STRING_TAG);
                        
                        //pattern group
                        serializer.startTag(null, PATTERN_GROUP_TAG);
                        buffer = new StringBuffer();
                        for (int j = 0; j < TransactionPattern.GROUP_NUMBER; j++) {
                            buffer.append(patternMap.get(OUTGOING_TAG).get(i).getValue(j));
                        }
                        serializer.text(buffer.toString()); 
                        serializer.endTag(null, PATTERN_GROUP_TAG);
                        serializer.endTag(null, TRANSACTION_PATTERN);
                    }
                    serializer.endTag(null, OUTGOING_TAG);
                }
                serializer.endTag(null, PATTERNS_TAG); 
                serializer.endDocument();
                serializer.flush();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    
    static public HashMap<String, ArrayList<TransactionPattern>> parcePatterns(Context context) {
        HashMap<String, ArrayList<TransactionPattern>> resultMap = new HashMap<String, ArrayList<TransactionPattern>>();
        
        XmlPullParserFactory factory;
        try {
            factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            String pattern;
            char[] patternGroup;
            int[] group;
            try {
                FileInputStream fis = context.openFileInput(FILE_NAME);

                xpp.setInput(fis, "UTF-8");
                int eventType = xpp.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if(eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals(CARD_OPERATION_TAG)) {
                            do {
                                pattern = null;
                                patternGroup = null;
                                try {
                                    do {
                                        eventType = xpp.next();
                                        if (eventType == XmlPullParser.START_TAG) {
                                            if (xpp.getName().equals(PATTERN_STRING_TAG)) {
                                                eventType = xpp.next();
                                                if (eventType == XmlPullParser.TEXT) {
                                                    pattern = new String (xpp.getText());
                                                }
                                            } else 
                                                if (xpp.getName().equals(PATTERN_GROUP_TAG)) {
                                                    eventType = xpp.next();
                                                    if (eventType == XmlPullParser.TEXT) {
                                                        patternGroup = xpp.getText().toCharArray();;
                                                    }
                                                }
                                        }
                                    } while (!((eventType == XmlPullParser.END_TAG) && (xpp.getName().equals(TRANSACTION_PATTERN))));
                                    eventType = xpp.next();
                                    if ((pattern != null) && (patternGroup != null)) {
                                        if (!resultMap.containsKey(CARD_OPERATION_TAG)) {
                                            resultMap.put(CARD_OPERATION_TAG, new ArrayList<TransactionPattern>());
                                        }
                                        group = new int[TransactionPattern.GROUP_NUMBER];
                                        for (int j = 0; j < TransactionPattern.GROUP_NUMBER; j++) {
                                            group[j] = Character.codePointAt(patternGroup, j) - NULL_INT;
                                            
                                        }
                                        resultMap.get(CARD_OPERATION_TAG).add(new TransactionPattern(pattern, group));
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }                            
                            } while ((eventType == XmlPullParser.START_TAG) && (xpp.getName().equals(TRANSACTION_PATTERN)));
                            continue;
                        } else if (xpp.getName().equals(INCOMING_TAG)) {
                            do {
                                pattern = null;
                                patternGroup = null;
                                try {
                                    do {
                                        eventType = xpp.next();
                                        if (eventType == XmlPullParser.START_TAG) {
                                            if (xpp.getName().equals(PATTERN_STRING_TAG)) {
                                                eventType = xpp.next();
                                                if (eventType == XmlPullParser.TEXT) {
                                                    pattern = new String (xpp.getText());
                                                }
                                            } else 
                                                if (xpp.getName().equals(PATTERN_GROUP_TAG)) {
                                                    eventType = xpp.next();
                                                    if (eventType == XmlPullParser.TEXT) {
                                                        patternGroup = xpp.getText().toCharArray();;
                                                    }
                                                }
                                        }
                                    } while (!((eventType == XmlPullParser.END_TAG) && (xpp.getName().equals(TRANSACTION_PATTERN))));
                                    eventType = xpp.next();
                                    if ((pattern != null) && (patternGroup != null)) {
                                        if (!resultMap.containsKey(INCOMING_TAG)) {
                                            resultMap.put(INCOMING_TAG, new ArrayList<TransactionPattern>());
                                        }
                                        group = new int[TransactionPattern.GROUP_NUMBER];
                                        for (int j = 0; j < TransactionPattern.GROUP_NUMBER; j++) {
                                            group[j] = Character.codePointAt(patternGroup, j) - NULL_INT;
                                            
                                        }
                                        resultMap.get(INCOMING_TAG).add(new TransactionPattern(pattern, group));
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }                            
                            } while ((eventType == XmlPullParser.START_TAG) && (xpp.getName().equals(TRANSACTION_PATTERN)));
                            continue;
                        } else if (xpp.getName().equals(OUTGOING_TAG)) {
                            do {
                                pattern = null;
                                patternGroup = null;
                                try {
                                    do {
                                        eventType = xpp.next();
                                        if (eventType == XmlPullParser.START_TAG) {
                                            if (xpp.getName().equals(PATTERN_STRING_TAG)) {
                                                eventType = xpp.next();
                                                if (eventType == XmlPullParser.TEXT) {
                                                    pattern = new String (xpp.getText());
                                                }
                                            } else 
                                                if (xpp.getName().equals(PATTERN_GROUP_TAG)) {
                                                    eventType = xpp.next();
                                                    if (eventType == XmlPullParser.TEXT) {
                                                        patternGroup = xpp.getText().toCharArray();;
                                                    }
                                                }
                                        }
                                    } while (!((eventType == XmlPullParser.END_TAG) && (xpp.getName().equals(TRANSACTION_PATTERN))));
                                    eventType = xpp.next();
                                    if ((pattern != null) && (patternGroup != null)) {
                                        if (!resultMap.containsKey(OUTGOING_TAG)) {
                                            resultMap.put(OUTGOING_TAG, new ArrayList<TransactionPattern>());
                                        }
                                        group = new int[TransactionPattern.GROUP_NUMBER];
                                        for (int j = 0; j < TransactionPattern.GROUP_NUMBER; j++) {
                                            group[j] = Character.codePointAt(patternGroup, j) - NULL_INT;
                                            
                                        }
                                        resultMap.get(OUTGOING_TAG).add(new TransactionPattern(pattern, group));
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }                            
                            } while ((eventType == XmlPullParser.START_TAG) && (xpp.getName().equals(TRANSACTION_PATTERN)));
                            continue;
                        }
                    } 
                    try {
                        eventType = xpp.next();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } 
        } catch (XmlPullParserException e1) {
            e1.printStackTrace();
        }
        return resultMap;
    }
    
    public static boolean addNewPattern(Context context, String operationType, TransactionPattern transactionPattern) {
        boolean result = false;
        DebugLogging.log("addNewPattern operationType " + operationType);
        if (!SMSBankingApplication.operationPatterns.containsKey(operationType)) {
            SMSBankingApplication.operationPatterns.put(operationType, new ArrayList<TransactionPattern>());
        }
        SMSBankingApplication.operationPatterns.get(operationType).add(transactionPattern);
        if (isExist()) {
            removeXMLFile();
        }
        serializePatterns(SMSBankingApplication.operationPatterns, context);
        return result;
    }
}
