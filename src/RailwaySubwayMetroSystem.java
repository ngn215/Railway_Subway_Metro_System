import java.util.Arrays;

import Concrete.AsynchronousLogger;
import Factory.CustomLoggerFactory;
import Factory.ExecutorServiceFactory;
import Factory.LineFactory;
import Factory.LinesStopsFactory;
import Factory.PersonFactory;
import Factory.StationFactory;
import Factory.StopsFactory;
import Factory.TrainFactory;
import Status.PersonStatus;
import Status.StationStatus;
import Status.ThreadStatus;
import Status.TrainStatus;

public class RailwaySubwayMetroSystem {

	private static AsynchronousLogger asyncLogger = CustomLoggerFactory.getAsynchronousLoggerInstance();
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		long startTimeMS = System.currentTimeMillis();
		
		try
		{			
			//initialize single thread executor service
			ExecutorServiceFactory.createAndExecuteSingleThreadExecutor(asyncLogger);
			
			//initialize station, line and person factory.
			StationFactory.initializeFactory();
			StopsFactory.initializeFactory();
			LinesStopsFactory.initializeFactory();
			LineFactory.initializeFactory();
			
			PersonFactory.randomlyGeneratePersons(5000, "Western");
			PersonFactory.randomlyGeneratePersons(5000, "Central");
			PersonFactory.randomlyGeneratePersons(5000, "HarborPanvel");
			PersonFactory.randomlyGeneratePersons(1000, "HarborAndheri");
			/*for (int i=0; i < 5000; i++)
			{
				PersonFactory.generatePersonAtStationWithDestination("Malad", "Goregaon");
				PersonFactory.generatePersonAtStationWithDestination("Goregaon", "Jogeshwari");
				PersonFactory.generatePersonAtStationWithDestination("Jogeshwari", "Vile Parle");
				PersonFactory.generatePersonAtStationWithDestination("Vile Parle", "Santacruz");
			}
			*/
			PersonFactory.closePersonsListFile();
			
			TrainFactory.createTrainInstance("VirarFast", "Western", "Up", 500, "Western-VIRCCGT-Fast");
			TrainFactory.createTrainInstance("WS1", "Western", "Up", 1000, "Western-Slow");
			TrainFactory.createTrainInstance("CS1", "Central", "Up", 1000, "Central-Slow");
			TrainFactory.createTrainInstance("CF1", "Central", "Down", 200, "Central-Fast");
			
			Thread.sleep(3000);
			
			TrainFactory.createTrainInstance("BorivaliFast", "Western", "Down", 400, "Western-BORCCGT-Fast");
			TrainFactory.createTrainInstance("CS2", "Central", "Up", 1500, "Central-Slow");
			
			Thread.sleep(3000);
			
			TrainFactory.createTrainInstance("HP1", "HarborPanvel", "Down", 1000, "HarborPanvel", 15);
			TrainFactory.createTrainInstance("HA1", "HarborAndheri", "Up", 1000, "HarborAndheri", 15);
			
			TrainStatus trainStatus = new TrainStatus(TrainFactory.getTrainsList());
			trainStatus.getStatus(20000);
			
			PersonStatus personStatus = new PersonStatus(PersonFactory.getPersonsList());
			personStatus.getStatus(20000);
			
			Thread.sleep(300);
			
			ThreadStatus threadStatus = new ThreadStatus();
			threadStatus.getStatus(30000);
			
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
			TrainFactory.shutDownAllTrainThreads();
			PersonFactory.shutDownAllPersonThreads();
			ExecutorServiceFactory.shutDownAllAsyncLoggerExecutors();
			
			long endTimeMS = System.currentTimeMillis();
			
			System.out.println("Total time taken for execution : " + (endTimeMS - startTimeMS) + " ms");
			System.out.println("Total time taken for execution : " + ((endTimeMS - startTimeMS) / 1000.0 / 60.0) + " minutes");
		}
	}
}
