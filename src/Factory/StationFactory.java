package Factory;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

import Concrete.AsynchronousLogger;
import Concrete.Station;


public class StationFactory {

	private static HashMap<String, Station> stationsMap = new HashMap<String, Station>();
	private static List<Station> stationsList = new ArrayList<Station>();
	private final static String STATIONSLISTINPUTFILE = "InputFiles/StationsList.txt";
	private final static AsynchronousLogger asyncLogger = CustomLoggerFactory.getAsynchronousLoggerInstance();
	
	private StationFactory()
	{
		//do nothing
	}
	
	public static HashMap<String, Station> getStationsMap() 
	{
		return stationsMap;
	}
	
	public static List<Station> getStationsList()
	{
		return stationsList;
	}

	public static void initializeFactory()
	{
		populateStationsMapAndList();
	}
	
	public static Station getStationInstance(String stationName)
	{
		if (stationsMap.containsKey(stationName))
			return stationsMap.get(stationName);
		else
		{
			System.out.println("ERROR : Station Name not in map");
			return null;
		}
	}
	
	private static Station createStationInstance(String stationName, int numberOfPlatforms)
	{
		return new Station(stationName, numberOfPlatforms);
	}
	
	private static void populateStationsMapAndList()
	{
		List<String> stations = getStationsListFromInputFile();
		
		for(String stationName : stations)
		{
			Station station;
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
	
	private static List<String> getStationsListFromInputFile()
	{
		List<String> stationsList = new ArrayList<String>();
		Path path = null;
		Scanner scanner = null;
		
		try
		{
			path = Paths.get(STATIONSLISTINPUTFILE);
			scanner =  new Scanner(path);
			while (scanner.hasNextLine())
		    {
				String stationLine = scanner.nextLine();
				if (!stationLine.equals("") && !stationLine.startsWith("-"))
				{
					stationsList.add(stationLine);
					//System.out.println(line);
				}
		    }      
		}
		catch(InputMismatchException | IOException e)
		{
			asyncLogger.log(e.toString(), true);
			e.printStackTrace();
		}
		finally
		{
			scanner.close();
		}
		
		return stationsList;
	}
}
