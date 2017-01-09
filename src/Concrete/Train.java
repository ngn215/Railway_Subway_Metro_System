package Concrete;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import Factory.CustomLoggerFactory;
import Factory.CustomThreadFactory;
import Factory.LockFactory;
import LockerClasses.ReentrantLockerUnlocker;


public class Train extends ReentrantLockerUnlocker implements Runnable {

	private String name;
	private Line line;
	private boolean directionUp;
	
	private Station previousStation;
	private Station currentStation;
	private Station nextStop;
	
	private int currentPlatformNumber;
	private int numberOfTripsCompleted;
	private boolean doorsOpen;
	private int noOfPeopleEnteringTrain;
	private int noOfPeopleExitingTrain;
	private final int speed;
	private final int capacity;
	//private final HashSet<Person> personsSet;
	private final Set<Person> personsSet;
	//private final ReentrantReadWriteLock personsSetLock;
	private final ReentrantReadWriteLock doorsLock;
	private final AsynchronousLogger asyncLogger;
	private final int totalTrips;
	private boolean shutDown;
	private final Thread thread;
	private final Stops stops;
	
	public Train(String name, Line line, boolean directionUp, int speed, Stops stops, int totalTrips)
	{
		this.name = name;
		this.line = line;
		this.directionUp = directionUp;
		this.speed = speed;
		this.capacity = 1000;
		this.totalTrips = totalTrips;
		this.shutDown = false;
		this.stops = stops;
		
		//this.personsSet = new HashSet<Person>();
		this.personsSet = Collections.newSetFromMap(new ConcurrentHashMap<Person, Boolean>());
		this.numberOfTripsCompleted = 0;
		this.noOfPeopleEnteringTrain = 0;
		this.noOfPeopleExitingTrain = 0;
		
		//this.personsSetLock = LockFactory.getReentrantReadWriteLockInstance(true);
		this.doorsLock = LockFactory.getReentrantReadWriteLockInstance(true);
		
		this.asyncLogger = CustomLoggerFactory.getAsynchronousLoggerInstance();
		this.thread = CustomThreadFactory.getThread(this, "T" + name, "Train");
		
		//gets first stop
		this.nextStop = stops.getFirstStop(directionUp);

	}
	
	public String getName() {
		return name;
	}

	public String getLineName() {
		return line.getName();
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
		if (currentStation!= null)
			return currentStation.getName();
		
		return "Out of station";
	}
	
	public boolean stopsAt(Station station)
	{
		return stops.isAStop(station);
	}
	
	public boolean trainFollowsLine(Line line)
	{
		return line.equals(this.line);
	}
	
	public boolean trainFollowsDirection(boolean directionUp)
	{
		return directionUp == this.directionUp;
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
			//readLock(personsSetLock);
			noOfPersons = personsSet.size();
		}
		finally
		{
			//readUnlock(personsSetLock);
		}		
		
		return capacity - noOfPersons;
	}
	
	public int getNumberOfPersons()
	{
		int noOfPersons = 0;
		
		try
		{
			//readLock(personsSetLock);
			noOfPersons = personsSet.size();
		}
		finally
		{
			//readUnlock(personsSetLock);
		}
		
		return noOfPersons;
	}
	
	public void reverseDirection()
	{
		asyncLogger.log("--- " + name + " Reversing Direction" + " ---");
		directionUp = !directionUp;
	}
	
	public void getTrainStatus()
	{
		System.out.println("TRAIN STATUS : " + name + " " + getDirectionName() + " " + getCurrentStationName() + "\t Persons count : " + getNumberOfPersons() + "\t TripNumber : " + (numberOfTripsCompleted - 1));
	}
	
	public void openDoors()
	{	
		try
		{
			writeLock(doorsLock);
			
			asyncLogger.log("Train : " + name + " opening doors at station : " + getCurrentStationName());
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
		
			asyncLogger.log("Train : " + name + " closing doors at station : " + getCurrentStationName());
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
			readLock(doorsLock);
			//writeLock(personsSetLock);
			
			if (!areDoorsOpen())
			{
				asyncLogger.log("Train : " + name + " doors are locked. " + " Person : " + person.getName() + " cannot enter at station : " + getCurrentStationName());
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
			//writeUnlock(personsSetLock);
			readUnlock(doorsLock);
		}
		
		return true;
	}
	
	public boolean enterTrain(Person person)
	{		
		try
		{			
			readLock(doorsLock);
			//writeLock(personsSetLock);
			
			if (!vacancyAvailable() || !areDoorsOpen()) //check if vacancy available and doors are open else break;
			{
				asyncLogger.log("Train : " + name + " doors are locked or No vacancy. " + " Person : " + person.getName() + " cannot enter at station : " + getCurrentStationName());
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
			//writeUnlock(personsSetLock);
			readUnlock(doorsLock);
		}
		
		return true;
	}

	public void exitCurrentStation()
	{
		currentStation.exitStationPlatform(this);
		
		previousStation = currentStation;
		
		currentStation = null;
		currentPlatformNumber = -1;
	}
	
	public void moveToNextStation(Station nextStation, int platformNumber)
	{
		//moving to platform
		nextStation.enterStationPlatform(this, platformNumber);
		
		//setting current platform number
		currentPlatformNumber = platformNumber;
		
		//setting current station
		currentStation = nextStation;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		numberOfTripsCompleted = 1;
		
		try
		{
			while(numberOfTripsCompleted <= totalTrips && !shutDown)
			{		
				doWhileTrainIsRunning();
			}
		}
		catch (InterruptedException e)
		{
			asyncLogger.log("Exception in Train : " + name + ". " + e, true);
		}
		
		//System.out.println("Train run complete");
		if (numberOfTripsCompleted > totalTrips)
			tripComplete();
		
	}
	
	private void doWhileTrainIsRunning() throws InterruptedException
	{	
		Station nextStation = null;
		
		//if previous station is null which means train is starting its journey then get first stop
		if (previousStation == null)
			nextStation = stops.getFirstStop(directionUp);
		else //else get next station from line
			nextStation = line.getNextStation(previousStation, directionUp);
		
		int platformNumber = getFreePlatform(nextStation);		
		
		moveToNextStation(nextStation, platformNumber);
		
		//we need to reverse direction here because otherwise people will never know the correct direction of the train
		//when the train reaches last stop (churchgate, dahanu road etc.) and reverses direction
		if (stops.isLastStop(currentStation, directionUp)) //reverse train when we hit last stop (NOT last station)
		{
			reverseDirection();		
			
			numberOfTripsCompleted++;
		}
		
		//only perform below if we are stopping here. if its not a stopping station then we just pass through.
		if (nextStop.equals(nextStation))
		{
			int numberOfExistingPersonsInTrain = getNumberOfPersons();
			
			//open train doors
			openDoors();
			
			//announcement for passengers in train
			announce();
			
			//signal for station that train is ready for intake
			readyForIntake();
			
			//wait for people to enter train
			Thread.sleep(100);
			
			//close train doors
			closeDoors();
			
			asyncLogger.log("No Of People in train " + name + " : " + numberOfExistingPersonsInTrain 
							+ " \t" + noOfPeopleEnteringTrain + "<- " + noOfPeopleExitingTrain + "-> " 
							+ " at station " + getCurrentStationName());
			
			//reset values
			noOfPeopleEnteringTrain = 0;
			noOfPeopleExitingTrain = 0;
			
			//System.out.println(this.getName() + " " + this.getDirection() + " "+ this.getCurrentStation());
			
			//getting nextStop for next cycle of run()
			nextStop = stops.getNextStop(currentStation, directionUp);
		}
		else
		{
			asyncLogger.log("Train : " + name + " is passing through station : " + getCurrentStationName());
		}
		
		//exit from current station
		exitCurrentStation();
		
		Thread.sleep(speed);
	}
	
	private int getFreePlatform(Station nextStation) throws InterruptedException
	{
		int platformNumber = nextStation.checkPlatformAvailabilty();
		
		while (platformNumber == -1)
		{
			asyncLogger.log("*** Station : " + nextStation.getName() + " not available...." + this.name + " waiting..." + " " + nextStation.printListOfTrainsInPlatforms(), true);
			
			Thread.sleep(1000);
			
			platformNumber = nextStation.checkPlatformAvailabilty();
			
			if (platformNumber != -1)
				asyncLogger.log("*** Found Empty platform : "+ platformNumber + " for " + this.name, true);
		}
		
		return platformNumber;
	}
	
	private void tripComplete()
	{
		asyncLogger.log("*** TRIP COMPLETED : " + name + " " + getDirectionName(), true);
	}
		
	public void startTrain()
 	{	
 		if (!isRunning())
 		{
 			asyncLogger.log("--- Starting Train : " + this.name + " ( " + stops.getName() + " ) " + " ---", true);
 			thread.start();
 		}
 		else
 		{
 			asyncLogger.log("--- Train : " + this.name + " ( " + stops.getName() + " ) " + " is already running !!", true);
 		}
	}
	
	public boolean isRunning()
	{
		return (thread.isAlive());
	}

	public void shutDown() {
		// TODO Auto-generated method stub
		shutDown = true;
	}
	
}
