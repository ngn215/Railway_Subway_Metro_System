package Factory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import Concrete.AsynchronousLogger;
import Concrete.Stops;

public class LinesStopsFactory {

	private final static HashMap<String, HashMap<String, Stops>> lineStopsMap = new HashMap<String, HashMap<String, Stops>>();
	private final static String LINESSTOPSLISTINPUTFILE = "InputFiles/LinesStops.txt";
	private final static String DELIMITER = ",";
	private final static AsynchronousLogger asyncLogger = CustomLoggerFactory.getAsynchronousLoggerInstance();
	private static boolean factoryInitialized = false;
	
	private LinesStopsFactory()
	{
		//do nothing
	}
	
	public static synchronized boolean initializeFactory()
	{
		//makes sure we only initialize factory once
		if (!factoryInitialized)
		{
			factoryInitialized = true;
			populateLinesStopsMap();
			return true;
		}
		
		return false;
	}
	
	private static void populateLinesStopsMap()
	{
		getLinesStopsFromInputFile();
	}
	
	public static HashMap<String, Stops> getStopsMap(String lineName)
	{		
		if (lineStopsMap.containsKey(lineName))
			return lineStopsMap.get(lineName);
		
		throw new IllegalStateException("StopsMap could not be found using provided lineName : " + (lineName == null ? "null":lineName));
	}
	
	private static void getLinesStopsFromInputFile()
	{
		Path path = null;
		Scanner scanner = null;
		
		try
		{
			path = Paths.get(LINESSTOPSLISTINPUTFILE);
			scanner =  new Scanner(path);
			while (scanner.hasNextLine())
		    {
				String lineName = null;
				Stops stops = null;
				String lineFromFile = scanner.nextLine();
				if (!lineFromFile.equals("") && !lineFromFile.startsWith("-"))
				{
					String[] tokensFromLine = lineFromFile.split(DELIMITER);
					
					for(int i=0; i<tokensFromLine.length; i++)
					{
						if (i==0)
						{
							lineName = tokensFromLine[i];
						}
						else
						{
							stops = StopsFactory.getStopsInstance(tokensFromLine[i]);
						}
					}
					
					if (lineStopsMap.containsKey(lineName))
					{
						lineStopsMap.get(lineName).put(stops.getName(), stops);
					}
					else
					{
						HashMap<String, Stops> hashMap = new HashMap<String, Stops>();
						hashMap.put(stops.getName(), stops);
						lineStopsMap.put(lineName, hashMap);
					}
					
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
}
