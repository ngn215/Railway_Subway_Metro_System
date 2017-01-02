package Factory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import Concrete.AsynchronousLogger;
import Concrete.Line;
import Concrete.Train;

public class TrainFactory {

	private final static List<Train> trainsList = new ArrayList<Train>();
	private final static int TOTALTRIPSDEFAULT = 10;
	private static ExecutorService executorService;
	private final static AsynchronousLogger asyncLogger = CustomLoggerFactory.getAsynchronousLoggerInstance();
	
	private TrainFactory()
	{
		//do nothing
	}
	
	public static void setNumberOfTrains(int totalNumberOfTrains)
	{
		executorService = ExecutorServiceFactory.createFixedThreadPoolExecutor(totalNumberOfTrains);
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
		
		asyncLogger.log("---- Starting train : " + name + " ----", true);
		ExecutorServiceFactory.executeThreadInPool(executorService, train);
		train.setThreadName("T" + train.getName());
		
		return train;
	}
	
	public static Train createTrainInstance(String name, String lineName, String direction, int speed)
	{
		return createTrainInstance(name, lineName, direction, speed, TOTALTRIPSDEFAULT);
	}
}
