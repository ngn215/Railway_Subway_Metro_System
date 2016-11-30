package Concrete;
import java.util.HashMap;
import java.util.List;


public class Line {
	
	String name;
	List<Station> stations;
	private HashMap<Station, Integer> stationsHashMap = new HashMap<Station, Integer>();
	
	public Line(String name, List<Station> stations)
	{
		this.name = name;
		this.stations = stations;
		
		buildHashMap();
	}
	
	public String getName() {
		return name;
	}

	private void buildHashMap()
	{
		for(int i=0; i < stations.size(); i++)
			stationsHashMap.put(stations.get(i), i);
	}
	
	public Station getFirstStation(boolean directionUp)
	{
		if (directionUp)
			return stations.get(0);
		else
			return stations.get(stations.size() - 1);
	}
	
	public Station getNextStation(Station station, boolean directionUp)
	{
		if (stationsHashMap.containsKey(station))
		{
			int currentIndex = stationsHashMap.get(station);
			
			if (directionUp)
			{
				if (currentIndex < stations.size() - 1)
					return stations.get(currentIndex + 1);
				else
					return stations.get(currentIndex - 1);
			}
			else
			{
				if (currentIndex > 0)
					return stations.get(currentIndex - 1);
				else
					return stations.get(currentIndex + 1);
			}
		}
		
		return null;
	}
	
	public boolean isLastStation(Station station, boolean directionUp)
	{
		if (stationsHashMap.containsKey(station))
		{
			int currentIndex = stationsHashMap.get(station);
			
			if (directionUp)
			{
				if (currentIndex == stations.size() - 1)
					return true;
				else
					return false;
			}
			else
			{
				if (currentIndex == 0)
					return true;
				else
					return false;
			}
		}
		
		return false;
	}
	
	public boolean hasStation(Station stationName)
	{
		if (stationsHashMap.isEmpty())
		{
			System.out.println("ERROR : stationsHashMap not initialized !!");
			return false;
		}
		
		if (stationsHashMap.containsKey(stationName))
			return true;
		else
			return false;
	}
	
	public boolean getDirection(Station sourceStationName, Station destinationStationName)
	{		
		int sourceStationPosition = stationsHashMap.get(sourceStationName);
		int destinationStationPosition = stationsHashMap.get(destinationStationName);
		
		if(sourceStationPosition < destinationStationPosition)
			return true;
		else
			return false;
	}

}
