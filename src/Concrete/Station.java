package Concrete;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import Factory.CustomLoggerFactory;


public class Station {
	
	private final String name;
	private final int numberOfPlatforms;
	private final ConcurrentHashMap<Integer, Train> trainPlatformMap;
	private final Set<Integer> availablePlatformsSet;
	private final Set<Person> personsInPlatformSet;
	//static int counter = 0;
	private final AsynchronousLogger asyncLogger;
	
	public Station(String name, int numberOfPlatforms)
	{
		this.name = name;
		this.numberOfPlatforms = numberOfPlatforms;
		this.trainPlatformMap = new ConcurrentHashMap<Integer, Train>();
		this.availablePlatformsSet = Collections.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>());
		this.personsInPlatformSet = Collections.newSetFromMap(new ConcurrentHashMap<Person, Boolean>());
		
		this.asyncLogger = CustomLoggerFactory.getAsynchronousLoggerInstance();
		
		initializeAvailablePlatformsHashMap();
	}
	
	private void initializeAvailablePlatformsHashMap()
	{	
		for(int i=0; i<numberOfPlatforms; i++)
		{
			availablePlatformsSet.add(i + 1);
		}
	}
	
	public String getName()
	{
		return name;
	}
	
	public void getStatus()
	{
		try
		{		
			System.out.println("STATION STATUS : " + this.name + ", Persons : " + this.personsInPlatformSet.size() + ", Trains : " + this.trainPlatformMap);
			//System.out.println("Persons counter : " + counter);
		}
		finally
		{
			
		}
	}
	
	public void enterStation(Person person)
	{
		//if person has not yet reached destination then add to set
		if (this.name != person.getDestinationStation().name)
		{	
			try
			{		
				asyncLogger.log("*** PERSON : " + person.getName() + " is entering " + name + " station. His destination is " + person.getDestinationStation().getName());
				personsInPlatformSet.add(person);
			}
			finally
			{
				
			}
		}
		else //person has reached destination. remove him from set.
		{
			asyncLogger.log("Person : " + person.getName() + " is entering station and has reached his destination : " + person.getDestinationStation().getName());
			exitStation(person);
			//counter++;
		}
	}
	
	public void exitStation(Person person)
	{
		try
		{		
			//asyncLogger.log("Person : " + person.getName() + " is exiting station : " + name);
			personsInPlatformSet.remove(person);
		}
		finally
		{
			
		}
	}
	
	public void enterStationPlatform(Train train, int platformNumber)
	{
		try
		{
			asyncLogger.log("Train : " + train.getName() + " entering station : " + name + " at platform : " + platformNumber);
			
			//we dont need to perform below step of removal from availablePlatformsSet because when train enters platform,
			//method checkPlatformAvailabilty() has already allocated it a platform and remove it from set.
			//availablePlatformsSet.remove(platformNumber);
			
			trainPlatformMap.put(platformNumber, train);
		}
		finally
		{
			//do nothing
		}
	}
	
	public void exitStationPlatform(Train train)
	{
		int platformNumber = train.getCurrentPlatformNumber();
		try
		{
			asyncLogger.log("Train : " + train.getName() + " is exiting station : " + name);
			
			//we want below code in synchronized block because we need to get up to date info of available platforms
			//this can be used to allocate platforms to other trains.
			synchronized(availablePlatformsSet)
			{
				availablePlatformsSet.add(platformNumber);
			}
			
			trainPlatformMap.remove(platformNumber);
		}
		finally
		{
			
		}
	}
	
	//train calls this method after closing doors to make sure person threads are interrupted.
	public void stopPersonsFromEnteringTrain(Train train)
	{
		for (Person person : personsInPlatformSet)
		{
			if (person.interruptThread(train))
			{
				asyncLogger.log("Station : " + this.name + " is Interrupting person : " + person.getName() + " for train : " + train.getName());
			}
		}
	}
	
	public void trainReadyForPeopleIntake()
	{
		announce();
	}
	
	private void announce()
	{
		synchronized(this)
		{
			//System.out.println("Station : " + name + " announcement to persons on platform" + " " + System.currentTimeMillis());
			this.notifyAll();
		}
	}
	
	public int noOfPersonsInStation()
	{
		try
		{
			return personsInPlatformSet.size();
		}
		finally
		{
			
		}
	}
	
	public int checkPlatformAvailabilty()
	{
		Boolean isEmpty = null;
		Integer platformNumber = null;
		
		try
		{
			isEmpty = availablePlatformsSet.isEmpty();
		}
		finally
		{
			//do nothing
		}
		
		if(!isEmpty)
		{
			try
			{
				//synchronizing below block on availablePlatformsSet because we dont want multiple train
				//threads to enter this block and get the same free platform number
				synchronized(availablePlatformsSet)
				{
					//check here again because another could have modified set by the time we obtain the lock on object :availablePlatformsSet
					if (availablePlatformsSet.isEmpty())
					{
						return -1;
					}
						
					Iterator<Integer> iter = availablePlatformsSet.iterator();
					platformNumber = iter.next();
					iter.remove(); //removing platform from hashset
					
					return platformNumber;
				}
			}
			finally
			{
				//do nothing
			}
		}
		
		return -1;
	}
	
	public String printListOfTrainsInPlatforms()
	{
		String str = "";
		
		try
		{		
			str = "[";
			if(!trainPlatformMap.isEmpty())
			{
				Iterator<Entry<Integer, Train>> iter = trainPlatformMap.entrySet().iterator();
				while (iter.hasNext()) 
				{
					Map.Entry<Integer, Train> pair = (Map.Entry<Integer, Train>)iter.next();
					String trainName = pair.getValue().getName();
					str += pair.getKey() + " : " + trainName + ", ";
				}
			}
		}
		finally
		{
			//do nothing
		}
		
		return str + "]";
	}
	
	public ConcurrentHashMap<Integer, Train> getTrainsInPlatforms()
	{
		ConcurrentHashMap<Integer, Train> trainPlatformMapClone;
		
		try
		{	
			trainPlatformMapClone = trainPlatformMap;//(HashMap<Integer, Train>) trainPlatformMap.clone(); //return clone
			return trainPlatformMapClone;
		}
		finally
		{
			//do nothing
		}
	}
}
