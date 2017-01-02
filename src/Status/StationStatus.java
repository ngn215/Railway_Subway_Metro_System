package Status;

import java.util.List;

import Concrete.AsynchronousLogger;
import Concrete.Station;
import Factory.CustomLoggerFactory;
import Factory.ExecutorServiceFactory;
import Interface.CustomExecutorServiceInterface;
import Interface.StatusInterface;

public class StationStatus implements StatusInterface,Runnable,CustomExecutorServiceInterface{

	private final List<Station> stationsList;
	private final AsynchronousLogger asyncLogger;
	private int refreshIntervalms;
	private boolean shutDown;
	
	public StationStatus(List<Station> stationsList)
	{
		this.stationsList = stationsList;
		this.asyncLogger = CustomLoggerFactory.getAsynchronousLoggerInstance();
		this.shutDown = false;
	}
	
	@Override
	public void getStatus(int refreshIntervalms) {
		// TODO Auto-generated method stub
		
		setRefreshIntervalms(refreshIntervalms);
		
		ExecutorServiceFactory.createAndExecuteSingleThreadExecutor(this);
		Thread.currentThread().setName("StationsStatusThread");
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
	
				System.out.println("-----------------S-T-A-T-I-O-N---S-T-A-T-U-S-------------------");
				
				for(Station station : stationsList)
				{
					station.getStatus();
				}
				
				System.out.println("-------------------------------------------------------------------");
			}
		}
		catch (InterruptedException e) 
		{
			asyncLogger.log("Exception in StationStatus : " + e);
		}
		
	}

	@Override
	public void shutDown() {
		// TODO Auto-generated method stub
		shutDown = true;
	}	
}
