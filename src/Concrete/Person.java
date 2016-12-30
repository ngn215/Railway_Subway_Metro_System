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
	private final Thread thread;
	private AsynchronousLogger asyncLogger;
	
	public Person(String name, Station sourceStation, Station destinationStation)
	{
		this.name = name;
		this.sourceStation = sourceStation;
		this.destinationStation = destinationStation;
		
		this.reachedDestination = false;
		this.inTrain = false;
		this.trainLine = LineFactory.getLineName(sourceStation, destinationStation);
		this.trainDirectionUp = LineFactory.getDirection(trainLine, sourceStation, destinationStation);
		this.thread = CustomThreadFactory.getThread(this, "T" + name, "Person");
		this.asyncLogger = CustomLoggerFactory.getAsynchronousLoggerInstance();
	}

	public synchronized void getPersonStatus()
	{
		if (train != null)
			System.out.println("PERSON STATUS : " + this.name + " Source : " + this.sourceStation.getName() + "\t Destination : " + this.destinationStation.getName() + ". \tIn Train : " + this.train.getName() + " " + this.train.getDirectionName());
		else
			System.out.println("PERSON STATUS : " + this.name + " Source : " + this.sourceStation.getName() + "\t Destination : " + this.destinationStation.getName());
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
	
	public Thread getThread() {
		return thread;
	}
	
	public boolean hasReachedDestination() {
		return reachedDestination;
	}

	public String getName() {
		return name;
	}

	public boolean canTakeThisTrain(Train train)
	{		
		if (train.getLineName().equals(trainLine) && train.getDirection() == trainDirectionUp)
		{
			return true;
		}
		
		return false;
	}
	
	public boolean checkIfTrainAtDestination()
	{		
		if (train.getCurrentStationName().equals(destinationStation.getName()))
		{
			return true;
		}
		
		return false;
	}
	
	public boolean enterTrain(Train train)
	{
		boolean success = train.enterTrain(this);
		
		if (success)
		{
			this.train = train;
			
			if (!train.getCurrentStationName().equals(sourceStation.getName()))
				System.out.println("Person : " + name + " at " + sourceStation.getName() + " is entering Train : " + train.getName() + " located at " + train.getCurrentStationName());
			
			return true;
		}
		
		return false;
		//System.out.println("*** PERSON : " + this.name + " is entering train " + " " + this.train.getName() + " " + this.train.getDirectionName());
	}
	
	public boolean exitTrain()
	{
		//System.out.println("*** PERSON : " + this.name + " is exiting train " + " " + this.train.getName() + " " + this.train.getDirectionName());
		boolean success = train.exitTrain(this);
		
		if (success)
		{
			//System.out.println("Person : " + name + " is exiting from Train : " + train.getName());
			this.train = null;
			this.reachedDestination = true;
			
			return true;
		}
		
		return false;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		while (!reachedDestination)
		{
			if (!inTrain) //waiting for train
			{
				boolean canTakeThisTrain = false;
				synchronized(sourceStation)
				{					
					try
					{
						sourceStation.wait();
						
						if(checkThreadInterruption())
							break;
						
						HashMap<Integer, Train> trainPlatformMap = sourceStation.getTrainsInPlatforms();
						
						if(checkThreadInterruption() || trainPlatformMap.isEmpty())
							break;
						
						Set<Entry<Integer, Train>> entrySet = trainPlatformMap.entrySet();
						
						if(checkThreadInterruption() || entrySet == null)
							break;
						
						Iterator<Entry<Integer, Train>> iter = entrySet.iterator();
						
						if(checkThreadInterruption())
							break;
						
						while (iter.hasNext() && !checkThreadInterruption()) 
						{
							Map.Entry<Integer, Train> pair = null;
							
							pair = (Map.Entry<Integer, Train>)iter.next();
							
							if(checkThreadInterruption() || pair == null)
								break;
							
							Train trainFromStationSet = pair.getValue();
							
							if(checkThreadInterruption())
								break;
	
							canTakeThisTrain = canTakeThisTrain(trainFromStationSet);
							
							if(checkThreadInterruption())
								break;
							
							if (canTakeThisTrain && enterTrain(trainFromStationSet))
							{
								inTrain = true;
								//System.out.println("Thread : " + thread.getName() + " has entered train.");
							}
						}
					}//end try block
					catch(InterruptedException|ConcurrentModificationException e)
					{
						//System.out.println("<< EXCEPTION in Person : " + this.name + " while waiting for train>>");
						//System.out.println(e);
						//e.printStackTrace();
						asyncLogger.log("<< EXCEPTION in Person : " + this.name + " while waiting for train>>" + " " + e, true);
					}
				}			
			}
			
			//when in train
			if (inTrain)
			{
				synchronized(train)
				{
					try 
					{
						train.wait();
					
						boolean isTrainAtDestination = false;
						
						if (checkThreadInterruption())
							break;
						
						isTrainAtDestination = checkIfTrainAtDestination();
						
						if (checkThreadInterruption())
							break;
						
						if (isTrainAtDestination && exitTrain())
						{
							inTrain = false;
							//System.out.println("Thread : " + thread.getName() + " has exited from train");
						}
					}
					catch(InterruptedException|ConcurrentModificationException e)
					{
						//System.out.println("<< EXCEPTION in Person : " + this.name + " while in train>>");
						//System.out.println(e);
						//e.printStackTrace();
						asyncLogger.log("<< EXCEPTION in Person : " + this.name + " while in train>>" + " " + e, true);
					}
				}
			}
			
		} //end of while(true)
	}
	
	private boolean checkThreadInterruption()
	{
		//logging this was causing problems because during process of logging if another train interrupts this person thread
		//then it raises interrupted exception in asyncLogger. Station class makes the interrupt call but train initiates it.
		//if (Thread.currentThread().isInterrupted())
			//asyncLogger.log("Person : " + this.name + " has been interrupted.");
		
		return Thread.currentThread().isInterrupted();
	}
	
	public void startPersonThread()
	{	
		if (!thread.isAlive())
		{
			thread.start();
		}
	}
}
