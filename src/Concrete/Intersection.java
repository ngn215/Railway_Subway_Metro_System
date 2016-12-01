package Concrete;

public class Intersection {

	//WORK IN PROGRESS
	
	String name;
	Line line1;
	Line line2;
	String intersectingStation;
	
	public Intersection(String name,Line line1, Line line2, String intersectingStation)
	{
		this.name = name;
		this.line1 = line1;
		this.line2 = line2;
		this.intersectingStation = intersectingStation;
	}
}
