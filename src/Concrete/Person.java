package Concrete;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import Factory.CustomLoggerFactory;
import Factory.CustomThreadFactory;
import Factory.LineFactory;


public class Person implements Runnable {

	private String name;
	private Station sourceStation;
	private Station destinationStation;
	private String trainLine;
	private boolean trainDirectionUp;
	private boolean reachedDestination;
	private Train train;
	private boolean inTrain;
	private volatile boolean interruptFlag;
	private final AsynchronousLogger asyncLogger;
	private boolean shutDown;
	private final Thread thread;
	
	public Person(String name, Station sourceStation, Station destinationStation)
	{
		this.name = name;
		this.sourceStation = sourceStation;
		this.destinationStation = destinationStation;
		this.shutDown = false;
		
		this.interruptFlag = false;
		this.reachedDestination = false;
		this.inTrain = false;
		this.trainLine = LineFactory.getLineName(sourceStation, destinationStation);
		this.trainDirectionUp = LineFactory.getDirection(trainLine, sourceStation, destinationStation);
		this.thread = CustomThreadFactory.getThread(this, "T" + name, "Person");
		
		this.asyncLogger = CustomLoggerFactory.getAsynchronousLoggerInstance();
	}
	
	public String getTrainLine() {
		return trainLine;
	}

	public Station getSourceStation() {
		return sourceStation;
	}

	public Station getDestinationStation() {
		return destinationStation;
	}

	public boolean isDirectionUp() {
		return trainDirectionUp;
	}
	
	public boolean hasReachedDestination() {
		return reachedDestination;
	}

	public String getName() {
		return name;
	}

	private boolean canTakeThisTrain(Train train)
	{		
		if (train.getLineName().equals(trainLine) && train.getDirection() == trainDirectionUp)
		{
			return true;
		}
		
		return false;
	}
	
	private boolean checkIfTrainAtDestination()
	{		
		if (train.getCurrentStationName().equals(destinationStation.getName()))
		{
			return true;
		}
		
		return false;
	}
	
	private void enterStation(Person person)
	{
		sourceStation.enterStation(person);
	}
	
	private boolean enterTrain(Train train)
	{
		boolean success = train.enterTrain(this);
		
		if (success)
		{
			this.train = train;
			
			if (!train.getCurrentStationName().equals(sourceStation.getName()))
				asyncLogger.log("Person : " + name + " at " + sourceStation.getName() + " is entering Train : " + train.getName() + " located at " + train.getCurrentStationName(), true);
			
			return true;
		}
		
		return false;
		//System.out.println("*** PERSON : " + this.name + " is entering train " + " " + this.train.getName() + " " + this.train.getDirectionName());
	}
	
	private boolean exitTrain()
	{
		//System.out.println("*** PERSON : " + this.name + " is exiting train " + " " + this.train.getName() + " " + this.train.getDirectionName());
		boolean success = train.exitTrain(this);
		
		if (success)
		{
			//System.out.println("Person : " + name + " is exiting from Train : " + train.getName());
			train = null;
			reachedDestination = true;
			
			return true;
		}
		
		return false;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		//enter station when thread first starts
		enterStation(this);
		
		while (!reachedDestination && !shutDown)
		{
			if (!inTrain && !shutDown) //waiting for train
			{							
				try
				{
					setInterruptFlag();
					
					synchronized(sourceStation)
					{					
						//waiting for station to announce
						sourceStation.wait();
					}
					
					resetInterruptFlag();
					
					doWhenInStation();
					
				}//end try block
				catch(InterruptedException|ConcurrentModificationException e)
				{
					//if (e instanceof InterruptedException)
						//setInterruptFlag();
					
					//System.out.println("<< EXCEPTION in Person : " + this.name + " while waiting for train>>");
					//System.out.println(e);
					//e.printStackTrace();
					asyncLogger.log("<< EXCEPTION in Person : " + this.name + " while waiting for train>>" + " " + e, false);
				}		
			}
			
			//when in train
			if (inTrain && !shutDown)
			{
				try 
				{
					setInterruptFlag();
					
					synchronized(train)
					{
						//waiting for train to announce
						train.wait();
					}
					
					resetInterruptFlag();
					
					doWhenInTrain();
				}//end try block
				catch(InterruptedException|ConcurrentModificationException e)
				{
					//if (e instanceof InterruptedException)
						//setInterruptFlag();
					
					//System.out.println("<< EXCEPTION in Person : " + this.name + " while in train>>");
					//System.out.println(e);
					//e.printStackTrace();
					asyncLogger.log("<< EXCEPTION in Person : " + this.name + " while in train>>" + " " + e, false);
				}
			}
			
		} //end of while(true)
	}
	
	private void doWhenInStation() throws ConcurrentModificationException
	{
		if(checkThreadInterruption())
			return;
		
		boolean canTakeThisTrain = false;
		
		HashMap<Integer, Train> trainPlatformMap = sourceStation.getTrainsInPlatforms();
		
		if(checkThreadInterruption() || trainPlatformMap.isEmpty())
			return;
		
		Set<Entry<Integer, Train>> entrySet = trainPlatformMap.entrySet();
		
		if(checkThreadInterruption() || entrySet == null)
			return;
		
		Iterator<Entry<Integer, Train>> iter = entrySet.iterator();
		
		if(checkThreadInterruption())
			return;
		
		while (iter.hasNext() && !checkThreadInterruption()) 
		{
			Map.Entry<Integer, Train> pair = null;
			
			pair = (Map.Entry<Integer, Train>)iter.next();
			
			if(checkThreadInterruption() || pair == null)
				return;
			
			Train trainFromStationSet = pair.getValue();
			
			if(checkThreadInterruption())
				return;

			canTakeThisTrain = canTakeThisTrain(trainFromStationSet);
			
			if(checkThreadInterruption())
				return;
			
			if (canTakeThisTrain && enterTrain(trainFromStationSet))
			{
				inTrain = true;
				//System.out.println("Thread : " + thread.getName() + " has entered train.");
			}
		}
	}
	
	private void doWhenInTrain() throws ConcurrentModificationException
	{
		boolean isTrainAtDestination = false;
		
		if (checkThreadInterruption())
			return;
		
		isTrainAtDestination = checkIfTrainAtDestination();
		
		if (checkThreadInterruption())
			return;
		
		if (isTrainAtDestination && exitTrain())
		{
			inTrain = false;
			//System.out.println("Thread : " + thread.getName() + " has exited from train");
		}
	}
	
	public boolean isRunning()
	{
		return thread.isAlive();
	}
	
	public void startPersonThread()
	{	
		if (!thread.isAlive())
		{
			thread.start();
		}
	}
	
	private boolean checkThreadInterruption()
	{		
		return (Thread.currentThread().isInterrupted() || interruptFlag); //we should check both because in interrupt() method we first call
																			//thread.interrupt() and then set interrupt flag;
	}
	
	public boolean interruptThread()
	{
		//only interrupt uninterrupted persons
		if (thread.getState() != Thread.State.WAITING && !interruptFlag)
		{
			setInterruptFlag();
			thread.interrupt();
			return true;
		}

		return false;
	}
	
	private void resetInterruptFlag()
	{
		interruptFlag = false;
	}
	
	private void setInterruptFlag()
	{
		interruptFlag = true;
	}
	
	public void shutDown()
	{
		shutDown = true;
	}
}
