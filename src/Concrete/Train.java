package Concrete;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import Factory.CustomLoggerFactory;
import Factory.CustomThreadFactory;
import LockerClasses.ReentrantLockerUnlocker;


public class Train extends ReentrantLockerUnlocker implements Runnable {

	private String name;
	private Line line;
	private boolean directionUp;
	private int speed;
	private Station currentStation;
	private int currentPlatformNumber;
	private int numberOfTrips;
	private final int capacity = 1000;
	private HashSet<Person> personsSet;
	private boolean doorsOpen;
	private static final ReentrantReadWriteLock personsSetLock = new ReentrantReadWriteLock(true);
	private static final ReentrantReadWriteLock doorsLock = new ReentrantReadWriteLock(true);
	private int noOfPeopleEnteringTrain;
	private int noOfPeopleExitingTrain;
	private final Thread thread;
	private final AsynchronousLogger asyncLogger;
	
	public Train(String name, Line line, boolean directionUp, int speed)
	{
		this.name = name;
		this.line = line;
		this.directionUp = directionUp;
		this.speed = speed;
		
		this.personsSet = new HashSet<Person>();
		this.numberOfTrips = 0;
		//this.currentStation = line.getFirstStation(directionUp);
		this.thread = CustomThreadFactory.getThread(this, "T" + name, "Train");//new Thread(this, "T" + name);
		this.noOfPeopleEnteringTrain = 0;
		this.noOfPeopleExitingTrain = 0;
		this.asyncLogger = CustomLoggerFactory.getAsynchronousLoggerInstance();

	}
	
	public String getName() {
		return name;
	}

	public String getLineName() {
		return line.name;
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
	
	public String getCurrentStationName() 
	{
		return currentStation.getName();
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
		int noOfPersons = 0;
		
		try
		{
			readLock(personsSetLock);
			noOfPersons = personsSet.size();
		}
		finally
		{
			readUnlock(personsSetLock);
		}		
		
		return capacity - noOfPersons;
	}
	
	public int getNumberOfPersons()
	{
		int noOfPersons = 0;
		
		try
		{
			readLock(personsSetLock);
			noOfPersons = personsSet.size();
		}
		finally
		{
			readUnlock(personsSetLock);
		}
		
		return noOfPersons;
	}
	
	public void reverseDirection()
	{
		asyncLogger.log("--- " + name + " Reversing Direction" + " ---");
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
		System.out.println("TRAIN STATUS : " + name + " " + getDirectionName() + " " + getCurrentStationName() + "\t Persons count : " + getNumberOfPersons() + "\t TripNumber : " + numberOfTrips);
	}
	
	public void openDoors()
	{	
		try
		{
			writeLock(doorsLock);
			
			//System.out.println("Train : " + name + " openining doors" + " " + System.currentTimeMillis());
			doorsOpen = true;
		}
		finally
		{		
			writeUnlock(doorsLock);
		}
	}
	
	public void closeDoors()
	{	
		try
		{
			writeLock(doorsLock);
		
			//asyncLogger.log("Train : " + this.name + " closing doors");
			doorsOpen = false;
		}
		finally
		{
			writeUnlock(doorsLock);
		}
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
			
			if (!person.getDestinationStation().getName().equals(getCurrentStationName()))
			{
				asyncLogger.log("ERR : Person is getting out at wrong station.", true);
			}
			
			//exit from train
			personsSet.remove(person);
			
			//person exits train and enters destination station
			currentStation.enterStation(person);
			
			noOfPeopleExitingTrain++;
			
			asyncLogger.log("Person : " + person.getName() + " exiting train : " + this.name + " at station : " + getCurrentStationName());
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
			
			asyncLogger.log("Person : " + person.getName() + " entering train : " + this.name + " from station : " + getCurrentStationName());
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
				asyncLogger.log("*** Station : " + nextStation.getName() + " not available...." + this.name + " waiting..." + " " + nextStation.printListOfTrainsInPlatforms(), true);
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					asyncLogger.log(Arrays.toString(e.getStackTrace()));
				}
				
				platformNumber = nextStation.checkPlatformAvailabilty();
				
				if (platformNumber != -1)
					asyncLogger.log("*** Found Empty platform : "+ platformNumber + " for " + this.name, true);
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
			
			int numberOfExistingPersonsInTrain = getNumberOfPersons();
			
			//open train doors
			openDoors();
			
			//announcement for passengers in train
			announce();
			
			//signal for station that train is ready for intake
			readyForIntake();
			
			//wait for people to enter train
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				asyncLogger.log(Arrays.toString(e.getStackTrace()));
			}
			
			//close train doors
			closeDoors();
			
			asyncLogger.log("No Of People in train " + name + " : " + numberOfExistingPersonsInTrain 
							+ " \t" + noOfPeopleEnteringTrain + "<- " + noOfPeopleExitingTrain + "-> " 
							+ " at station " + getCurrentStationName());
			
			//reset values
			noOfPeopleEnteringTrain = 0;
			noOfPeopleExitingTrain = 0;
			
			//System.out.println(this.getName() + " " + this.getDirection() + " "+ this.getCurrentStation());
			try {
				Thread.sleep(speed);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				asyncLogger.log(Arrays.toString(e.getStackTrace()));
			}
		
			currentPlatformNumber = platformNumber;
		}
		
		//System.out.println("Train run complete");
		
	}
	
	public void tripComplete()
	{
		asyncLogger.log("*** TRIP COMPLETED : " + name + " " + getDirectionName(), true);
	}
	
	public void startTrain()
	{	
		if (!isRunning())
		{
			asyncLogger.log("--- Starting Train : " + this.name + " ( " + this.getLineName() + " ) " + " ---", true);
			thread.start();
		}
		else
		{
			asyncLogger.log("--- Train : " + this.name + " ( " + this.getLineName() + " ) " + " is already running !!", true);
		}
	}
	
	public boolean isRunning()
	{
		if (thread.isAlive())
			return true;
			
		return false;
	}
	
}
