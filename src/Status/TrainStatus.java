package Status;

import java.util.ArrayList;
import java.util.List;

import Concrete.Train;
import Factory.CustomThreadFactory;
import Factory.TrainFactory;
import Interface.StatusInterface;

public class TrainStatus implements StatusInterface,Runnable {

	private List<Train> trains = new ArrayList<Train>();
	private int refreshIntervalms;
	
	public TrainStatus(List<Train> trains)
	{
		this.trains = trains;
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
