package com.meryrua.smsbanking;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;

public class DebugLogging {
    private static final String FILENAME = "/smsbanking-log";

    private static final String LOG_TAG = "com.meryrua.smsbanking";
    private static boolean allowDebug = false;
    private static boolean readDebugMode = false;
    
    public static void log(Context context, String str) {
        if (!readDebugMode) {
            setDebuggable(context);
        }
        if (allowDebug) {
            String strFrmt = "dd-MM-yy kk:mm";
            File root = new File(Environment.getExternalStorageDirectory(), "temp");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, FILENAME);
            FileWriter writer;
            BufferedWriter bWriter;
            try {
                writer = new FileWriter(gpxfile, true);
                bWriter = new BufferedWriter(writer);
                bWriter.newLine();
                bWriter.append(DateFormat.format(strFrmt, new Date()) + LOG_TAG + " : " + str);
                bWriter.flush();
                bWriter.close();
                writer.close();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            Log.d(LOG_TAG, " : " + DateFormat.format(strFrmt, new Date()) + " "+ str);
        }
    }
    
    public static void log(String str) {
        if (allowDebug) {
            String strFrmt = "dd-MM-yy kk:mm";
            File root = new File(Environment.getExternalStorageDirectory(), "temp");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, FILENAME);
            FileWriter writer;
            BufferedWriter bWriter;
            try {
                writer = new FileWriter(gpxfile, true);
                bWriter = new BufferedWriter(writer);
                bWriter.newLine();
                bWriter.append(DateFormat.format(strFrmt, new Date()) + LOG_TAG + " : " + str);
                bWriter.flush();
                bWriter.close();
                writer.close();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            Log.d(LOG_TAG, " : " + DateFormat.format(strFrmt, new Date()) + str);
        }
    }

    

    public static void setDebuggable(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            int flags = packageInfo.applicationInfo.flags;
            allowDebug = (flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
            readDebugMode = true;
        } catch (NameNotFoundException e) {
            allowDebug = false;
            readDebugMode = false;
        }           
    }

}
