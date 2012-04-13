package com.meryrua.smsbanking;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.Random;

import java.nio.channels.FileChannel;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.DateFormat;
import android.util.Log;

public class CrashReporter implements Thread.UncaughtExceptionHandler
{
	 String VersionName;
	 String PackageName;
	 String FilePath;
	 String PhoneModel;
	 String AndroidVersion;
	 String Board;
	 String Brand;
	// String CPU_ABI;
	 String Device;
	 String Display;
	 String FingerPrint;
	 String Host; 
	 String ID;
	 String Manufacturer;
	 String Model;
	 String Product;
	 String Tags;
	 long Time;
	 String Type;
	 String User; 

	 private Thread.UncaughtExceptionHandler previousHandler;
	 private static CrashReporter    S_mInstance;
	 private Context       curContext;
	 
	 public void init( Context context )
	 {
	  previousHandler = Thread.getDefaultUncaughtExceptionHandler();
	  Thread.setDefaultUncaughtExceptionHandler( this );  
	  curContext = context;
	 }
	 
	 public long getAvailableInternalMemorySize() { 
	        File path = Environment.getDataDirectory(); 
	        StatFs stat = new StatFs(path.getPath()); 
	        long blockSize = stat.getBlockSize(); 
	        long availableBlocks = stat.getAvailableBlocks(); 
	        return availableBlocks * blockSize; 
	    } 
	     
	 public long getTotalInternalMemorySize() { 
	        File path = Environment.getDataDirectory(); 
	        StatFs stat = new StatFs(path.getPath()); 
	        long blockSize = stat.getBlockSize(); 
	        long totalBlocks = stat.getBlockCount(); 
	        return totalBlocks * blockSize; 
	    } 
	 
	 public void uncaughtException(Thread t, Throwable e)
	 {
	  String Report = "";
	  final Writer result = new StringWriter();
	  final PrintWriter printWriter = new PrintWriter(result);
	  e.printStackTrace(printWriter);
	  String stacktrace = result.toString();
	  Report += stacktrace;

	  Report += "\n";
	  Report += "Cause : \n";
	  Report += "======= \n";

	  Throwable cause = e.getCause();
	  while (cause != null)
	  {
	   cause.printStackTrace( printWriter );
	   Report += result.toString();
	   cause = cause.getCause();
	  }
	  printWriter.close();
	  Report += "****  End of current Report ***";
	  saveAsFile(Report);
	  Log.d("NATALIA!!!", "Error received");
	  previousHandler.uncaughtException(t, e);
	 }
	 
	 static CrashReporter getInstance()
	 {
	  if ( S_mInstance == null )
	   S_mInstance = new CrashReporter();
	  return S_mInstance;
	 }
	 
	 private void saveAsFile(String errorContent)
	 {
		 Date curDate = new Date();
		 String strFrmt = "dd-MM-yy";
		 String fileName = "/stack-" + DateFormat.format(strFrmt, curDate) + ".stacktrace";
		try
	    {
	        File root = new File(Environment.getExternalStorageDirectory(), "temp");
	        if (!root.exists()) {
	            root.mkdirs();
	        }
	        File gpxfile = new File(root, fileName);
	        FileWriter writer = new FileWriter(gpxfile);
	        writer.append(errorContent);
	        writer.flush();
	        writer.close();
	    }
	    catch(IOException e)
	    {
	         e.printStackTrace();
	   	  Log.d("NATALIA ERROR ON ERROR", "er " + e.getMessage());
	    }
}
	 
	}

