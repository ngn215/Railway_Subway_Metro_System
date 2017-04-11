package Concrete;

import java.util.HashMap;
import java.util.List;


public class Line {
	
	private String name;
	private final List<Station> stationsList;
	private final HashMap<Station, Integer> stationsHashMap;
	private final HashMap<String, Stops> stopsMap;
	
	private Line(String name, List<Station> stationsList, HashMap<String, Stops> stopsMap)
	{
		this.name = name;
		this.stationsList = stationsList;
		this.stationsHashMap = new HashMap<Station, Integer>();
		this.stopsMap = stopsMap;
		
		buildHashMap();
	}
	
	public static Line getInstance(String name, List<Station> stationsList, HashMap<String, Stops> stopsMap)
	{
		//validate
		if (name == null || name.isEmpty())
			throw new IllegalArgumentException("Argument : name cannot be " + (name == null ? "null" : "empty"));
		
		if (stationsList == null || stationsList.isEmpty())
			throw new IllegalArgumentException("Argument : stationsList cannot be " + (stationsList == null ? "null" : "empty"));
		
		if (stopsMap == null || stopsMap.isEmpty())
			throw new IllegalArgumentException("Argument : stopsMap cannot be " + (stopsMap == null ? "null" : "empty"));
		
		//return line instance
		return new Line(name, stationsList, stopsMap);
	}
	
	public String getName() {
		return name;
	}

	public List<Station> getStationsList() {
		return stationsList;
	}
	
	public Stops getStops(String stopsName)
	{
		return stopsMap.get(stopsName);
	}

	private void buildHashMap()
	{
		for(int i=0; i < stationsList.size(); i++)
			stationsHashMap.put(stationsList.get(i), i);
	}
	
	private Station getFirstStation(boolean directionUp)
	{
		if (directionUp)
			return stationsList.get(0);
		else
			return stationsList.get(stationsList.size() - 1);
	}
	
	public Station getNextStation(Station station, boolean directionUp)
	{
		//if current station is null then return first station
		if (station == null)
			return getFirstStation(directionUp);
		
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
		if (stationsHashMap.containsKey(stationName))
			return true;
		else
			return false;
	}
	
	public boolean getDirection(Station sourceStation, Station destinationStation)
	{		
		int sourceStationPosition = stationsHashMap.get(sourceStation);
		int destinationStationPosition = stationsHashMap.get(destinationStation);
		
		//we dont need below validation because station parameters are set on person by person factory while 
		//creating persons. while creating persons we get station from stationfactory which validates if it
		//is a valid station or not. if invalid station then we complain there itself.
		/*if (sourceStationPosition == null)
			throw new IllegalStateException("Couldnt find sourceStationPosition using passed in sourceStation : "  
												+ (sourceStation == null ? "null" : sourceStation.getName()));
		
		if (destinationStationPosition == null)
			throw new IllegalStateException("Couldnt find destinationStationPosition using passed in destinationStation : " 
												+ (destinationStation == null ? "null" : destinationStation.getName()));*/
		
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
