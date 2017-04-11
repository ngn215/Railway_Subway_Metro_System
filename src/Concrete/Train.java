package Concrete;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
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
	private AtomicInteger noOfPeopleInTrain; //we need this variable because concurrent set may not get us latest value.
	private AtomicInteger noOfPeopleEnteringTrain;
	private AtomicInteger noOfPeopleExitingTrain;
	private final int speed;
	private final int capacity;
	private final Set<Person> personsSet;
	private final ReentrantReadWriteLock doorsLock;
	private final AsynchronousLogger asyncLogger;
	private final int totalTrips;
	private boolean shutDown;
	private final Thread thread;
	private final Stops stops;
	//private final Semaphore semaphore;
	
	private Train(String name, Line line, boolean directionUp, int speed, Stops stops, int totalTrips)
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
		this.noOfPeopleEnteringTrain = new AtomicInteger(0);
		this.noOfPeopleExitingTrain = new AtomicInteger(0);
		this.noOfPeopleInTrain = new AtomicInteger(0);
		
		//this.semaphore = new Semaphore(100, true);
		
		//this.personsSetLock = LockFactory.getReentrantReadWriteLockInstance(true);
		this.doorsLock = LockFactory.getReentrantReadWriteLockInstance(true);
		
		this.asyncLogger = CustomLoggerFactory.getAsynchronousLoggerInstance();
		this.thread = CustomThreadFactory.getThread(this, "T" + name, "Train");
		
		//gets first stop
		this.nextStop = stops.getFirstStop(directionUp);

	}
	
	public static Train getInstance(String name, Line line, boolean directionUp, int speed, Stops stops, int totalTrips)
	{
		//validate
		if (name == null || name.isEmpty())
			throw new IllegalArgumentException("Argument : name cannot be null or empty");
		
		if (line == null)
			throw new IllegalArgumentException("Argument : lineName cannot be null");
		
		if (speed < 0)
			throw new IllegalArgumentException("Argument : speed should be greater than zero");
		
		if (stops == null)
			throw new IllegalArgumentException("Argument : stopsName cannot be null");
		
		if (totalTrips <= 0)
			throw new IllegalArgumentException("Argument : totalTrips should be greater than zero");
		
		//return train instance
		return new Train(name, line, directionUp, speed, stops, totalTrips);
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
		return noOfPeopleInTrain.get() < capacity;
	}
	
	public int noOfVacanciesAvailable()
	{		
		return capacity - noOfPeopleInTrain.get();
	}
	
	public int getNumberOfPersons()
	{
		return noOfPeopleInTrain.get();
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
			//semaphore.acquire();
			readLock(doorsLock);
			
			if (!areDoorsOpen())
			{
				asyncLogger.log("Train : " + name + " doors are locked. " + " Person : " + person.getName() + " cannot exit at station : " + getCurrentStationName(), true);
				return false;
			}
			
			if (!person.getDestinationStation().getName().equals(getCurrentStationName()))
			{
				asyncLogger.log("ERR : Person is getting out at wrong station.", true);
			}
			
			//decrement counter
			noOfPeopleInTrain.decrementAndGet();
			
			//exit from train
			personsSet.remove(person);
			
			//person exits train and enters destination station
			currentStation.enterStation(person);
			
			noOfPeopleExitingTrain.incrementAndGet();
			
			asyncLogger.log("Person : " + person.getName() + " exiting train : " + this.name + " at station : " + getCurrentStationName());
		}
		finally
		{
			readUnlock(doorsLock);
			//semaphore.release();
		}
		
		return true;
	}
	
	public boolean enterTrain(Person person)
	{		
		try
		{							
			//semaphore.acquire();
			readLock(doorsLock); //this lock will prevent train doors from closing when person is entering
			
			if (checkPersonThreadInterruption())
			{
				return false; //person thread is interrupted.
			}
			
			//add check here to the train id here.........
			
			if (!vacancyAvailable()) //check if vacancy available and doors are open else break;
			{
				asyncLogger.log("Train : " + name + " has No Vacancy. " + " Person : " + person.getName() + " cannot enter train at station : " + person.getSourceStation().getName());
				return false;
			}
			else if (!areDoorsOpen()) //what if we get to this point and train has moved to next station and opened doors. we need to prevent such cases using an id for train
			{
				asyncLogger.log("Train : " + name + " doors are Locked. " + " Person : " + person.getName() + " cannot enter train at station : " + person.getSourceStation().getName());
				return false;
			}
			
			//increment counter
			noOfPeopleInTrain.incrementAndGet();
			
			//exit from station
			currentStation.exitStation(person);
			
			//now enter train
			personsSet.add(person);
			
			noOfPeopleEnteringTrain.incrementAndGet();
			
			asyncLogger.log("Person : " + person.getName() + " entering train : " + this.name + " from station : " + getCurrentStationName());
		}
		finally
		{
			readUnlock(doorsLock);
			//semaphore.release();
		}
		
		return true;
	}
	
	private boolean checkPersonThreadInterruption()
	{		
		return (Thread.currentThread().isInterrupted()); //the person thread trying to enter train.
	}

	public void exitCurrentStation()
	{
		//interruptPersonsInTrain();
		
		currentStation.exitStationPlatform(this);
		
		previousStation = currentStation;
		
		currentStation = null;
		currentPlatformNumber = -1;
	}
	
	private void moveToNextStation(Station nextStation, int platformNumber)
	{
		//moving to platform
		nextStation.enterStationPlatform(this, platformNumber);
		
		//setting current platform number
		currentPlatformNumber = platformNumber;
		
		//setting current station
		currentStation = nextStation;
	}
	
	//tells us whether threads are still trying to exit train.
	private boolean arePeopleExitingTrain()
	{
		for(Person person : personsSet)
		{
			if (person.isNotWaiting())
				return true;
		}
		
		return false;
	}
	
	private boolean areThereAnyPeopleInTrain()
	{
		return !personsSet.isEmpty();
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		numberOfTripsCompleted = 1;
		
		while(numberOfTripsCompleted <= totalTrips && !shutDown)
		{		
			try
			{
				doWhileTrainIsRunning();
			}
			catch (InterruptedException e)
			{
				asyncLogger.log("Exception in Train : " + name + ". " + e, true);
			}
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
			
			//announcement for passengers in train that train has reached station
			announce();
			
			//check if people are still exiting the train. we wait till all persons have exited train.
			while(areThereAnyPeopleInTrain() //while there are people in train 
					&& arePeopleExitingTrain() //and while people are still trying to exit train
					)
			{
				//sleep
				Thread.sleep(5);
			}
			
			//signal for station that train is ready for intake
			readyForIntake();
			
			//wait for people to enter train
			Thread.sleep(100);
			
			//close train doors
			closeDoors();
			
			//ask station to interrupt persons trying to enter this train
			currentStation.stopPersonsFromEnteringTrain(this);
			
			asyncLogger.log("No Of People in train " + name + " : " + numberOfExistingPersonsInTrain 
							+ " \t" + noOfPeopleEnteringTrain + "<- " + noOfPeopleExitingTrain + "-> " 
							+ " at station " + getCurrentStationName(), false);
			
			//reset values
			noOfPeopleEnteringTrain.set(0);
			noOfPeopleExitingTrain.set(0);
			
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
