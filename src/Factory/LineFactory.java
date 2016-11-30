package Factory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import Concrete.Line;
import Concrete.Station;

public class LineFactory {
	
	private static HashMap<String, Line> linesMap = new HashMap<String, Line>();
	
	public LineFactory()
	{
		populateLinesMap();
	}
	
	private void populateLinesMap()
	{
		populateSlowLines();
		populateExpressLines();
	}
	
	private void populateSlowLines()
	{
		populateLineMap("WesternSlow");
		populateLineMap("CentralSlow");
	}
	
	private void populateExpressLines()
	{
		populateLineMap("WesternFast");
		populateLineMap("CentralFast");
	}
	
	private void populateLineMap(String lineName)
	{
		List<String> stationsList = null;
		List<Station> stations = null;
		
		if (lineName.equals("WesternSlow"))
			stationsList = getStationsWesternSlowLine();
		else if (lineName.equals("CentralSlow"))
			stationsList = getStationsCentralSlowLine();
		else if (lineName.equals("WesternFast"))
			stationsList = getStationsWesternFastLine();
		else if (lineName.equals("CentralFast"))
			stationsList = getStationsCentralFastLine();
			
		stations = new ArrayList<Station>();
		
		for(int i=0; i<stationsList.size(); i++)
		{			
			stations.add(StationFactory.getStationInstance(stationsList.get(i)));
		}
		
		Line line = new Line(lineName, stations);
		linesMap.put(lineName, line);
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
	
	public static List<String> getStationsWesternSlowLine()
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
	}
	
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
