//import Concrete.Intersection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import Concrete.AsynchronousLogger;
import Factory.CustomLoggerFactory;
import Factory.LineFactory;
import Factory.PersonFactory;
import Factory.StationFactory;
import Factory.TrainFactory;
import Status.PersonStatus;
import Status.StationStatus;
import Status.TrainStatus;

public class RailwaySubwayMetroSystem {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		//get asynchronous logger instance
		AsynchronousLogger asyncLogger = CustomLoggerFactory.getAsynchronousLoggerInstance();
		
		//initialize single thread executor service
		ExecutorService executor = Executors.newSingleThreadExecutor();
		
		//submit execute request
		executor.execute(asyncLogger);
		
		//initialize station, line and person factory.
		StationFactory.initializeFactory();
		LineFactory.initializeFactory();
		PersonFactory.randomlyGeneratePersons(10000, "WesternSlow");
		PersonFactory.randomlyGeneratePersons(10000, "CentralSlow");
		PersonFactory.randomlyGeneratePersons(10000, "HarborPanvel");
		PersonFactory.randomlyGeneratePersons(2000, "HarborAndheri");
		
		TrainFactory.createTrainInstance("W1", "WesternSlow", "Up", 1000, true);
		TrainFactory.createTrainInstance("W2", "WesternFast", "Up", 500, true);
		TrainFactory.createTrainInstance("C1", "CentralSlow", "Up", 1000, true);
		TrainFactory.createTrainInstance("C2", "CentralFast", "Down", 100, true);
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		TrainFactory.createTrainInstance("W3", "WesternSlow", "Down", 1500, true);
		TrainFactory.createTrainInstance("C3", "CentralSlow", "Up", 1500, true);
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		TrainFactory.createTrainInstance("W3", "HarborPanvel", "Down", 1500, true);
		TrainFactory.createTrainInstance("C3", "HarborAndheri", "Up", 1500, true);
		
		TrainStatus trainStatus = new TrainStatus(TrainFactory.getTrainsList());
		trainStatus.getStatus(60000);
		
		PersonStatus personStatus = new PersonStatus(PersonFactory.getPersonsList());
		personStatus.getStatus(20000);
		
		//StationStatus stationStatus = new StationStatus(StationFactory.getStationsList());
		//stationStatus.getStatus(60000);
		
		//Intersection westernCentralIntersection = new Intersection("Western-Central1", westernSlowLine, centralSlowLine, "Dadar");
		
		//wait till are trains are running
		while(TrainFactory.areTrainsRunning())
		{
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//shutting down executor
		try 
		{
		    System.out.println("Attempt to shutdown executor");
		    executor.shutdown();
		    asyncLogger.shutDown();
		    executor.awaitTermination(5, TimeUnit.SECONDS);
		}
		catch (InterruptedException e) 
		{
		    System.err.println("Tasks interrupted");
		}
		finally 
		{
		    if (!executor.isTerminated()) 
		    {
		        System.err.println("Cancel non-finished tasks");
		        executor.shutdownNow();
			    System.out.println("Shutdown finished");
		    }
		}
	}
}
