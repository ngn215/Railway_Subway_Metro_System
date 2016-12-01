package Concrete;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import LockerClasses.ReentrantLockerUnlocker;


public class Station extends ReentrantLockerUnlocker{
	
	private String name;
	int numberOfPlatforms;
	HashMap<Integer, Train> trainPlatformMap = new HashMap<Integer, Train>();
	HashSet<Integer> availablePlatformsSet = new HashSet<Integer>();
	HashSet<Person> personsInPlatformSet = new HashSet<Person>();
	//static int counter = 0;
	private static final ReentrantReadWriteLock trainPlatformMapLock = new ReentrantReadWriteLock(true);
	private static final ReentrantReadWriteLock availablePlatformsSetLock = new ReentrantReadWriteLock(true);
	private static final ReentrantReadWriteLock personsInPlatformSetLock = new ReentrantReadWriteLock(true);

	
	public Station(String name, int numberOfPlatforms)
	{
		this.name = name;
		this.numberOfPlatforms = numberOfPlatforms;
		
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
		
		System.out.println("STATION STATUS : " + this.name + " Persons : " + this.personsInPlatformSet.size() + " Trains : " + this.trainPlatformMap);
		//System.out.println("Persons counter : " + counter);
		
		readUnlock(personsInPlatformSetLock);
		readUnlock(trainPlatformMapLock);
	}
	
	public void enterStation(Person person)
	{
		//if person has not yet reached destination then add to set
		if (this.name != person.destinationStation.name)
		{	
			writeLock(personsInPlatformSetLock);
			
			//System.out.println("*** PERSON : " + person.name + " is entering " + name + " station. His destination is " + person.destinationStation);
			personsInPlatformSet.add(person);
			
			writeUnlock(personsInPlatformSetLock);
		}
		else //person has reached destination. remove him from set.
		{
			//System.out.println("*** PERSON : " + person.name + " has reached his destination " + person.destinationStation);
			exitStation(person);
			//counter++;
		}
	}
	
	public void exitStation(Person person)
	{
		writeLock(personsInPlatformSetLock);
		
		//System.out.println("*** PERSON : " + person.name + " is exiting station ");
		personsInPlatformSet.remove(person);
		
		writeUnlock(personsInPlatformSetLock);
	}
	
	public void enterStationPlatform(Train train, int platformNumber)
	{
		//get both write locks here. because this step needs to update both structures to enter the platform
		writeLock(availablePlatformsSetLock);
		writeLock(trainPlatformMapLock);
		
		//System.out.println("Train : " + train.getName() + " entering station : " + name + " " + System.currentTimeMillis());
		
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
		
		//System.out.println("Train : " + train.getName() + " exiting station : " + name + " " + System.currentTimeMillis());
		
		availablePlatformsSet.add(platformNumber);
		trainPlatformMap.remove(platformNumber);
		
		HashSet<Person> personsInPlatformSetClone = (HashSet<Person>) personsInPlatformSet.clone();
		for (Person person : personsInPlatformSetClone)
		{
			//only interrupt running threads
			if (person.getThread().getState() != Thread.State.WAITING)
			{
				System.out.println("Station : " + this.name + " is " + "Interrupting person : " + person.getName());
				person.getThread().interrupt();
			}
		}
		
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
	
	/*private void announce(Train train)
	{
		readLock(personsInPlatformSetLock);
		boolean isEmpty = personsInPlatformSet.isEmpty();
		readUnlock(personsInPlatformSetLock);
		
		//if there are people in platform
		if(!isEmpty)
		{
			writeLock(personsInPlatformSetLock);
			
			//check here again because another could have modified set by the time obtain the write lock.
			if (personsInPlatformSet.isEmpty())
			{
				writeUnlock(personsInPlatformSetLock);
				return;
			}
			
			Iterator iter = personsInPlatformSet.iterator();
			while(iter.hasNext())
			{
				Person person = (Person)iter.next();
				if (person.canTakeThisTrain(train))
				{
					if(train.enterTrain(person)) //if train has vacancy then this will return true
					{	
						person.enterTrain(train);
						iter.remove();
					}
					else //if there is no vacancy then break
						break;
				}
			}
			
			writeUnlock(personsInPlatformSetLock);
		}
		
	}*/
	
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
		
		HashMap<Integer, Train> trainPlatformMapClone = null;
		if(!trainPlatformMap.isEmpty())
		{
			trainPlatformMapClone = trainPlatformMap;//(HashMap<Integer, Train>) trainPlatformMap.clone();
		}
		
		readUnlock(trainPlatformMapLock);
		
		return trainPlatformMapClone;
	}
}
