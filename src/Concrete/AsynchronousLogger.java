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

import Interface.CustomExecutorServiceInterface;

public class AsynchronousLogger implements Runnable, CustomExecutorServiceInterface {
	
	private final String LOGFILENAME;
	private final BlockingQueue<String> sharedQueue;
	private final PrintWriter writer;
	private final SimpleDateFormat sdf;
	private boolean shutdown;
	
	public AsynchronousLogger()
	{
		this.LOGFILENAME = "Logs/LogFile.log";
		this.sharedQueue = new LinkedBlockingQueue<String>();
		this.writer = createLogFile(LOGFILENAME);
		this.sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		this.shutdown = false;
	}
	
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
		
        boolean putSuccessfull = false; //flag makes sure that message is added even if there is an interrupted exception
        //boolean threadInterrupted = false;
        
        //this loop makes sure that even if the thread was interrupted we make sure that the message is added to the queue
        while(!putSuccessfull)
        {
			try 
			{
	            sharedQueue.put(dateStr + " \t " + message);
	            putSuccessfull = true;
	        } 
			catch (InterruptedException e) {
	            //Logger.getLogger(AsynchronousLogger.class.getName()).log(Level.SEVERE, Thread.currentThread().getName(), ex);
				System.out.println(e + " Message at exception in async logger : " + message);
	            
	            message += " (Logger attempt 2)";
	            
	            //this catch block clears out the interruptflag. so we set threadInterruptedflag to true.
	            //and in the following if block we make sure we interrupt the thread.
	            //threadInterrupted = true;
	        }
        }
        
        //if thread was interrupted during put then make sure we interrupt it once we have added it to queue
        //if (threadInterrupted)
        	//Thread.currentThread().interrupt();
		
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
	
	@Override
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
