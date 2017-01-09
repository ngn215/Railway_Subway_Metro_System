package Factory;

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
	
	private StopsFactory()
	{
		//do nothing
	}
	
	public static void initializeFactory()
	{
		populateStopsMap();
	}
	
	private static void populateStopsMap()
	{
		getStopsListFromInputFile();
	}
	
	public static Stops getStopsInstance(String stopsName)
	{
		if (stopsMap.containsKey(stopsName))
		{
			return stopsMap.get(stopsName);
		}
		else
		{
			asyncLogger.log("ERROR : " + stopsName + " not initialized !!", true);
			return null;
		}
	}
	
	public static List<Stops> getStopsList()
	{
		return stopsList;
	}
	
	private static void getStopsListFromInputFile()
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
							//System.out.println("Line name : " + lineName);
						}
						else
						{
							Station stationInstance = StationFactory.getStationInstance(tokensFromLine[i]);
							stoppingStationsList.add(stationInstance);
							//System.out.println(tokensFromLine[i]);
						}
					}
					
					Stops stops = new Stops(stopsName, stoppingStationsList);
					stopsMap.put(stopsName, stops);
					stopsList.add(stops);
					//line.printStationsList();
				}
		    }      
		}
		catch(Exception e)
		{
			System.out.println(e);
			e.printStackTrace();
			asyncLogger.log(Arrays.toString(e.getStackTrace()));
		}
		finally
		{
			scanner.close();
		}
	}
}
