package Factory;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import Concrete.Line;
import Concrete.Station;

public class LineFactory {
	
	private final static HashMap<String, Line> linesMap = new HashMap<String, Line>();
	private final static List<Line> linesList = new ArrayList<Line>();
	private final static String LINESLISTINPUTFILE = "InputFiles/LinesList.txt";
	private final static String DELIMITER = ",";
	
	private LineFactory()
	{
		//do nothing
	}
	
	public static void initializeFactory()
	{
		populateLinesMap();
	}
	
	private static void populateLinesMap()
	{
		getLinesListFromInputFile();
	}
	
	public static Line getLineInstance(String lineName)
	{
		if (linesMap.containsKey(lineName))
		{
			return linesMap.get(lineName);
		}
		else
		{
			System.out.println("ERROR : " + lineName + " not initialized !!");
			return null;
		}
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
							//System.out.println("Line name : " + lineName);
						}
						else
						{
							Station stationInstance = StationFactory.getStationInstance(tokensFromLine[i]);
							stationsList.add(stationInstance);
							//System.out.println(tokensFromLine[i]);
						}
					}
					
					Line line = new Line(lineName, stationsList);
					linesMap.put(lineName, line);
					linesList.add(line);
					//line.printStationsList();
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
	}
	
	public static String getLineName(Station sourceStationName, Station destinationStationName)
	{
		if(linesList.isEmpty()) 
		{
			System.out.println("ERROR : lines have not been initialized !!");
			return null;
		}
		
		for(Line line : linesList) 
		{
			//System.out.println(line.hasStation(sourceStationName) + " " + line.hasStation(sourceStationName));
			
			if (line.hasStation(sourceStationName) && line.hasStation(destinationStationName))
				return line.getName();
		}
		
		return null;
	}
	
	public static boolean getDirection(String lineName, Station sourceStationName, Station destinationStationName)
	{
		if(linesMap.isEmpty() || !linesMap.containsKey(lineName))
		{
			System.out.println("ERROR : " + lineName + " is not initialized in the map");
			return false;
		}
		
		Line line = linesMap.get(lineName);
			
		if (line.getDirection(sourceStationName, destinationStationName))
			return true;
		
		return false;
	}

}
