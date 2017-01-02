package Factory;

import java.util.ArrayList;
import java.util.List;

import Concrete.AsynchronousLogger;
import Concrete.Line;
import Concrete.Train;

public class TrainFactory {

	private final static List<Train> trainsList = new ArrayList<Train>();
	private final static int TOTALTRIPSDEFAULT = 10;
	private final static AsynchronousLogger asyncLogger = CustomLoggerFactory.getAsynchronousLoggerInstance();
	
	private TrainFactory()
	{
		//do nothing
	}
	
	public static List<Train> getTrainsList()
	{
		return trainsList;
	}
	
	public static Train createTrainInstance(String name, String lineName, String direction, int speed, int totalTrips)
	{
		Line line = LineFactory.getLineInstance(lineName);
		boolean directionUp = direction.equals("Up") ? true : false;
		
		Train train = new Train(name, line, directionUp, speed, totalTrips);        
		trainsList.add(train);
		
		train.startTrain();
		
		return train;
	}
	
	public static Train createTrainInstance(String name, String lineName, String direction, int speed)
	{
		return createTrainInstance(name, lineName, direction, speed, TOTALTRIPSDEFAULT);
	}
	
	public static boolean areTrainsRunning()
	{
		for(Train train : trainsList)
		{
			if (train.isRunning())
				return true;
		}
		
		return false;
 	}
	
	public static void shutDownAllTrainThreads()
	{
		asyncLogger.log("Shutting down all train threads.", true);
		
		for(Train train : trainsList)
		{
			if (train.isRunning())
				train.shutDown();
		}
	}
}
