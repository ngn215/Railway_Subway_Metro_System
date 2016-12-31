package Concrete;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AsynchronousLogger implements Runnable {
	
	private final static String LOGFILENAME = "Logs/LogFile.log";
	private final static BlockingQueue<String> sharedQueue = new LinkedBlockingQueue<String>();
	private PrintWriter writer;
	private boolean shutdown = false;
	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	public void log(String message, boolean printToConsole)
	{
		//StringBuilder sb = new StringBuilder();
		//sb.append(message);
		//sb.append(" ");
		//sb.append(System.currentTimeMillis());
		
		if (printToConsole)
        {
        	System.out.println(message);
        }
        
        String dateStr = getFormattedDate();
		
		try 
		{
            sharedQueue.put(dateStr + " \t " + message);
        } 
		catch (InterruptedException ex) {
            Logger.getLogger(AsynchronousLogger.class.getName()).log(Level.SEVERE, Thread.currentThread().getName(), ex);
        }
		
	}
	
	public void log(String message)
	{
		log(message, false);
	}
	
	public String getFormattedDate()
	{
	    return sdf.format(new Date());
	}
	
	@Override
	public void run() {
		
		//System.out.println("Starting " + AsynchronousLogger.class.getName() + "...");
		writer = createLogFile(LOGFILENAME);
		
		try
		{
			// TODO Auto-generated method stub
			while(!shutdown || sharedQueue.peek() != null)
			{
	            	String text = sharedQueue.poll(1, TimeUnit.SECONDS);
	                
	            	if (text != null)
	            		writer.println(text);
	        }
		}
		catch (InterruptedException e) 
		{
			Logger.getLogger(AsynchronousLogger.class.getName()).log(Level.SEVERE, null, e);
        }
		finally
		{
			closeLogFile();
		}
		
	}
	
	private PrintWriter createLogFile(String logFileName)
	{
		try {
			return new PrintWriter(logFileName, "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			Logger.getLogger(AsynchronousLogger.class.getName()).log(Level.SEVERE, null, e);
		}
		
		return null;
	}
	
	public void shutDown()
	{
		shutdown = true;
	}
	
	private void closeLogFile()
	{
		System.out.println("Closing log file");
		writer.close();
	}

}
