package Status;

import java.util.Arrays;
import java.util.List;

import Concrete.AsynchronousLogger;
import Concrete.Train;
import Factory.CustomLoggerFactory;
import Factory.CustomThreadFactory;
import Factory.TrainFactory;
import Interface.StatusInterface;

public class TrainStatus implements StatusInterface,Runnable {

	private final List<Train> trains;
	private final AsynchronousLogger asyncLogger;
	private int refreshIntervalms;
	
	public TrainStatus(List<Train> trains)
	{
		this.trains = trains;
		this.asyncLogger = CustomLoggerFactory.getAsynchronousLoggerInstance();
	}
	
	@Override
	public void getStatus(int refreshIntervalms) {
		// TODO Auto-generated method stub
		
		setRefreshIntervalms(refreshIntervalms);
		
		Thread thread = CustomThreadFactory.getThread(this, "TrainStatusThread", "TrainStatus");
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
		
		while(TrainFactory.areTrainsRunning())
		{
			try {
				Thread.sleep(refreshIntervalms);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				asyncLogger.log(Arrays.toString(e.getStackTrace()));
			}
			
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
}
