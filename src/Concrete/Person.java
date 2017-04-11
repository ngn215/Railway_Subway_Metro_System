package Concrete;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import Factory.CustomLoggerFactory;
import Factory.CustomThreadFactory;
import Factory.LineFactory;


public class Person implements Runnable {

	private final String name;
	private final Station sourceStation;
	private final Station destinationStation;
	private final Line trainLine;
	private final boolean trainDirectionUp;
	private boolean reachedDestination;
	private Train train;
	private boolean inTrain;
	//private volatile boolean interruptFlag;
	private final AsynchronousLogger asyncLogger;
	private boolean shutDown;
	private final Thread thread;
	private volatile Train checkingForTrain;
	private final Set<Train> checkedTrainsSet;
	
	private Person(String name, Station sourceStation, Station destinationStation)
	{
		this.name = name;
		this.sourceStation = sourceStation;
		this.destinationStation = destinationStation;
		this.shutDown = false;
		
		//this.interruptFlag = false;
		this.reachedDestination = false;
		this.inTrain = false;
		this.trainLine = LineFactory.getLineFromSourceAndDestination(sourceStation, destinationStation);
		this.trainDirectionUp = LineFactory.getDirectionFromSourceAndDestination(trainLine, sourceStation, destinationStation);
		this.thread = CustomThreadFactory.getThread(this, "T" + name, "Person");
		this.checkedTrainsSet = new HashSet<Train>();
		
		this.asyncLogger = CustomLoggerFactory.getAsynchronousLoggerInstance();
	}
	
	public static Person getInstance(String name, Station sourceStation, Station destinationStation)
	{
		//validate
		if (name == null || name.isEmpty())
			throw new IllegalArgumentException("Argument : name cannot be " + (name == null ? "null" : "empty"));
		
		if (sourceStation == null)
			throw new IllegalArgumentException("Argument : sourceStation cannot be null");
		
		if (destinationStation == null)
			throw new IllegalArgumentException("Argument : destinationStation cannot be null");
		
		//return person instance
		return new Person(name, sourceStation, destinationStation);
	}
	
	public String getTrainLineName() {
		return trainLine.getName();
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
		if (train.trainFollowsLine(trainLine) 
			&& train.trainFollowsDirection(trainDirectionUp) 
			&& train.stopsAt(destinationStation))
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
			while (!inTrain && !shutDown) //waiting for train
			{											
				//wait if we have checked all trains in platforms
				while(allStationPlatformTrainsChecked() && !shutDown)
				{
					//resetInterruptFlag(); //getting read to be interrupted again
					
					try
					{
						synchronized(sourceStation)
						{														
							//waiting for station to announce
							sourceStation.wait();
						}
					}
					catch(InterruptedException e)
					{
						//setInterruptFlag(); //this makes sure multiple trains dont interrupt this thread. doesnt sound right
						asyncLogger.log("<< EXCEPTION in Person : " + this.name + " while waiting for train>>" + " " + e, false);
					}
					finally
					{
						//clear checked trains set
						checkedTrainsSet.clear();
					}
				}
				
				//resetInterruptFlag(); //getting read to be interrupted again
				
				doWhenInStation();
				
				thread.interrupted(); //clear interrupted status of the thread before entering below block else it will throw interrupted exception
				
				//wait before we go to next iteration
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					System.out.println("Person : " + name + " sleep interrupted.");
					e.printStackTrace();
				}
			}
			
			//when in train wait for signals from train. we are not checking any interruptions here.
			while (inTrain && !shutDown)
			{
				//resetInterruptFlag();
				
				try 
				{	
					synchronized(train)
					{
						//waiting for train to announce
						train.wait();						
					}
				}
				catch(InterruptedException e)
				{
					//setInterruptFlag();
					asyncLogger.log("<< EXCEPTION in Person : " + this.name + " while in train>>" + " " + e, false);
				}
				
				//resetInterruptFlag();
								
				doWhenInTrain();
			}
			
		} //end of while(!reachedDestination && !shutDown)
	}
	
	private void doWhenInStation()
	{				
		boolean canTakeThisTrain = false;
				
		Set<Entry<Integer, Train>> entrySet = sourceStation.getTrainsInPlatforms().entrySet();
		
		if(entrySet.isEmpty())
			return;
		
		Iterator<Entry<Integer, Train>> iter = entrySet.iterator();
		
		while (iter.hasNext() //while iterator has more trains 
				&& !checkThreadInterruption() //and while thread has not been interrupted
				&& !inTrain //and while person is not in train
				) 
		{			
			Map.Entry<Integer, Train> pair = (Map.Entry<Integer, Train>)iter.next();
			
			if(pair == null || checkThreadInterruption())
				return;
			
			checkingForTrain = pair.getValue();
			
			checkedTrainsSet.add(checkingForTrain);
			
			if(checkThreadInterruption())
				return;

			canTakeThisTrain = canTakeThisTrain(checkingForTrain);
			
			if(checkThreadInterruption())
				return;
			
			if (canTakeThisTrain && enterTrain(checkingForTrain))
			{
				inTrain = true;
				//checkingForTrain = null;
				//System.out.println("Thread : " + thread.getName() + " has entered train.");
			}
			
			//done checking current train Or entered a train
			checkingForTrain = null;
		}		
	}
	
	private void doWhenInTrain()
	{		
		if(!checkIfTrainAtDestination())
			return;
		
		if (exitTrain())
		{
			inTrain = false;
			//System.out.println("Thread : " + thread.getName() + " has exited from train");
		}
	}
	
	private boolean areStationPlatformsEmpty()
	{		
		return sourceStation.getTrainsInPlatforms().isEmpty();
	}
	
	private boolean allStationPlatformTrainsChecked()
	{
		if (areStationPlatformsEmpty())
			return true;
		
		if (checkedTrainsSet.isEmpty())
			return false;
		
		Iterator<Entry<Integer, Train>> iter = sourceStation.getTrainsInPlatforms().entrySet().iterator();
		
		while (iter.hasNext()) //while iterator has more trains  
		{
			//if we use train id approach then we need to make changes here as well.
			if (!checkedTrainsSet.contains(iter.next().getValue()))
				return false;
		}
		
		//System.out.println("Person : " + name + " allStationPlatformTrainsChecked ? true");
		return true;
	}
	
	private boolean checkThreadInterruption()
	{	
		//return interruptFlag;		
		return thread.isInterrupted();
	}
	
	//interrupt all threads that are not waiting and do not have interruptFlag set
	public boolean interruptThread()
	{
		return interruptThread(null);
	}
	
	//interrupt person threads that are trying to enter this train.
	//if train == null then just check for waiting and interruptFlag.
	//Note : we needed to add this parameter : interruptRaisedByTrain because we don't want every train trying to
	//interrupt every person. we want only those trains to interrupt persons in which the person is trying to get into.
	public boolean interruptThread(Train interruptRaisedByTrain)
	{
		if (interruptRaisedByTrain == null || interruptRaisedByTrain.equals(checkingForTrain))
		{
			//only interrupt uninterrupted persons
			//if (thread.getState() != Thread.State.WAITING && !interruptFlag)
			if (thread.getState() != Thread.State.WAITING)
			{
				//setInterruptFlag();
				thread.interrupt();
				return true;
			}
		}

		return false;
	}
	
	/*private void resetInterruptFlag()
	{
		interruptFlag = false;
	}
	
	private void setInterruptFlag()
	{
		interruptFlag = true;
	}*/
	
	public void shutDown()
	{
		shutDown = true;
	}
	
	public boolean isRunning()
	{
		return thread.isAlive();
	}
	
	public boolean isNotWaiting()
	{
		//System.out.println(name + " " + thread.getState());
		return (thread.getState() != Thread.State.WAITING);
	}
	
	public void startPersonThread()
	{	
		if (!thread.isAlive())
		{
			thread.start();
		}
	}
}
