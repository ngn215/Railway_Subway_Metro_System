//import Concrete.Intersection;
import Factory.LineFactory;
import Factory.PersonFactory;
import Factory.StationFactory;
import Factory.TrainFactory;
import Status.TrainStatus;

public class RailwaySubwayMetroSystem {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		//initialize station, line and person factory.
		StationFactory.initializeFactory();
		LineFactory.initializeFactory();
		PersonFactory.randomlyGeneratePersons(15000);
		
		TrainFactory.getTrainInstance("W1", "WesternSlow", "Up", 1000, true);
		TrainFactory.getTrainInstance("W2", "WesternFast", "Up", 1500, true);
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		TrainFactory.getTrainInstance("W3", "WesternSlow", "Down", 500, true);
		TrainFactory.getTrainInstance("C1", "CentralSlow", "Up", 1000, true);
		TrainFactory.getTrainInstance("C2", "CentralFast", "Down", 1500, true);
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		TrainFactory.getTrainInstance("C3", "CentralSlow", "Up", 100, true);
		
		TrainStatus ts = new TrainStatus(TrainFactory.getTrainsList(), 60000);
		ts.getTrainsStatus();
		
		//Intersection westernCentralIntersection = new Intersection("Western-Central1", westernSlowLine, centralSlowLine, "Dadar");
	}
	
}
