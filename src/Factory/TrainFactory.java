package Factory;

import java.util.ArrayList;
import java.util.List;

import Concrete.Line;
import Concrete.Train;

public class TrainFactory {

	private static List<Train> trains = new ArrayList<Train>();
	
	private TrainFactory()
	{
		//do nothing
	}
	
	public static List<Train> getTrainsList()
	{
		return trains;
	}
	
	public static Train getTrainInstance(String name, String lineName, String direction, int speed, boolean start)
	{
		Line line = LineFactory.getLineInstance(lineName);
		boolean directionUp = direction.equals("Up") ? true : false;
		
		Train train = new Train(name, line, directionUp, speed);        
		trains.add(train);
		
		if (start)
			startTrain(train);
		
		return train;
	}
	
	public static void startTrain(Train train)
	{
		train.startTrain();
	}
}
