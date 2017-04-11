package Factory;

import java.util.ArrayList;
import java.util.List;

import Concrete.AsynchronousLogger;
import Concrete.Line;
import Concrete.Stops;
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
	
	public static Train createTrainInstance(String name, String lineName, String direction, int speed, String stopsName, int totalTrips)
	{	
		Line line = LineFactory.getLineInstance(lineName);				
		Stops stops = line.getStops(stopsName);
		boolean directionUp = direction.equals("Up") ? true : false;
		
		Train train = Train.getInstance(name, line, directionUp, speed, stops, totalTrips);        
		trainsList.add(train);
		
		train.startTrain();
		
		return train;
	}
	
	public static Train createTrainInstance(String name, String lineName, String direction, int speed, String stopsName)
	{		
		return createTrainInstance(name, lineName, direction, speed, stopsName, TOTALTRIPSDEFAULT);
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
