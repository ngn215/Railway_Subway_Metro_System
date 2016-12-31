package Concrete;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import Factory.CustomLoggerFactory;
import Factory.LockFactory;
import LockerClasses.ReentrantLockerUnlocker;


public class Station extends ReentrantLockerUnlocker{
	
	private String name;
	private int numberOfPlatforms;
	private HashMap<Integer, Train> trainPlatformMap = new HashMap<Integer, Train>();
	private HashSet<Integer> availablePlatformsSet = new HashSet<Integer>();
	private HashSet<Person> personsInPlatformSet = new HashSet<Person>();
	//static int counter = 0;
	private final ReentrantReadWriteLock trainPlatformMapLock;
	private final ReentrantReadWriteLock availablePlatformsSetLock;
	private final ReentrantReadWriteLock personsInPlatformSetLock;
	private final AsynchronousLogger asyncLogger;
	
	public Station(String name, int numberOfPlatforms)
	{
		this.name = name;
		this.numberOfPlatforms = numberOfPlatforms;
		
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
		readLock(personsInPlatformSetLock);
		readLock(trainPlatformMapLock);
		
		System.out.println("STATION STATUS : " + this.name + ", Persons : " + this.personsInPlatformSet.size() + ", Trains : " + this.trainPlatformMap);
		//System.out.println("Persons counter : " + counter);
		
		readUnlock(personsInPlatformSetLock);
		readUnlock(trainPlatformMapLock);
	}
	
	public void enterStation(Person person)
	{
		//if person has not yet reached destination then add to set
		if (this.name != person.getDestinationStation().name)
		{	
			writeLock(personsInPlatformSetLock);
			
			asyncLogger.log("*** PERSON : " + person.getName() + " is entering " + name + " station. His destination is " + person.getDestinationStation().getName());
			personsInPlatformSet.add(person);
			
			writeUnlock(personsInPlatformSetLock);
		}
		else //person has reached destination. remove him from set.
		{
			asyncLogger.log("Person : " + person.getName() + " has reached his destination " + person.getDestinationStation().getName());
			exitStation(person);
			//counter++;
		}
	}
	
	public void exitStation(Person person)
	{
		writeLock(personsInPlatformSetLock);
		
		asyncLogger.log("Person : " + person.getName() + " is exiting station : " + name);
		personsInPlatformSet.remove(person);
		
		writeUnlock(personsInPlatformSetLock);
	}
	
	public void enterStationPlatform(Train train, int platformNumber)
	{
		//get both write locks here. because this step needs to update both structures to enter the platform
		writeLock(availablePlatformsSetLock);
		writeLock(trainPlatformMapLock);
		
		asyncLogger.log("Train : " + train.getName() + " entering station : " + name);
		
		availablePlatformsSet.remove(platformNumber);
		writeUnlock(availablePlatformsSetLock);
		
		trainPlatformMap.put(platformNumber, train);
		writeUnlock(trainPlatformMapLock);
	}
	
	public void exitStationPlatform(Train train)
	{
		int platformNumber = train.getCurrentPlatformNumber();
		
		//get both write locks here. because this step needs to update both structures to free the platform
		writeLock(availablePlatformsSetLock);
		writeLock(trainPlatformMapLock);
		//we also need persons in platform lock so that persons entering the train do not concurrently try to access set
		//when we are trying to iterate through set and interrupt the person threads
		writeLock(personsInPlatformSetLock);
		
		//System.out.println("Train : " + train.getName() + " exiting station : " + name + " " + System.currentTimeMillis());
		
		//Interrupting persons first
		for (Person person : personsInPlatformSet)
		{
			//only interrupt running threads
			if (person.getThread().getState() != Thread.State.WAITING)
			{
				asyncLogger.log("Station : " + this.name + " is " + "Interrupting person : " + person.getName() + " for train : " + train.getName());
				person.getThread().interrupt();
			}
		}
		
		availablePlatformsSet.add(platformNumber);
		trainPlatformMap.remove(platformNumber);
		
		writeUnlock(personsInPlatformSetLock);
		writeUnlock(trainPlatformMapLock);
		writeUnlock(availablePlatformsSetLock);
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
		readLock(personsInPlatformSetLock);
		int size = personsInPlatformSet.size();
		readUnlock(personsInPlatformSetLock);
		
		return size;
	}
	
	public int checkPlatformAvailabilty()
	{
		readLock(availablePlatformsSetLock);
		boolean isEmpty = availablePlatformsSet.isEmpty();
		readUnlock(availablePlatformsSetLock);
		
		if(!isEmpty)
		{
			writeLock(availablePlatformsSetLock);
			
			//check here again because another could have modified set by the time we obtain the writelock
			if (availablePlatformsSet.isEmpty())
			{
				writeUnlock(availablePlatformsSetLock);
				return -1;
			}
				
			Iterator<Integer> iter = availablePlatformsSet.iterator();
			int platformNumber = iter.next();
			iter.remove(); //removing platform from hashset
			
			writeUnlock(availablePlatformsSetLock);
			
			return platformNumber;
		}
		
		return -1;
	}
	
	public String printListOfTrainsInPlatforms()
	{
		readLock(trainPlatformMapLock);
		
		String str = "[";
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
		
		readUnlock(trainPlatformMapLock);
		
		return str + "]";
	}
	
	public HashMap<Integer, Train> getTrainsInPlatforms()
	{
		readLock(trainPlatformMapLock);
		
		HashMap<Integer, Train> trainPlatformMapClone = trainPlatformMap;//(HashMap<Integer, Train>) trainPlatformMap.clone();
		
		readUnlock(trainPlatformMapLock);
		
		return trainPlatformMapClone;
	}
}
