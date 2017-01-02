import java.util.Arrays;

import Concrete.AsynchronousLogger;
import Factory.CustomLoggerFactory;
import Factory.ExecutorServiceFactory;
import Factory.LineFactory;
import Factory.PersonFactory;
import Factory.StationFactory;
import Factory.TrainFactory;
import Status.PersonStatus;
import Status.ThreadStatus;
import Status.TrainStatus;

public class RailwaySubwayMetroSystem {

	private static AsynchronousLogger asyncLogger = CustomLoggerFactory.getAsynchronousLoggerInstance();
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
				
		try
		{
			//initialize single thread executor service
			ExecutorServiceFactory.createAndExecuteSingleThreadExecutor(asyncLogger);
			
			//initialize station, line and person factory.
			StationFactory.initializeFactory();
			LineFactory.initializeFactory();
			PersonFactory.randomlyGeneratePersons(100, "WesternSlow");
			PersonFactory.randomlyGeneratePersons(100, "CentralSlow");
			PersonFactory.randomlyGeneratePersons(100, "HarborPanvel");
			PersonFactory.randomlyGeneratePersons(100, "HarborAndheri");
			PersonFactory.closePersonsListFile();
			
			TrainFactory.setNumberOfTrains(8);
			
			TrainFactory.createTrainInstance("WS1", "WesternSlow", "Up", 1000);
			TrainFactory.createTrainInstance("WF1", "WesternFast", "Up", 500);
			TrainFactory.createTrainInstance("CS1", "CentralSlow", "Up", 1000);
			TrainFactory.createTrainInstance("CF1", "CentralFast", "Down", 100);
			
			Thread.sleep(3000);
			
			TrainFactory.createTrainInstance("WS2", "WesternSlow", "Down", 1500);
			TrainFactory.createTrainInstance("CS2", "CentralSlow", "Up", 1500);
			
			Thread.sleep(3000);
			
			TrainFactory.createTrainInstance("HP1", "HarborPanvel", "Down", 1000, 10);
			TrainFactory.createTrainInstance("HA1", "HarborAndheri", "Up", 1000, 10);
			
			TrainStatus trainStatus = new TrainStatus(TrainFactory.getTrainsList());
			trainStatus.getStatus(60000);
			
			PersonStatus personStatus = new PersonStatus(PersonFactory.getPersonsList());
			personStatus.getStatus(20000);
			
			Thread.sleep(300);
			
			ThreadStatus threadStatus = new ThreadStatus();
			threadStatus.getStatus(20000);
			
			//StationStatus stationStatus = new StationStatus(StationFactory.getStationsList());
			//stationStatus.getStatus(60000);
			
			//Intersection westernCentralIntersection = new Intersection("Western-Central1", westernSlowLine, centralSlowLine, "Dadar");
			
			//wait till are people are still waiting
			while(PersonFactory.arePeopleStillWaitingForTrains())
			{
				Thread.sleep(3000);
			}
			
		}
		catch(Exception e)
		{
			asyncLogger.log("Exception in main class" , true);
			e.printStackTrace();
			asyncLogger.log(Arrays.toString(e.getStackTrace()));
		}
		finally
		{
			ExecutorServiceFactory.shutDownAllExecutors();
			ExecutorServiceFactory.shutDownAllAsyncLoggerExecutors();
		}
	}
}
