package Factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CustomThreadFactory {
	
	//http://howtodoinjava.com/core-java/multi-threading/creating-threads-using-java-util-concurrent-threadfactory/
	//https://examples.javacodegeeks.com/core-java/util/concurrent/threadfactory/java-util-concurrent-threadfactory-example/
			
	private static int threadCount = 0;
	private static HashMap<String,List<Thread>> entityThreadMap = new HashMap<String,List<Thread>>();
	
	private CustomThreadFactory()
	{
		//do nothing
	}
	
	public static Thread getThread(Runnable runnable, String name, String entity)
	{
		Thread thread = new Thread(runnable, name);
		
		addToEntityThreadMap(entity, thread);
		incrementThreadCount();
		
		return thread;
	}
	
	public static Thread getThread(Runnable runnable, String name)
	{
		return getThread(runnable, name, "Others");
	}
	
	private static void incrementThreadCount()
	{
		threadCount++;
	}
	
	public static int totalThreadCount()
	{
		return threadCount;
	}
		
	private static synchronized void addToEntityThreadMap(String entity, Thread thread)
	{
		if (entityThreadMap.containsKey(entity))
		{
			List<Thread> threadList = entityThreadMap.get(entity);
			threadList.add(thread);
			//entityThreadMap.put(entity, threadList);
		}
		else
		{
			List<Thread> threadList = new ArrayList<Thread>();
			threadList.add(thread);
			entityThreadMap.put(entity, threadList);
		}
	}
	
	//might have to synchronize this
	public static String getEntityThreadCount(boolean hideStatusThreadCount)
	{
		StringBuilder sb = new StringBuilder();
		Iterator<Entry<String, List<Thread>>> it = entityThreadMap.entrySet().iterator();
	    while (it.hasNext()) 
	    {
	        Map.Entry<String, List<Thread>> pair = it.next();
	        if (!hideStatusThreadCount || (hideStatusThreadCount && !pair.getKey().contains("Status")))
	        {
	        	sb.append(pair.getKey() + " : " + pair.getValue().size());
	        	sb.append(", ");
	        }
	    }
	    
	    return sb.toString();
	}
	
	public static String getAliveThreadsCountPerEntity(boolean hideStatusThreadCount)
	{
		StringBuilder sb = new StringBuilder();
		int aliveThreadsCount = 0;
		Iterator<Entry<String, List<Thread>>> it = entityThreadMap.entrySet().iterator();
	    while (it.hasNext()) 
	    {
	        Map.Entry<String, List<Thread>> pair = it.next();
	        if (!hideStatusThreadCount || (hideStatusThreadCount && !pair.getKey().contains("Status")))
	        {
		        List<Thread> threadsList = pair.getValue();
		        
		        for(Thread thread : threadsList)
		        {
		        	if (thread.isAlive())
		        		aliveThreadsCount++;
		        }
		        	
		        sb.append(pair.getKey() + " : " + aliveThreadsCount);
		        sb.append(", ");
	        }
	        
	        aliveThreadsCount = 0; //reset to zero
	    }
	    
	    return sb.toString();
	}
	
	public static boolean areThreadsAlive(boolean ignoreStatusThreads)
	{
		Iterator<Entry<String, List<Thread>>> it = entityThreadMap.entrySet().iterator();
		
	    while (it.hasNext()) 
	    {
	        Map.Entry<String, List<Thread>> pair = it.next();
	        if (!ignoreStatusThreads || (ignoreStatusThreads && !pair.getKey().contains("Status")))
	        {
		        List<Thread> threadList = pair.getValue();
		        
		        for(Thread thread : threadList)
		        {
		        	if (thread.isAlive())
		        		return true;
		        }
	        }
	    }
	    
	    return false;
	}
	
	public static int getAliveThreadsCount(boolean hideStatusThreadCount)
	{
		int aliveThreadCount = 0;
		Iterator<Entry<String, List<Thread>>> it = entityThreadMap.entrySet().iterator();
		
	    while (it.hasNext()) 
	    {
	        Map.Entry<String, List<Thread>> pair = it.next();
	        if (!hideStatusThreadCount || (hideStatusThreadCount && !pair.getKey().contains("Status")))
	        {
		        List<Thread> threadList = pair.getValue();
		        
		        for(Thread thread : threadList)
		        {
		        	if (thread.isAlive())
		        		aliveThreadCount++;
		        }
	        }
	    }
	    
	    return aliveThreadCount;
	}
	
	public static String getThreadStatesCount(boolean hideStatusThreadCount)
	{
		HashMap<Thread.State, Integer> threadStatesCount = new HashMap<Thread.State, Integer>();
		threadStatesCount.put(Thread.State.BLOCKED, 0);
		threadStatesCount.put(Thread.State.NEW, 0);
		threadStatesCount.put(Thread.State.RUNNABLE, 0);
		threadStatesCount.put(Thread.State.TERMINATED, 0);
		threadStatesCount.put(Thread.State.TIMED_WAITING, 0);
		threadStatesCount.put(Thread.State.WAITING, 0);
		
		Iterator<Entry<String, List<Thread>>> it = entityThreadMap.entrySet().iterator();
	    while (it.hasNext()) 
	    {
	        Map.Entry<String, List<Thread>> pair = it.next();
	        if (!hideStatusThreadCount || (hideStatusThreadCount && !pair.getKey().contains("Status")))
	        {
		        List<Thread> threadsList = pair.getValue();
		        
		        for(Thread thread : threadsList)
		        {
		        	Thread.State state = thread.getState();
		        	int count = threadStatesCount.get(state);
		        	threadStatesCount.put(state, count + 1);
		        }
	        }
	    }
	    
	    return threadStatesCount.toString();
	}
}
