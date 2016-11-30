package LockerClasses;

import Concrete.Station;


public class MyLocks {
	
	Station station;
	String lockType;
	
	MyLocks(Station station)
	{
		this.station = station;
		this.lockType = "Person-Station";
	}

}
