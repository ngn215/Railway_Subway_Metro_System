package Concrete;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import Factory.CustomLoggerFactory;
import Factory.LockFactory;
import LockerClasses.ReentrantLockerUnlocker;


public class Station extends ReentrantLockerUnlocker{
	
	private final String name;
	private final int numberOfPlatforms;
	private final ConcurrentHashMap<Integer, Train> trainPlatformMap;
	private final Set<Integer> availablePlatformsSet;
	private final Set<Person> personsInPlatformSet;
	//static int counter = 0;
	private final ReentrantReadWriteLock trainPlatformMapLock;
	private final ReentrantReadWriteLock availablePlatformsSetLock;
	private final ReentrantReadWriteLock personsInPlatformSetLock;
	private final AsynchronousLogger asyncLogger;
	
	public Station(String name, int numberOfPlatforms)
	{
		this.name = name;
		this.numberOfPlatforms = numberOfPlatforms;
		this.trainPlatformMap = new ConcurrentHashMap<Integer, Train>();
		this.availablePlatformsSet = Collections.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>());
		this.personsInPlatformSet = Collections.newSetFromMap(new ConcurrentHashMap<Person, Boolean>());
		
		this.trainPlatformMapLock = LockFactory.getReentrantReadWriteLockInstance(true);
		this.availablePlatformsSetLock = LockFactory.getReentrantReadWriteLockInstance(true);
		this.personsInPlatformSetLock = LockFactory.getReentrantReadWriteLockInstance(true);
		
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
			readLock(personsInPlatformSetLock);
			readLock(trainPlatformMapLock);
		
			System.out.println("STATION STATUS : " + this.name + ", Persons : " + this.personsInPlatformSet.size() + ", Trains : " + this.trainPlatformMap);
			//System.out.println("Persons counter : " + counter);
		}
		finally
		{
			readUnlock(personsInPlatformSetLock);
			readUnlock(trainPlatformMapLock);
		}
	}
	
	public void enterStation(Person person)
	{
		//if person has not yet reached destination then add to set
		if (this.name != person.getDestinationStation().name)
		{	
			try
			{
				readLock(personsInPlatformSetLock);
			
				asyncLogger.log("*** PERSON : " + person.getName() + " is entering " + name + " station. His destination is " + person.getDestinationStation().getName());
				personsInPlatformSet.add(person);
			}
			finally
			{
				readUnlock(personsInPlatformSetLock);
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
			readLock(personsInPlatformSetLock);
			
			//asyncLogger.log("Person : " + person.getName() + " is exiting station : " + name);
			personsInPlatformSet.remove(person);
		}
		finally
		{
			readUnlock(personsInPlatformSetLock);
		}
	}
	
	public void enterStationPlatform(Train train, int platformNumber)
	{
		try
		{
			//get both write locks here. because this step needs to update both structures to enter the platform
			readLock(availablePlatformsSetLock);
			readLock(trainPlatformMapLock);
			
			asyncLogger.log("Train : " + train.getName() + " entering station : " + name + " at platform : " + platformNumber);
		
			availablePlatformsSet.remove(platformNumber);
		
			trainPlatformMap.put(platformNumber, train);
		}
		finally
		{
			readUnlock(availablePlatformsSetLock);
			readUnlock(trainPlatformMapLock);
		}
	}
	
	public void exitStationPlatform(Train train)
	{
		int platformNumber = train.getCurrentPlatformNumber();
		try
		{
			//get both write locks here. because this step needs to update both structures to free the platform
			readLock(availablePlatformsSetLock);
			readLock(trainPlatformMapLock);
			//we also need persons in platform lock so that persons entering the train do not concurrently try to access set
			//when we are trying to iterate through set and interrupt the person threads
			writeLock(personsInPlatformSetLock);
			
			//System.out.println("Train : " + train.getName() + " exiting station : " + name + " " + System.currentTimeMillis());
			
			//Interrupting persons first
			for (Person person : personsInPlatformSet)
			{
				if (person.interruptThread())
				{
					asyncLogger.log("Station : " + this.name + " is " + "Interrupting person : " + person.getName() + " for train : " + train.getName());
				}
			}
			
			availablePlatformsSet.add(platformNumber);
			trainPlatformMap.remove(platformNumber);
		}
		finally
		{
			writeUnlock(personsInPlatformSetLock);
			readUnlock(trainPlatformMapLock);
			readUnlock(availablePlatformsSetLock);
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
			readLock(personsInPlatformSetLock);
			return personsInPlatformSet.size();
		}
		finally
		{
			readUnlock(personsInPlatformSetLock);
		}
	}
	
	public int checkPlatformAvailabilty()
	{
		Boolean isEmpty = null;
		Integer platformNumber = null;
		
		try
		{
			readLock(availablePlatformsSetLock);
			isEmpty = availablePlatformsSet.isEmpty();
		}
		finally
		{
			readUnlock(availablePlatformsSetLock);
		}
		
		if(!isEmpty)
		{
			try
			{
				writeLock(availablePlatformsSetLock);
				
				//check here again because another could have modified set by the time we obtain the writelock
				if (availablePlatformsSet.isEmpty())
				{
					return -1;
				}
					
				Iterator<Integer> iter = availablePlatformsSet.iterator();
				platformNumber = iter.next();
				iter.remove(); //removing platform from hashset
			}
			finally
			{
				writeUnlock(availablePlatformsSetLock);
			}
			
			return platformNumber;
		}
		
		return -1;
	}
	
	public String printListOfTrainsInPlatforms()
	{
		String str = "";
		
		try
		{
			readLock(trainPlatformMapLock);
			
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
			readUnlock(trainPlatformMapLock);
		}
		
		return str + "]";
	}
	
	public ConcurrentHashMap<Integer, Train> getTrainsInPlatforms()
	{
		ConcurrentHashMap<Integer, Train> trainPlatformMapClone;
		
		try
		{
			readLock(trainPlatformMapLock);
		
			trainPlatformMapClone = trainPlatformMap;//(HashMap<Integer, Train>) trainPlatformMap.clone();
		}
		finally
		{
			readUnlock(trainPlatformMapLock);
		}
		
		return trainPlatformMapClone;
	}
}
