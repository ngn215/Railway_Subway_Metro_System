package Concrete;
import java.util.HashMap;
import java.util.List;


public class Line {
	
	String name;
	List<Station> stationsList;
	private HashMap<Station, Integer> stationsHashMap = new HashMap<Station, Integer>();
	
	public Line(String name, List<Station> stationsList)
	{
		this.name = name;
		this.stationsList = stationsList;
		
		buildHashMap();
	}
	
	public String getName() {
		return name;
	}

	public List<Station> getStationsList() {
		return stationsList;
	}

	private void buildHashMap()
	{
		for(int i=0; i < stationsList.size(); i++)
			stationsHashMap.put(stationsList.get(i), i);
	}
	
	public Station getFirstStation(boolean directionUp)
	{
		if (directionUp)
			return stationsList.get(0);
		else
			return stationsList.get(stationsList.size() - 1);
	}
	
	public Station getNextStation(Station station, boolean directionUp)
	{
		if (stationsHashMap.containsKey(station))
		{
			int currentIndex = stationsHashMap.get(station);
			
			if (directionUp)
			{
				if (currentIndex < stationsList.size() - 1)
					return stationsList.get(currentIndex + 1);
				else
					return stationsList.get(currentIndex - 1);
			}
			else
			{
				if (currentIndex > 0)
					return stationsList.get(currentIndex - 1);
				else
					return stationsList.get(currentIndex + 1);
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
				if (currentIndex == stationsList.size() - 1)
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
	
	public void printStationsList()
	{
		System.out.println("Printing stations list for line : " + this.name);
		for(Station station : stationsList)
		{
			System.out.println(station.getName());
		}
	}

}
