package Status;

import java.util.ArrayList;
import java.util.List;

import Concrete.Station;

public class StationStatus implements Runnable{

	private List<Station> stationsList = new ArrayList<Station>();
	private int refreshInterval;
	
	public StationStatus(List<Station> stationsList, int refreshInterval)
	{
		this.stationsList = stationsList;
		this.refreshInterval = refreshInterval;
	}
	
	public void getStationsStatus()
	{		
		Thread thread = new Thread(this, "StationsStatusThread");
		thread.start();
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		while(true)
		{
			try {
				Thread.sleep(refreshInterval);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.println("---------------------S-T-A-T-U-S-------------------------------------");
			
			for(Station station : stationsList)
			{
				station.getStatus();
			}
			
			System.out.println("-------------------------------------------------------------------");
		}
	}

	
}
