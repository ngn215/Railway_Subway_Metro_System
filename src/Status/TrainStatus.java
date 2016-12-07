package Status;

import java.util.ArrayList;
import java.util.List;

import Concrete.Train;

public class TrainStatus implements Runnable {

	List<Train> trains = new ArrayList<Train>();
	int refreshInterval;
	
	public TrainStatus(List<Train> trains, int refreshInterval)
	{
		this.trains = trains;
		this.refreshInterval = refreshInterval;
	}
	
	public void getTrainsStatus()
	{		
		Thread thread = new Thread(this, "StatusThread");
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
			
			for(Train train : trains)
			{
				train.getTrainStatus();
			}
			
			//below code can be moved to station and person status
			
			/*int countOfPeopleNotReachedDestination = 0;
			for(Person person : PersonFactory.getPersons())
			{
				if (!person.hasReachedDestination())
				{
					//person.getPersonStatus();
					countOfPeopleNotReachedDestination++;
				}
			}
			System.out.println("PERSON STATUS : number of persons yet to reach destination : " + countOfPeopleNotReachedDestination);*/
			
//			for(String station : StationFactory.getStationsArray())
//			{
//				StationFactory.getStationInstance(station).getStatus();
//			}
			
			System.out.println("-------------------------------------------------------------------");
		}
		
	}
	
}
