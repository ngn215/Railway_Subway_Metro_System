package Status;

import java.util.List;

import Concrete.AsynchronousLogger;
import Concrete.Train;
import Factory.CustomLoggerFactory;
import Factory.ExecutorServiceFactory;
import Interface.CustomExecutorServiceInterface;
import Interface.StatusInterface;

public class TrainStatus implements StatusInterface,Runnable,CustomExecutorServiceInterface {

	private final List<Train> trains;
	private final AsynchronousLogger asyncLogger;
	private int refreshIntervalms;
	private boolean shutDown;
	
	public TrainStatus(List<Train> trains)
	{
		this.trains = trains;
		this.asyncLogger = CustomLoggerFactory.getAsynchronousLoggerInstance();
		this.shutDown = false;
	}
	
	@Override
	public void getStatus(int refreshIntervalms) {
		// TODO Auto-generated method stub
		
		setRefreshIntervalms(refreshIntervalms);
		
		ExecutorServiceFactory.createAndExecuteSingleThreadExecutor(this);
		Thread.currentThread().setName("TrainStatusThread");		
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
			while(!shutDown)
			{
				Thread.sleep(refreshIntervalms);
				
				System.out.println("------------------------T-R-A-I-N---S-T-A-T-U-S-------------------------");
				
				for(Train train : trains)
				{
					//only get status for running trains
					if (train.isRunning())
						train.getTrainStatus();
				}
				
				System.out.println("-------------------------------------------------------------------");
			}
		}
		catch (InterruptedException e)
		{
			asyncLogger.log("Exeception in TrainStatus : " + e);
		}
		
	}

	@Override
	public void shutDown() {
		// TODO Auto-generated method stub
		shutDown = true;
	}	
}
