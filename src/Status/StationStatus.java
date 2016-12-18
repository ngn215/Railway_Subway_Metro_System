package Status;

import java.util.ArrayList;
import java.util.List;

import Concrete.Station;
import Interface.StatusInterface;

public class StationStatus implements StatusInterface,Runnable{

	private List<Station> stationsList = new ArrayList<Station>();
	private int refreshIntervalms;
	
	public StationStatus(List<Station> stationsList, int refreshInterval)
	{
		this.stationsList = stationsList;
	}
	
	@Override
	public void getStatus(int refreshIntervalms) {
		// TODO Auto-generated method stub
		
		setRefreshIntervalms(refreshIntervalms);
		
		Thread thread = new Thread(this, "StationsStatusThread");
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
