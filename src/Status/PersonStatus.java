package Status;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import Concrete.AsynchronousLogger;
import Concrete.Line;
import Concrete.Person;
import Factory.CustomLoggerFactory;
import Factory.CustomThreadFactory;
import Factory.LineFactory;
import Interface.StatusInterface;

public class PersonStatus implements StatusInterface,Runnable{

	private final List<Person> personsList;
	private final AsynchronousLogger asyncLogger;
	private int refreshIntervalms;
	
	public PersonStatus(List<Person> personsList)
	{
		this.personsList = personsList;
		this.asyncLogger = CustomLoggerFactory.getAsynchronousLoggerInstance();
	}
	
	@Override
	public void getStatus(int refreshIntervalms) {
		// TODO Auto-generated method stub
		
		setRefreshIntervalms(refreshIntervalms);
		
		Thread thread = CustomThreadFactory.getThread(this, "PersonStatusThread", "PersonStatus");
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
		
		int numberOfPeopleYetToReachDestination;
		HashMap<String, Integer> linePeopleMap = new HashMap<String, Integer>();
		
		while(true)
		{
			numberOfPeopleYetToReachDestination = 0;
			linePeopleMap.clear();
			
			try {
				Thread.sleep(refreshIntervalms);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				asyncLogger.log(Arrays.toString(e.getStackTrace()));
			}
			
			System.out.println("------------------P-E-R-S-O-N---S-T-A-T-U-S---------------------");
			
			//populating map with zero values
			for(Line line : LineFactory.getLinesList())
			{
				linePeopleMap.put(line.getName(), 0);
			}
			
			for(Person person : personsList)
			{
				if(!person.hasReachedDestination())
				{
					String lineName = person.getTrainLine();
					
					linePeopleMap.put(lineName, linePeopleMap.get(lineName) + 1);
					numberOfPeopleYetToReachDestination++;
				}
			}
			
			System.out.println("Number of people yet to reach destination : " + numberOfPeopleYetToReachDestination);
			System.out.println(linePeopleMap.toString());
			
			System.out.println("-------------------------------------------------------------------");
		}
	}
}
