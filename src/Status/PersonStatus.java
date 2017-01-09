package Status;

import java.util.HashMap;
import java.util.List;

import Concrete.AsynchronousLogger;
import Concrete.Line;
import Concrete.Person;
import Factory.CustomLoggerFactory;
import Factory.ExecutorServiceFactory;
import Factory.LineFactory;
import Interface.CustomExecutorServiceInterface;
import Interface.StatusInterface;

public class PersonStatus implements StatusInterface,Runnable, CustomExecutorServiceInterface{

	private final List<Person> personsList;
	private final AsynchronousLogger asyncLogger;
	private int refreshIntervalms;
	private boolean shutDown;
	
	public PersonStatus(List<Person> personsList)
	{
		this.personsList = personsList;
		this.asyncLogger = CustomLoggerFactory.getAsynchronousLoggerInstance();
		this.shutDown = false;
	}
	
	@Override
	public void getStatus(int refreshIntervalms) {
		// TODO Auto-generated method stub
		
		setRefreshIntervalms(refreshIntervalms);
		
		ExecutorServiceFactory.createAndExecuteSingleThreadExecutor(this);
		Thread.currentThread().setName("PersonStatusThread");
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
		
		try
		{
			while(!shutDown)
			{
				numberOfPeopleYetToReachDestination = 0;
				linePeopleMap.clear();
				
				Thread.sleep(refreshIntervalms);
				
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
						String lineName = person.getTrainLineName();
						
						linePeopleMap.put(lineName, linePeopleMap.get(lineName) + 1);
						numberOfPeopleYetToReachDestination++;
					}
				}
				
				System.out.println("Number of people yet to reach destination : " + numberOfPeopleYetToReachDestination);
				System.out.println(linePeopleMap.toString());
				
				System.out.println("-------------------------------------------------------------------");
			}
		}
		catch(InterruptedException e)
		{
			asyncLogger.log("Exception in PersonStatus : " + e);
		}
	}

	@Override
	public void shutDown() {
		// TODO Auto-generated method stub
		shutDown = true;
	}
}
