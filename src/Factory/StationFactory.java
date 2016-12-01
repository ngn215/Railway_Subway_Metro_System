package Factory;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import Concrete.Station;


public class StationFactory {

	private static HashMap<String, Station> stationsHashMap = new HashMap<String, Station>();
	private final static String STATIONSLISTINPUTFILE = "InputFiles/StationsList.txt";
	
	public StationFactory()
	{
		populateStationsHashMap();
	}
	
	public static Station getStationInstance(String stationName)
	{
		if (stationsHashMap.containsKey(stationName))
			return stationsHashMap.get(stationName);
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
	
	private static void populateStationsHashMap()
	{
		List<String> stations = getStationsListFromInputFile();
		
		for(String station : stations)
		{
			if(station.equals("Dadar") || station.equals("Matunga"))
				stationsHashMap.put(station, createStationInstance(station, 5));
			else
				stationsHashMap.put(station, createStationInstance(station, 2));
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
		catch(Exception e)
		{
			System.out.println(e);
		}
		finally
		{
			scanner.close();
		}
		
		return stationsList;
	}
}
