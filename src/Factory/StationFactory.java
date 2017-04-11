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


public class StationFactory {

	private final static HashMap<String, Station> stationsMap = new HashMap<String, Station>();
	private final static List<Station> stationsList = new ArrayList<Station>();
	private final static String STATIONSLISTINPUTFILE = "InputFiles/StationsList.txt";
	private final static AsynchronousLogger asyncLogger = CustomLoggerFactory.getAsynchronousLoggerInstance();
	private static boolean factoryInitialized = false;
	
	private StationFactory()
	{
		//do nothing
	}
	
	public static synchronized boolean initializeFactory()
	{
		//makes sure we only initialize factory once
		if (!factoryInitialized)
		{
			factoryInitialized = true;
			populateStationsMapAndList();
			return true;
		}
		
		return false;
	}
	
	public static HashMap<String, Station> getStationsMap() 
	{
		return stationsMap;
	}
	
	public static List<Station> getStationsList()
	{
		return stationsList;
	}

	private static void populateStationsMapAndList()
	{
		getStationsListFromInputFile();
	}
	
	public static Station getStationInstance(String stationName)
	{				
		if (stationsMap.containsKey(stationName))
			return stationsMap.get(stationName);
		
		throw new IllegalStateException("Station could not be found using provided stationName : " + (stationName == null ? "null":stationName));
	}
	
	private static void getStationsListFromInputFile()
	{
		Path path = null;
		Scanner scanner = null;
		
		try
		{
			path = Paths.get(STATIONSLISTINPUTFILE);
			scanner =  new Scanner(path);
			while (scanner.hasNextLine())
		    {
				String stationName = scanner.nextLine();
				Station station = null;
				
				if (!stationName.equals("") && !stationName.startsWith("-"))
				{					
					if(stationName.equals("Dadar") || stationName.equals("Matunga"))
					{
						station = createStationInstance(stationName, 5);
					}
					else if(stationName.equals("Churchgate") || stationName.equals("Chhatrapati Shivaji Terminus") 
							|| stationName.equals("Kalyan"))
					{
						station = createStationInstance(stationName, 3);
					}
					else
					{
						station = createStationInstance(stationName, 2);
					}
					
					stationsMap.put(stationName, station);
					stationsList.add(station);
				}
		    }      
		}
		catch(IOException e)
		{
			e.printStackTrace();
			asyncLogger.log(Arrays.toString(e.getStackTrace()));
			throw new RuntimeException("Exception while getting Stations list from file.");
		}
		finally
		{
			if (scanner != null)
				scanner.close();
		}
	}
	
	private static Station createStationInstance(String stationName, int numberOfPlatforms)
	{	
		return Station.getInstance(stationName, numberOfPlatforms);
	}

}
