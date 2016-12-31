package Factory;

import java.util.ArrayList;
import java.util.List;

import Concrete.Line;
import Concrete.Train;

public class TrainFactory {

	private final static List<Train> trainsList = new ArrayList<Train>();
	
	private TrainFactory()
	{
		//do nothing
	}
	
	public static List<Train> getTrainsList()
	{
		return trainsList;
	}
	
	public static Train createTrainInstance(String name, String lineName, String direction, int speed, boolean start)
	{
		Line line = LineFactory.getLineInstance(lineName);
		boolean directionUp = direction.equals("Up") ? true : false;
		
		Train train = new Train(name, line, directionUp, speed);        
		trainsList.add(train);
		
		if (start)
			startTrain(train);
		
		return train;
	}
	
	public static void startTrain(Train train)
	{
		train.startTrain();
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
}
