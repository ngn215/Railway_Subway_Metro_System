package Status;

import Concrete.AsynchronousLogger;
import Factory.CustomLoggerFactory;
import Factory.CustomThreadFactory;
import Factory.ExecutorServiceFactory;
import Interface.CustomExecutorServiceInterface;
import Interface.StatusInterface;

public class ThreadStatus implements StatusInterface, Runnable, CustomExecutorServiceInterface {

	private final AsynchronousLogger asyncLogger;
	private int refreshIntervalms;
	private boolean shutDown;
	
	public ThreadStatus()
	{
		this.asyncLogger = CustomLoggerFactory.getAsynchronousLoggerInstance();
		this.shutDown = false;
	}
	
	@Override
	public void getStatus(int refreshIntervalms) {
		// TODO Auto-generated method stub
		
		setRefreshIntervalms(refreshIntervalms);
		
		ExecutorServiceFactory.createAndExecuteSingleThreadExecutor(this);
		Thread.currentThread().setName("ThreadStatusThread");		
	}

	@Override
	public void setRefreshIntervalms(int refreshIntervalms) {
		// TODO Auto-generated method stub
		
		this.refreshIntervalms = refreshIntervalms;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		try
		{
			while(CustomThreadFactory.areThreadsAlive(true) && !shutDown)
			{
				Thread.sleep(refreshIntervalms);
				
				System.out.println("-----------------T-H-R-E-A-D-----S-T-A-T-U-S-------------------");
				
				System.out.println("Alive Threads : " + CustomThreadFactory.getAliveThreadsCount(true));
				System.out.println("Alive Threads per entity : " + CustomThreadFactory.getAliveThreadsCountPerEntity(true));
				System.out.println("Threads States count : " + CustomThreadFactory.getThreadStatesCount(true));
				System.out.println("Total Threads per entity : " + CustomThreadFactory.getEntityThreadCount(true));
				
				System.out.println("-------------------------------------------------------------------");
			}
		}
		catch(InterruptedException e)
		{
			asyncLogger.log("Exception in ThreadStatus : " + e);
		}
		
	}

	@Override
	public void shutDown() {
		// TODO Auto-generated method stub
		shutDown = true;
	}
}