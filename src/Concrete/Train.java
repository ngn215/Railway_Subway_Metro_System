package Concrete;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import LockerClasses.ReentrantLockerUnlocker;


public class Train extends ReentrantLockerUnlocker implements Runnable {

	private String name;
	private Line line;
	private boolean directionUp;
	private int speed;
	Station currentStation;
	int currentPlatformNumber;
	int numberOfTrips;
	private final int capacity = 1000;
	HashSet<Person> personsSet;
	private boolean doorsOpen;
	private static final ReentrantReadWriteLock personsSetLock = new ReentrantReadWriteLock(true);
	private static final ReentrantReadWriteLock doorsLock = new ReentrantReadWriteLock(true);
	int noOfPeopleEnteringTrain = 0;
	int noOfPeopleExitingTrain = 0;
	
	public Train(String name, Line line, boolean directionUp, int speed)
	{
		this.name = name;
		this.line = line;
		this.directionUp = directionUp;
		this.speed = speed;
		
		this.personsSet = new HashSet<Person>();
		this.numberOfTrips = 0;
		//this.currentStation = line.getFirstStation(directionUp);

	}
	
	public String getName() {
		return name;
	}
	
	public String getLineName() {
		return line.name;
	}

	public String getCurrentStation()
	{
		return this.currentStation.getName();
	}	
	
	public boolean getDirection() 
	{
		return directionUp;
	}
	
	public int getCurrentPlatformNumber() 
	{
		return currentPlatformNumber;
	}
	
	public String getDirectionName()
	{
		return directionUp ? "^" : "v";
	}
	
	private boolean vacancyAvailable()
	{
		//readLock(personsSetLock);		
		int noOfPersons = personsSet.size();
		
		//if (personsSetLock.isWriteLockedByCurrentThread())
			//System.out.println("is write locked");
		
		//readUnlock(personsSetLock);
		//if (noOfPersons >= capacity)
			//System.out.println("vacancyAvailable() : " + noOfPersons + " " + (noOfPersons < capacity));
		
		return noOfPersons < capacity;
	}
	
	public int noOfVacanciesAvailable()
	{
		readLock(personsSetLock);
		int noOfPersons = personsSet.size();
		readUnlock(personsSetLock);
		
		return capacity - noOfPersons;
	}
	
	public int getNumberOfPersons()
	{
		readLock(personsSetLock);
		int noOfPersons = personsSet.size();
		readUnlock(personsSetLock);
		
		return noOfPersons;
	}
	
	public void reverseDirection()
	{
		//System.out.println("--- " + this.getName() + " Reversing Direction" + " ---");
		directionUp = !directionUp;
	}
	
	public void moveTo(Station nextStation, int platformNumber)
	{
		//freeing platform
		if (currentStation != null)
			currentStation.exitStationPlatform(this);
		
		//moving to platform
		nextStation.enterStationPlatform(this, platformNumber);
		
		//setting current station
		currentStation = nextStation;
	}
	
	public void getTrainStatus()
	{
		System.out.println("TRAIN STATUS : " + name + " " + getDirectionName() + " " + getCurrentStation() + "\t Persons count : " + getNumberOfPersons() + "\t TripNumber : " + numberOfTrips);
	}
	
	public void openDoors()
	{	
		writeLock(doorsLock);
		
		//System.out.println("Train : " + name + " openining doors" + " " + System.currentTimeMillis());
		doorsOpen = true;
		
		writeUnlock(doorsLock);
	}
	
	public void closeDoors()
	{	
		writeLock(doorsLock);
		
		//System.out.println("Train : " + name + " closing doors" + " " + System.currentTimeMillis());
		doorsOpen = false;
		
		writeUnlock(doorsLock);
	} 
	
	private boolean areDoorsOpen()
	{
		//readLock(doorsLock);
		
		//boolean status = doorsOpen;
		
		//readUnlock(doorsLock);
		
		return doorsOpen;
	}
	
	public void announce()
	{
		//int numberOfPersonsBefore = getNumberOfPersons();
		
		//announcement for persons inside train
		synchronized(this)
		{
			//System.out.println("Train : " + name + " announcing to all passengers in train"  + " " + System.currentTimeMillis());
			this.notifyAll();
		}
		
		//int numberOfPersonsAfter = getNumberOfPersons();
		//System.out.println("Train : " + name + " Number of Persons Before : " + numberOfPersonsBefore 
			//				+ " Number of Persons After : " + numberOfPersonsAfter);
	}
	
	public void readyForIntake()
	{
		currentStation.trainReadyForPeopleIntake();
	}
	
	public boolean exitTrain(Person person)
	{		
		try
		{						
			writeLock(doorsLock);
			writeLock(personsSetLock);
			
			if (!areDoorsOpen())
			{
				//System.out.println("Doors are locked." + " Person(" + person.name + ")");
				return false;
			}
			
			if (!person.destinationStation.getName().equals(currentStation.getName()))
				System.out.println("ERR : Person is getting out at wrong station.");
			
			//exit from train
			personsSet.remove(person);
			
			//person exits train and enters destination station
			currentStation.enterStation(person);
			
			noOfPeopleExitingTrain++;
		}
		catch(Exception e)
		{
			System.out.println("Exception : " + e);
		}
		finally
		{
			writeUnlock(personsSetLock);
			writeUnlock(doorsLock);
		}
		
		return true;
	}
	
	public boolean enterTrain(Person person)
	{		
		try
		{			
			writeLock(doorsLock);
			writeLock(personsSetLock);
			
			if (!vacancyAvailable() || !areDoorsOpen()) //check if vacancy available and doors are open else break;
			{
				//System.out.println("no vacancy or doors are closed" + " Person (" + person.name + ")");
				return false;
			}
			
			//exit from station
			currentStation.exitStation(person);
			
			//now enter train
			personsSet.add(person);
			
			noOfPeopleEnteringTrain++;
		}
		catch(Exception e)
		{
			System.out.println("Exception : " + e);
		}
		finally
		{
			writeUnlock(personsSetLock);
			writeUnlock(doorsLock);
		}
		
		return true;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		numberOfTrips = 1;
		Station nextStation;
		
		while(true)
		{		
			if (currentStation != null)
				nextStation = line.getNextStation(currentStation, directionUp);
			else
				nextStation = line.getFirstStation(directionUp);
			
			int platformNumber = nextStation.checkPlatformAvailabilty();
			
			while (platformNumber == -1)
			{
				System.out.println("*** Station : " + nextStation.getName() + " not available...." + this.name + " waiting..." + " " + nextStation.printListOfTrainsInPlatforms());
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				platformNumber = nextStation.checkPlatformAvailabilty();
				
				if (platformNumber != -1)
					System.out.println("*** Found Empty platform : "+ platformNumber + " for " + this.name);
			}
			
			moveTo(nextStation, platformNumber);
			
			//we need to reverse direction here because otherwise people will never know the correct direction of the train
			//when the train reaches last stop (churchgate, dahanu road etc.) and reverses direction
			if (line.isLastStation(nextStation, directionUp))
			{
				this.reverseDirection();		
				
				//stop after 10 trips
				if (numberOfTrips == 10)
				{
					tripComplete();
					break;
				}
				
				numberOfTrips++;
			}
			
			//int numberOfExistingPersonsInTrain = getNumberOfPersons();
			
			//open train doors
			openDoors();
			
			//announcement for passengers in train
			announce();
			
			//signal for station that train is ready for intake
			readyForIntake();
			
			//wait for people to enter train
			try {
				Thread.sleep(200);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			//close train doors
			closeDoors();
			
			//System.out.println("No Of People in train " + name + " : " + numberOfExistingPersonsInTrain + " \t" + noOfPeopleEnteringTrain + "<- " + noOfPeopleExitingTrain + "-> " + " at station " + this.getCurrentStation());
			//noOfPeopleEnteringTrain = 0;
			//noOfPeopleExitingTrain = 0;
			
			//System.out.println(this.getName() + " " + this.getDirection() + " "+ this.getCurrentStation());
			try {
				Thread.sleep(speed);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			currentPlatformNumber = platformNumber;
		}
		
	}
	
	public void tripComplete()
	{
		System.out.println("*** TRIP COMPLETED : " + name + " " + getDirectionName());
	}
	
}
