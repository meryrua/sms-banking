package com.meryrua.smsbanking;

import java.io.FileInputStream;
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
import android.util.Log;
import android.util.Xml;

public class XMLParcerSerializer {
    private static final String FILE_NAME = "operations_pattern.xml";
    private static final String PATTERNS_TAG = "patterns";
    public static final String TRANSACTION_TAG = "transaction";
    public static final String INCOMING_TAG = "incoming";
    public static final String OUTGOING_TAG = "outgoing";
    
    private static final String LOG_TAG = "com.meryrua.smsbanking:XMLParcerSerializer";
    
    public void serializePatterns(HashMap<String, ArrayList<String>> patternMap, Context context){
        try {
            FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            
            XmlSerializer serializer = Xml.newSerializer();
            try {
                serializer.setOutput(fos, "UTF-8");
                serializer.startDocument(null, Boolean.valueOf(true));
                
                serializer.startTag(null, PATTERNS_TAG);
                    serializer.startTag(null, TRANSACTION_TAG);
                    for (int i = 0; i < patternMap.get(TRANSACTION_TAG).size(); i++){
                        serializer.text(patternMap.get(TRANSACTION_TAG).get(i)); 
                    }
                    serializer.endTag(null, TRANSACTION_TAG);
                    serializer.startTag(null, INCOMING_TAG);
                    for (int i = 0; i < patternMap.get(INCOMING_TAG).size(); i++){
                        serializer.text(patternMap.get(INCOMING_TAG).get(i)); 
                    }
                    serializer.endTag(null, INCOMING_TAG);
                    serializer.startTag(null, OUTGOING_TAG);
                    for (int i = 0; i < patternMap.get(OUTGOING_TAG).size(); i++){
                        serializer.text(patternMap.get(OUTGOING_TAG).get(i)); 
                    }
                    serializer.endTag(null, OUTGOING_TAG);
                serializer.endTag(null, PATTERNS_TAG); 
                serializer.endDocument();
                serializer.flush();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally{
                try {
                    fos.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public HashMap<String, ArrayList<String>> parcePatterns(Context context){
        HashMap<String, ArrayList<String>> resultMap = new HashMap<String, ArrayList<String>>();
        
        XmlPullParserFactory factory;
        try {
            factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            try {
                FileInputStream fis = context.openFileInput(FILE_NAME);

                xpp.setInput(fis, "UTF-8");
                int eventType = xpp.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                 if(eventType == XmlPullParser.START_DOCUMENT) {
                     Log.d(LOG_TAG, "start document");
                 } else if(eventType == XmlPullParser.START_TAG) {
                     Log.d(LOG_TAG, "start tag");
                     if (xpp.getName().equals(TRANSACTION_TAG)){
                         do{
                             try {
                                 eventType = xpp.next();
                                 if (eventType == XmlPullParser.TEXT){
                                     if (!resultMap.containsKey(TRANSACTION_TAG)){
                                         resultMap.put(TRANSACTION_TAG, new ArrayList<String>());
                                     }
                                     resultMap.get(TRANSACTION_TAG).add(xpp.getText());
                                 }
                             } catch (IOException e) {
                                 // TODO Auto-generated catch block
                                 e.printStackTrace();
                             }                            
                         }while (eventType == XmlPullParser.TEXT);
                         continue;
                     } else if (xpp.getName().equals(INCOMING_TAG)){
                         do{
                             try {
                                 eventType = xpp.next();
                                 if (eventType == XmlPullParser.TEXT){
                                     if (!resultMap.containsKey(INCOMING_TAG)){
                                         resultMap.put(INCOMING_TAG, new ArrayList<String>());
                                     }
                                     resultMap.get(INCOMING_TAG).add(xpp.getText());
                                 }
                             } catch (IOException e) {
                                 // TODO Auto-generated catch block
                                 e.printStackTrace();
                             }                            
                         }while (eventType == XmlPullParser.TEXT);
                         continue;
                         
                     } else if (xpp.getName().equals(OUTGOING_TAG)){
                         do{
                             try {
                                 eventType = xpp.next();
                                 if (eventType == XmlPullParser.TEXT){
                                     if (!resultMap.containsKey(OUTGOING_TAG)){
                                         resultMap.put(OUTGOING_TAG, new ArrayList<String>());
                                     }
                                     resultMap.get(OUTGOING_TAG).add(xpp.getText());
                                 }
                             } catch (IOException e) {
                                 // TODO Auto-generated catch block
                                 e.printStackTrace();
                             }                            
                         }while (eventType == XmlPullParser.TEXT);
                         continue;
                         
                     }
                 } else if(eventType == XmlPullParser.END_TAG) {
                     Log.d(LOG_TAG, "end tag");
                 } 
                 try {
                    eventType = xpp.next();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                }
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } 

        
        } catch (XmlPullParserException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        
        
        return resultMap;
    }

}
