package Status;

import java.util.Arrays;
import java.util.List;

import Concrete.AsynchronousLogger;
import Concrete.Station;
import Factory.CustomLoggerFactory;
import Factory.CustomThreadFactory;
import Interface.StatusInterface;

public class StationStatus implements StatusInterface,Runnable{

	private final List<Station> stationsList;
	private final AsynchronousLogger asyncLogger;
	private int refreshIntervalms;
	
	public StationStatus(List<Station> stationsList)
	{
		this.stationsList = stationsList;
		this.asyncLogger = CustomLoggerFactory.getAsynchronousLoggerInstance();
	}
	
	@Override
	public void getStatus(int refreshIntervalms) {
		// TODO Auto-generated method stub
		
		setRefreshIntervalms(refreshIntervalms);
		
		Thread thread = CustomThreadFactory.getThread(this, "StationsStatusThread", "StationsStatus");
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
		
		while(true)
		{
			try {
				Thread.sleep(refreshIntervalms);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				asyncLogger.log(Arrays.toString(e.getStackTrace()));
			}
			
			System.out.println("-----------------S-T-A-T-I-O-N---S-T-A-T-U-S-------------------");
			
			for(Station station : stationsList)
			{
				station.getStatus();
			}
			
			System.out.println("-------------------------------------------------------------------");
		}
	}	
}
