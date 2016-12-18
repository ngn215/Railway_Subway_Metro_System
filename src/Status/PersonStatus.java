package Status;

import java.util.ArrayList;
import java.util.List;

import Concrete.Person;
import Concrete.Station;
import Interface.StatusInterface;

public class PersonStatus implements StatusInterface,Runnable{

	private List<Person> personsList = new ArrayList<Person>();
	private int refreshIntervalms;
	
	public PersonStatus(List<Person> personsList)
	{
		this.personsList = personsList;
	}
	
	@Override
	public void getStatus(int refreshIntervalms) {
		// TODO Auto-generated method stub
		
		setRefreshIntervalms(refreshIntervalms);
		
		Thread thread = new Thread(this, "PersonStatusThread");
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
			
			System.out.println("------------------P-E-R-S-O-N---S-T-A-T-U-S---------------------");
			
			int numberOfPeopleYetToReachDestination = 0;
			for(Person person : personsList)
			{
				if(!person.hasReachedDestination())
					numberOfPeopleYetToReachDestination++;
			}
			
			System.out.println("Number of people yet to reach destination : " + numberOfPeopleYetToReachDestination);
			
			System.out.println("-------------------------------------------------------------------");
		}
	}
}
