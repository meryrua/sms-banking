package com.meryrua.smsbanking;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.DateFormat;
import android.util.Log;

public class CrashReporter implements Thread.UncaughtExceptionHandler{
	String VersionName;
	String PackageName;
	String FilePath;
	String PhoneModel;
	String AndroidVersion;
	String Board;
	String Brand;
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
	
	private static final String LOG_TAG = "com.meryrua.smsbanking:CrashReporter";
	 
	public void init( Context context ){
	    previousHandler = Thread.getDefaultUncaughtExceptionHandler();
	    Thread.setDefaultUncaughtExceptionHandler( this );  
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
	 
	public void uncaughtException(Thread t, Throwable e){
	    String Report = "";
	    final Writer result = new StringWriter();
	    final PrintWriter printWriter = new PrintWriter(result);
	    e.printStackTrace(printWriter);
	    String stacktrace = result.toString();
	    Report += stacktrace;

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
	    Log.d(LOG_TAG, "Error received");
	    previousHandler.uncaughtException(t, e);
	}
	 
	static CrashReporter getInstance(){
	    if ( S_mInstance == null )
	        S_mInstance = new CrashReporter();
	    return S_mInstance;
	}
	 
	private void saveAsFile(String errorContent){
	    Date curDate = new Date();
		String strFrmt = "dd-MM-yy";
		String fileName = "/stack-" + DateFormat.format(strFrmt, curDate) + ".stacktrace";
		try{
	        File root = new File(Environment.getExternalStorageDirectory(), "temp");
	        if (!root.exists()) {
	            root.mkdirs();
	        }
	        File gpxfile = new File(root, fileName);
	        FileWriter writer = new FileWriter(gpxfile);
	        writer.append(errorContent);
	        writer.flush();
	        writer.close();
	    }catch(IOException e){
	        e.printStackTrace();
	        Log.d(LOG_TAG, "er " + e.getMessage());
	    }
	} 
}