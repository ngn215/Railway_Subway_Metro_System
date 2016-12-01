package Concrete;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


public class Person implements Runnable {

	String name;
	int weight;
	Station sourceStation;
	Station destinationStation;
	String trainLine;
	boolean trainDirectionUp;
	boolean reachedDestination;
	Train train;
	boolean inTrain;
	private Thread thread;
	
	public Person(String name, Station sourceStation, Station destinationStation)
	{
		this.name = name;
		this.sourceStation = sourceStation;
		this.destinationStation = destinationStation;
		
		this.reachedDestination = false;
		this.inTrain = false;
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

	public void setTrainLine(String trainLine) {
		this.trainLine = trainLine;
	}

	public boolean isDirectionUp() {
		return trainDirectionUp;
	}

	public void setTrainDirectionUp(boolean trainDirectionUp) {
		this.trainDirectionUp = trainDirectionUp;
	}
	
	public Thread getThread() {
		return thread;
	}

	public void setThread(Thread thread) {
		this.thread = thread;
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
		if (train.getCurrentStation().equals(destinationStation.getName()))
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
			
			if (!train.getCurrentStation().equals(sourceStation.getName()))
				System.out.println("Person : " + name + " at " + sourceStation.getName() + " is entering Train : " + train.getName() + " located at " + train.getCurrentStation());
			
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
		
		while (true)
		{
			if (!inTrain) //waiting for train
			{
				boolean canTakeThisTrain = false;
				synchronized(sourceStation)
				{
					try {
						sourceStation.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					HashMap<Integer, Train> trainPlatformMap = sourceStation.getTrainsInPlatforms();
					
					if(checkThreadInterruption())
						break;
					
					Iterator<Entry<Integer, Train>> iter = trainPlatformMap.entrySet().iterator();
					
					if(checkThreadInterruption())
						break;
					
					while (iter.hasNext() && !checkThreadInterruption()) 
					{
						Map.Entry<Integer, Train> pair = (Map.Entry<Integer, Train>)iter.next();
						Train trainFromStationSet = pair.getValue();
						
						//System.out.println("inside iterator : " + trainFromStationSet.getName());
						
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
				}			
			}
			
			//when in train
			if (inTrain)
			{
				synchronized(train)
				{
					try {
						train.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
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
			}
			
		} //end of while(true)
	}
	
	private boolean checkThreadInterruption()
	{
		if (Thread.currentThread().isInterrupted())
			System.out.println("Person : " + this.name + " has been interrupted.");
		
		return Thread.currentThread().isInterrupted();
	}
}
