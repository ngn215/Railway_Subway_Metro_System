package Factory;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import Concrete.Line;
import Concrete.Station;

public class LineFactory {
	
	private static HashMap<String, Line> linesMap = new HashMap<String, Line>();
	private final static String LINESLISTINPUTFILE = "InputFiles/LinesList.txt";
	private final static String DELIMITER = ",";
	
	public LineFactory()
	{
		populateLinesMap();
	}
	
	private void populateLinesMap()
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
					String[] tokensFromLine = lineFromFile.split(",");
					
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
	
/*	public static List<String> getStationsWesternSlowLine()
	{		
		List<String> stationsWesternLineList = Arrays.asList(
										"Churchgate",
										"Marine Lines",
										"Charni Road",
										"Grant Road",
										"Mumbai Central",
										"Mahalaxmi",
										"Lower Parel",
										"Elphinstone Road",
										"Dadar",
										"Matunga",
										"Mahim",
										"Bandra",
										"Khar Road",
										"Santacruz",
										"Vile Parle",
										"Andheri",
										"Jogeshwari",
										"Goregaon",
										"Malad",
										"Kandivali",
										"Borivali",
										"Dahisar",
										"Mira Road",
										"Bhayandar",
										"Naigaon",
										"Vasai Road",
										"Nala Sopara",
										"Virar",
										"Vaitarna",
										"Saphale",
										"Kelve Road",
										"Palghar",
										"Umroli",
										"Boisar",
										"Vangaon",
										"Dahanu Road"
										);
		
		return stationsWesternLineList;
	}
	
	public static List<String> getStationsCentralSlowLine()
	{
		List<String> stationsCentralLineList = Arrays.asList(
												"Masjid",
												"Sandhurst Road",
												"Byculla",
												"Chinchpokli",
												"Currey Road",
												"Parel",
												"Dadar",
												"Matunga",
												"Sion",
												"Kurla",
												"Vidyavihar",
												"Ghatkopar",
												"Vikhroli",
												"Kanjurmarg",
												"Bhandup",
												"Nahur",
												"Mulund",
												"Thane",
												"Kalwa",
												"Mumbra",
												"Diva",
												"Kopar",
												"Dombivli",
												"Thakurli",
												"Kalyan"
												);
		
		return stationsCentralLineList;
	}
	
	public static List<String> getStationsWesternFastLine()
	{		
		List<String> stationsWesternLineList = Arrays.asList(
										"Churchgate",
										"Dadar",
										"Bandra",
										"Andheri",
										"Borivali",
										"Virar",
										"Dahanu Road"
										);
		
		return stationsWesternLineList;
	}
	
	public static List<String> getStationsCentralFastLine()
	{
		List<String> stationsCentralLineList = Arrays.asList(
												"Masjid",
												"Dadar",
												"Ghatkopar",
												"Thane",
												"Dombivli",
												"Kalyan"
												);
		
		return stationsCentralLineList;
	}*/
	
	public static String getLineName(Station sourceStationName, Station destinationStationName)
	{
		if(linesMap.isEmpty()) 
		{
			System.out.println("ERROR : linesMap not initialized !!");
			return null;
		}
		
		Iterator iter = linesMap.entrySet().iterator();
		while (iter.hasNext()) 
		{
			Map.Entry pair = (Map.Entry)iter.next();
			Line line = (Line) pair.getValue();
			
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
