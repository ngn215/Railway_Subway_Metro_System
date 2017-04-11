package Concrete;

import java.util.HashMap;
import java.util.List;

public class Stops {
	
	private final String name;
	private final List<Station> stopsList;
	private final HashMap<Station, Integer> stopsMap = new HashMap<Station, Integer>();
	
	private Stops(String name, List<Station> stopsList)
	{
		this.name = name;
		this.stopsList = stopsList;
		populateStopsMap();
	}
	
	public static Stops getInstance(String name, List<Station> stopsList)
	{
		//validate
		if (name == null || name.isEmpty())
			throw new IllegalArgumentException("Argument : name cannot be " + (name == null ? "null" : "empty"));
		
		if (stopsList == null || stopsList.isEmpty())
			throw new IllegalArgumentException("Argument : stopsList cannot be " + (stopsList == null ? "null" : "empty"));
		
		//return stops instance
		return new Stops(name, stopsList);
	}
	
	public String getName() {
		return name;
	}
	
	public void populateStopsMap()
	{
		for(int i=0; i < stopsList.size(); i++)
			stopsMap.put(stopsList.get(i), i);
	}
	
	public boolean isAStop(Station station)
	{
		return stopsMap.containsKey(station);
	}
	
	public Station getFirstStop(boolean directionUp)
	{
		if (directionUp)
			return stopsList.get(0);
		else
			return stopsList.get(stopsList.size() - 1);
	}
	
	public Station getNextStop(Station currentStop, boolean directionUp)
	{
		if (currentStop == null)
			return getFirstStop(directionUp);
		
		if (stopsMap.containsKey(currentStop))
		{
			int currentIndex = stopsMap.get(currentStop);
			
			if (directionUp)
			{
				if (currentIndex < stopsList.size() - 1)
					return stopsList.get(currentIndex + 1);
				else
					return stopsList.get(currentIndex - 1);
			}
			else
			{
				if (currentIndex > 0)
					return stopsList.get(currentIndex - 1);
				else
					return stopsList.get(currentIndex + 1);
			}
		}
		
		return null;
	}
	
	public boolean isLastStop(Station currentStop, boolean directionUp)
	{
		if (stopsMap.containsKey(currentStop))
		{
			int currentIndex = stopsMap.get(currentStop);
			
			if (directionUp)
			{
				if (currentIndex == stopsList.size() - 1)
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
}
