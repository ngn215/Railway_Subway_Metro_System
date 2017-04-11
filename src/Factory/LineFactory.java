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
import Concrete.Line;
import Concrete.Station;
import Concrete.Stops;

public class LineFactory {
	
	private final static HashMap<String, Line> linesMap = new HashMap<String, Line>();
	private final static List<Line> linesList = new ArrayList<Line>();
	private final static String LINESLISTINPUTFILE = "InputFiles/LinesList.txt";
	private final static String DELIMITER = ",";
	private final static AsynchronousLogger asyncLogger = CustomLoggerFactory.getAsynchronousLoggerInstance();
	private static boolean factoryInitialized = false;
	
	private LineFactory()
	{
		//do nothing
	}
	
	public static synchronized boolean initializeFactory()
	{
		//makes sure we only initialize factory once
		if (!factoryInitialized)
		{
			factoryInitialized = true;
			populateLinesMap();
			return true;
		}
		
		return false;
	}
	
	private static void populateLinesMap()
	{
		getLinesListFromInputFile();
	}
	
	public static Line getLineInstance(String lineName)
	{		
		if (linesMap.containsKey(lineName))
			return linesMap.get(lineName);
		
		throw new IllegalStateException("Line could not be found using provided lineName : " + (lineName == null ? "null":lineName));
	}
	
	public static List<Line> getLinesList()
	{
		return linesList;
	}
	
	private static void getLinesListFromInputFile()
	{
		Path path = null;
		Scanner scanner = null;
		String lineName = null;
		
		try
		{
			path = Paths.get(LINESLISTINPUTFILE);
			scanner =  new Scanner(path);
			while (scanner.hasNextLine())
		    {
				String lineFromFile = scanner.nextLine();
				if (!lineFromFile.equals("") && !lineFromFile.startsWith("-"))
				{
					List<Station> stationsList = new ArrayList<Station>();
					String[] tokensFromLine = lineFromFile.split(DELIMITER);
					
					for(int i=0; i<tokensFromLine.length; i++)
					{
						if (i==0)
						{
							lineName = tokensFromLine[i];
						}
						else
						{
							Station stationInstance = StationFactory.getStationInstance(tokensFromLine[i]);
							stationsList.add(stationInstance);
						}
					}
					
					Line line = createLineInstance(lineName, stationsList, LinesStopsFactory.getStopsMap(lineName));
					
					linesMap.put(lineName, line);
					linesList.add(line);
				}
		    }      
		}
		catch(IOException e)
		{
			e.printStackTrace();
			asyncLogger.log(Arrays.toString(e.getStackTrace()));
			throw new RuntimeException("Exception while getting Lines list from file.");
		}
		finally
		{
			if (scanner != null)
				scanner.close();
		}
	}
	
	private static Line createLineInstance(String lineName, List<Station> stationsList,HashMap<String, Stops> stopsMap)
	{				
		return Line.getInstance(lineName, stationsList, stopsMap);
	}
	
	public static Line getLineFromSourceAndDestination(Station sourceStation, Station destinationStation)
	{
		//we dont need these validation because
		//the last IllegalStateException will take care of all below cases :
		//if line was not found then either we passed in wrong parameters or linesList was not initialized properly
		//linesList is empty.
		/*if (sourceStation == null)
			throw new IllegalArgumentException("Argument : sourceStation cannot be null");
		
		if (destinationStation == null)
			throw new IllegalArgumentException("Argument : destinationStation cannot be null");
		
		if(linesList.isEmpty()) 
			throw new IllegalStateException("Lines have not been initialized");*/
		
		for(Line line : linesList) 
		{
			//System.out.println(line.hasStation(sourceStationName) + " " + line.hasStation(sourceStationName));
			
			if (line.hasStation(sourceStation) && line.hasStation(destinationStation))
				return line;
		}
		
		throw new IllegalStateException("Couldnt find any line for the provided source and destination station");
	}
	
	public static boolean getDirectionFromSourceAndDestination(Line line, Station sourceStation, Station destinationStation)
	{
		//we dont need below validation because
		//if invalid arguments are passed then getDirection will complain
		/*if (sourceStation == null)
			throw new IllegalArgumentException("Argument : sourceStation cannot be null");
		
		if (destinationStation == null)
			throw new IllegalArgumentException("Argument : destinationStation cannot be null");*/
		
		assert line != null;
		
		return line.getDirection(sourceStation, destinationStation);
	}

}
