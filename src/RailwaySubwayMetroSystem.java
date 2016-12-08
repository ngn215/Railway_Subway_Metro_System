//import Concrete.Intersection;
import Factory.LineFactory;
import Factory.PersonFactory;
import Factory.StationFactory;
import Factory.TrainFactory;
import Status.StationStatus;
import Status.TrainStatus;

public class RailwaySubwayMetroSystem {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		//initialize station, line and person factory.
		StationFactory.initializeFactory();
		LineFactory.initializeFactory();
		PersonFactory.randomlyGeneratePersons(10000, "WesternSlow");
		PersonFactory.randomlyGeneratePersons(10000, "CentralSlow");
		PersonFactory.randomlyGeneratePersons(10000, "HarborPanvel");
		PersonFactory.randomlyGeneratePersons(2000, "HarborAndheri");
		
		TrainFactory.getTrainInstance("W1", "WesternSlow", "Up", 1000, true);
		TrainFactory.getTrainInstance("W2", "WesternFast", "Up", 500, true);
		TrainFactory.getTrainInstance("C1", "CentralSlow", "Up", 1000, true);
		TrainFactory.getTrainInstance("C2", "CentralFast", "Down", 100, true);
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		TrainFactory.getTrainInstance("W3", "WesternSlow", "Down", 1500, true);
		TrainFactory.getTrainInstance("C3", "CentralSlow", "Up", 1500, true);
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		TrainFactory.getTrainInstance("W3", "HarborPanvel", "Down", 1500, true);
		TrainFactory.getTrainInstance("C3", "HarborAndheri", "Up", 1500, true);
		
		TrainStatus trainStatus = new TrainStatus(TrainFactory.getTrainsList(), 60000);
		trainStatus.getTrainsStatus();
		
		//StationStatus stationStatus = new StationStatus(StationFactory.getStationsList(), 60000);
		//stationStatus.getStationsStatus();
		
		//Intersection westernCentralIntersection = new Intersection("Western-Central1", westernSlowLine, centralSlowLine, "Dadar");
	}
	
}
