package Status;

import java.util.Arrays;

import Concrete.AsynchronousLogger;
import Factory.CustomLoggerFactory;
import Factory.CustomThreadFactory;
import Interface.StatusInterface;

public class ThreadStatus implements StatusInterface, Runnable {

	private int refreshIntervalms;
	private final AsynchronousLogger asyncLogger;
	
	public ThreadStatus()
	{
		this.asyncLogger = CustomLoggerFactory.getAsynchronousLoggerInstance();
	}
	
	@Override
	public void getStatus(int refreshIntervalms) {
		// TODO Auto-generated method stub
		
		setRefreshIntervalms(refreshIntervalms);
		
		Thread thread = CustomThreadFactory.getThread(this, "ThreadStatusThread", "ThreadStatus");
		thread.start();
		
	}

	@Override
	public void setRefreshIntervalms(int refreshIntervalms) {
		// TODO Auto-generated method stub
		
		this.refreshIntervalms = refreshIntervalms;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		while(CustomThreadFactory.areThreadsAlive(true))
		{
			try {
				Thread.sleep(refreshIntervalms);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				asyncLogger.log(Arrays.toString(e.getStackTrace()));
			}
			
			System.out.println("-----------------T-H-R-E-A-D-----S-T-A-T-U-S-------------------");
			
			System.out.println("Alive Threads : " + CustomThreadFactory.getAliveThreadsCount(true));
			System.out.println("Alive Threads per entity : " + CustomThreadFactory.getAliveThreadsCountPerEntity(true));
			System.out.println("Threads States count : " + CustomThreadFactory.getThreadStatesCount(true));
			System.out.println("Total Threads per entity : " + CustomThreadFactory.getEntityThreadCount(true));
			
			System.out.println("-------------------------------------------------------------------");
		}
		
	}

}
