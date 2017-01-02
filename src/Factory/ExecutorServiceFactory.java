package Factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import Concrete.AsynchronousLogger;
import Interface.CustomExecutorServiceInterface;

public class ExecutorServiceFactory {

	private final static HashMap<ExecutorService, CustomExecutorServiceInterface> executorServicesSingleThreadMap = new HashMap<ExecutorService, CustomExecutorServiceInterface>();
	private final static HashMap<ExecutorService, List<CustomExecutorServiceInterface>> executorServicesThreadPoolMap = new HashMap<ExecutorService, List<CustomExecutorServiceInterface>>();
	private final static HashMap<ExecutorService, CustomExecutorServiceInterface> executorServicesForAsyncLoggersMap = new HashMap<ExecutorService, CustomExecutorServiceInterface>();
	private final static AsynchronousLogger asyncLogger = CustomLoggerFactory.getAsynchronousLoggerInstance();
	
	private ExecutorServiceFactory()
	{
		//do nothing
	}
	
	public static synchronized void createAndExecuteSingleThreadExecutor(CustomExecutorServiceInterface runnable)
	{
		ExecutorService executorService = createSingleThreadExecutorInstance();
		executorServicesSingleThreadMap.put(executorService, runnable);
		executorService.execute((Runnable) runnable);
	}
	
	public static synchronized void createAndExecuteSingleThreadExecutorForAsyncLogger(CustomExecutorServiceInterface runnable)
	{
		ExecutorService executorService = createSingleThreadExecutorInstance();
		executorServicesForAsyncLoggersMap.put(executorService, runnable);
		executorService.execute((Runnable) runnable);
	}
	
	private static ExecutorService createSingleThreadExecutorInstance()
	{
		return Executors.newSingleThreadExecutor();
	}
	
	public static synchronized ExecutorService createFixedThreadPoolExecutor(int numberOfThreads)
	{
		ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
		executorServicesThreadPoolMap.put(executorService, new ArrayList<CustomExecutorServiceInterface>());
		
		return executorService;
	}
	
	public static synchronized void executeThreadInPool(ExecutorService executorService, CustomExecutorServiceInterface runnable)
	{
		executorService.execute((Runnable) runnable);
	}
	
	public static synchronized void shutDownAllExecutors()
	{
		shutDownAllSingleThreadExecutors();
		shutDownAllThreadPoolExecutors();
	}
	
	public static synchronized void shutDownAllSingleThreadExecutors()
	{
		Iterator<Entry<ExecutorService, CustomExecutorServiceInterface>> iter = executorServicesSingleThreadMap.entrySet().iterator();
		while (iter.hasNext()) 
		{
			Map.Entry<ExecutorService, CustomExecutorServiceInterface> pair = (Map.Entry<ExecutorService, CustomExecutorServiceInterface>)iter.next();
			ExecutorService executorService = pair.getKey();
			CustomExecutorServiceInterface cesInterface = pair.getValue();
			String className = cesInterface.getClass().getName();
			
			try 
			{
				asyncLogger.log("Attempt to shutdown executor for :  " + className, true);
					
				cesInterface.shutDown();
				executorService.shutdown();
				executorService.awaitTermination(5, TimeUnit.SECONDS);
			}
			catch (InterruptedException e) 
			{
				asyncLogger.log("Tasks interrupted (" + className + ")", true);
			}
			finally 
			{
			    if (!executorService.isTerminated()) 
			    {
			    	asyncLogger.log("Cancel non-finished tasks (" + className + ")", true);
			        executorService.shutdownNow();
			        asyncLogger.log("Shutdown finished (" + className + ")", true);
			    }
			}
		}
	}
	
	public static synchronized void shutDownAllThreadPoolExecutors()
	{
		Iterator<Entry<ExecutorService, List<CustomExecutorServiceInterface>>> iter = executorServicesThreadPoolMap.entrySet().iterator();
		while (iter.hasNext()) 
		{
			Map.Entry<ExecutorService, List<CustomExecutorServiceInterface>> pair = (Map.Entry<ExecutorService, List<CustomExecutorServiceInterface>>)iter.next();
			ExecutorService executorService = pair.getKey();
			List<CustomExecutorServiceInterface> cesInterfaceList = pair.getValue();
			
			//call shutdown for each thread
			for(CustomExecutorServiceInterface cesInterface : cesInterfaceList)
			{
				cesInterface.shutDown();
			}
			
			try 
			{
				asyncLogger.log("Attempt to shutdown executor for fixed thread pool", true);
				executorService.shutdown();
				executorService.awaitTermination(5, TimeUnit.SECONDS);
			}
			catch (InterruptedException e) 
			{
				asyncLogger.log("Tasks interrupted", true);
			}
			finally 
			{
			    if (!executorService.isTerminated()) 
			    {
			    	asyncLogger.log("Cancel non-finished tasks", true);
			        executorService.shutdownNow();
			        asyncLogger.log("Shutdown finished", true);
			    }
			}
		}
	}
	
	public static synchronized void shutDownAllAsyncLoggerExecutors()
	{
		Iterator<Entry<ExecutorService, CustomExecutorServiceInterface>> iter = executorServicesForAsyncLoggersMap.entrySet().iterator();
		while (iter.hasNext()) 
		{
			Map.Entry<ExecutorService, CustomExecutorServiceInterface> pair = (Map.Entry<ExecutorService, CustomExecutorServiceInterface>)iter.next();
			ExecutorService executorService = pair.getKey();
			CustomExecutorServiceInterface cesInterface = pair.getValue();
			String className = cesInterface.getClass().getName();
			
			try 
			{
				System.out.println("Attempt to shutdown async logger executor for :  " + className);
				cesInterface.shutDown();
				executorService.shutdown();
				executorService.awaitTermination(5, TimeUnit.SECONDS);
			}
			catch (InterruptedException e) 
			{
				System.out.println("Tasks interrupted (" + className + ")");
			}
			finally 
			{
			    if (!executorService.isTerminated()) 
			    {
			    	System.err.println("Cancel non-finished tasks (" + className + ")");
			        executorService.shutdownNow();
				    System.out.println("Shutdown finished (" + className + ")");
			    }
			}
		}
	}
}
