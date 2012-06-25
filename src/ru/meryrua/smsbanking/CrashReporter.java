package ru.meryrua.smsbanking;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.util.Date;
import android.content.Context;
import android.os.Environment;
import android.text.format.DateFormat;

public class CrashReporter implements Thread.UncaughtExceptionHandler {
	private Thread.UncaughtExceptionHandler previousHandler;
	private static CrashReporter s_mInstance;
	
	@SuppressWarnings("unused")
    private static final String LOG_TAG = "com.meryrua.smsbanking:CrashReporter";
	 
	public void init(Context context) {
	    previousHandler = Thread.getDefaultUncaughtExceptionHandler();
	    Thread.setDefaultUncaughtExceptionHandler(this);  
	}
	 
	/*public long getAvailableInternalMemorySize() { 
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
	} */
	 
	public void uncaughtException(Thread t, Throwable e) {
	    String report = "";
	    final Writer result = new StringWriter();
	    final PrintWriter printWriter = new PrintWriter(result);
	    e.printStackTrace(printWriter);
	    String stacktrace = result.toString();
	    report += stacktrace;

	    Throwable cause = e.getCause();
	    while (cause != null) {
	        cause.printStackTrace( printWriter );
	        report += result.toString();
	        cause = cause.getCause();
	    }
	    printWriter.close();
	    saveAsFile(report);
	    previousHandler.uncaughtException(t, e);
	}
	 
	static CrashReporter getInstance() {
	    if ( s_mInstance == null ) {
	        s_mInstance = new CrashReporter();
	    }
	    return s_mInstance;
	}
	 
	private void saveLogFileToSD() {
	    Date curDate = new Date();
	    String strFrmt = "dd-MM-yy";
        String logFile = "/log_file";
        File root = new File(Environment.getExternalStorageDirectory(), "temp");
        File data = Environment.getDataDirectory();
        
        File currentLog = new File(data, logFile);
        File backupLog = new File(root, logFile + DateFormat.format(strFrmt, curDate));

        if (backupLog.exists())
            backupLog.delete();

        if (currentLog.exists()) {
            makeLogsFolder();
            try {
                copy(currentLog, backupLog);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
       }
    }
	
	private void copy(File from, File to) throws FileNotFoundException, IOException {
	    FileChannel src = null;
	    FileChannel dst = null;
	    try {
	        src = new FileInputStream(from).getChannel();
	        dst = new FileOutputStream(to).getChannel();
	        dst.transferFrom(src, 0, src.size());
	    } catch(FileNotFoundException ex) {
	        ex.printStackTrace();
	    } catch(IOException ex1) {
	        ex1.printStackTrace();
	    } finally {
	        if (src != null) {
	            src.close();
	        }
	        if (dst != null) {
	            dst.close();
	        }
	    }
	}

    private void makeLogsFolder() {
        try {
            File sdFolder = new File(Environment.getExternalStorageDirectory(), "/temp/");
            sdFolder.mkdirs();
        } catch (Exception e) {}
    }
	
	private void saveAsFile(String errorContent) {
	    saveLogFileToSD();
	    Date curDate = new Date();
		String strFrmt = "dd-MM-yy hh:mm";
		String fileName = "/smsbanking-log";
		try {
	        File root = new File(Environment.getExternalStorageDirectory(), "temp");
	        if (!root.exists()) {
	            root.mkdirs();
	        }
	        File gpxfile = new File(root, fileName);
	        FileWriter writer = new FileWriter(gpxfile, true);
	        BufferedWriter bWriter = new BufferedWriter(writer);
	        bWriter.newLine();
	        bWriter.append(DateFormat.format(strFrmt, curDate));
	        bWriter.newLine();
	        bWriter.append(errorContent);
	        bWriter.flush();
	        bWriter.close();
	        writer.close();
	    } catch(IOException e) {
	        e.printStackTrace();
	    }
	} 
}