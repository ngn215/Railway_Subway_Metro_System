package Factory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import Concrete.AsynchronousLogger;
import Concrete.Station;
import Concrete.Stops;

public class StopsFactory {

	private final static HashMap<String, Stops> stopsMap = new HashMap<String, Stops>();
	private final static List<Stops> stopsList = new ArrayList<Stops>();
	private final static String STOPSLISTINPUTFILE = "InputFiles/StopsList.txt";
	private final static String DELIMITER = ",";
	private final static AsynchronousLogger asyncLogger = CustomLoggerFactory.getAsynchronousLoggerInstance();
	private static boolean factoryInitialized = false;
	
	private StopsFactory()
	{
		//do nothing
	}
	
	public static synchronized boolean initializeFactory()
	{
		//makes sure we only initialize factory once
		if (!factoryInitialized)
		{
			factoryInitialized = true;
			populateStopsMapAndList();
			return true;
		}
		
		return false;
	}
	
	private static void populateStopsMapAndList()
	{
		getStopsFromInputFile();
	}
	
	public static Stops getStopsInstance(String stopsName)
	{	
		if (stopsMap.containsKey(stopsName))
			return stopsMap.get(stopsName);

		throw new IllegalStateException("Stops could not be found using provided stopsName : " + (stopsName == null ? "null":stopsName));
	}
	
	public static List<Stops> getStopsList()
	{
		return stopsList;
	}
	
	private static void getStopsFromInputFile()
	{
		Path path = null;
		Scanner scanner = null;
		
		String stopsName = null;
		
		try
		{
			path = Paths.get(STOPSLISTINPUTFILE);
			scanner =  new Scanner(path);
			while (scanner.hasNextLine())
		    {
				String lineFromFile = scanner.nextLine();
				if (!lineFromFile.equals("") && !lineFromFile.startsWith("-"))
				{
					List<Station> stoppingStationsList = new ArrayList<Station>();
					String[] tokensFromLine = lineFromFile.split(DELIMITER);
					
					for(int i=0; i<tokensFromLine.length; i++)
					{
						if (i==0)
						{
							stopsName = tokensFromLine[i];
						}
						else
						{
							Station stationInstance = StationFactory.getStationInstance(tokensFromLine[i]);
							stoppingStationsList.add(stationInstance);
						}
					}
					
					Stops stops = createStopInstance(stopsName, stoppingStationsList);
					stopsMap.put(stopsName, stops);
					stopsList.add(stops);
				}
		    }      
		}
		catch(IOException e)
		{
			e.printStackTrace();
			asyncLogger.log(Arrays.toString(e.getStackTrace()));
			throw new RuntimeException("Exception while getting Stops list from file.");
		}
		finally
		{
			if (scanner != null)
				scanner.close();
		}
	}
	
	private static Stops createStopInstance(String stopsName, List<Station> stoppingStationsList)
	{	
		return Stops.getInstance(stopsName, stoppingStationsList);
	}
}
