package Factory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import Concrete.Station;


public class StationFactory {

	private static HashMap<String, Station> stationsHashMap = new HashMap<String, Station>();
	
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
		List<String> stations = getStationsArray();
		
		for(String station : stations)
		{
			if(station.equals("Dadar") || station.equals("Matunga"))
				stationsHashMap.put(station, createStationInstance(station, 5));
			else
				stationsHashMap.put(station, createStationInstance(station, 2));
		}
	}
	
	public static List<String> getStationsArray()
	{
		List<String> stations = Arrays.asList("Churchgate",
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
									"Dahanu Road",
									//------
									"Masjid",
									"Sandhurst Road",
									"Byculla",
									"Chinchpokli",
									"Currey Road",
									"Parel",
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
		
		return stations;
	}
}
