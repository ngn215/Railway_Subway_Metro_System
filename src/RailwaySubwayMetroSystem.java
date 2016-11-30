import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import Concrete.Intersection;
import Concrete.Line;
import Concrete.Person;
import Concrete.Station;
import Concrete.Train;
import Factory.LineFactory;
import Factory.StationFactory;

public class RailwaySubwayMetroSystem {

	HashSet<Person> persons = new HashSet<Person>();

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		RailwaySubwayMetroSystem railwaySubwaysystem = new RailwaySubwayMetroSystem();
		ArrayList<Train> trains = new ArrayList<Train>();
		
		//initialize stationfactory. this will populate hashmap
		StationFactory stationFactory = new StationFactory();
		
		LineFactory lineFactory = new LineFactory();
		Line westernSlowLine = LineFactory.getLineInstance("WesternSlow");
		Line centralSlowLine = LineFactory.getLineInstance("CentralSlow");
		Line westernFastLine = LineFactory.getLineInstance("WesternFast");
		Line centralFastLine = LineFactory.getLineInstance("CentralFast");
		
		Intersection westernCentralIntersection = new Intersection("Western-Central1", westernSlowLine, centralSlowLine, "Dadar");
		
		railwaySubwaysystem.addPersons(10000);
		
		Train w1 = new Train("W1", westernSlowLine, true, 1000);        
		railwaySubwaysystem.startTrain(w1);
		trains.add(w1);
		
		Train w2 = new Train("W2", westernFastLine, true, 1500);
		railwaySubwaysystem.startTrain(w2);
		trains.add(w2);
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Train w3 = new Train("W3", westernSlowLine, false, 500);
		railwaySubwaysystem.startTrain(w3);
		trains.add(w3);
		
		/*Train c1 = new Train("C1", centralLine, true, 1000);
		controller.startTrain(c1);
		trains.add(c1);
		
		Train c2 = new Train("C2", centralLine, false, 1500);
		controller.startTrain(c2);
		trains.add(c2);
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Train c3 = new Train("C3", centralLine, true, 100);
		controller.startTrain(c3);
		trains.add(c3);*/
		
		//controller.getTrainsAndStationsStatus(trains);
		
	}
	
	public Thread startTrain(Train t)
	{
		System.out.println("--- Starting Train : " + t.getName() + " ( " + t.getLineName() + " ) " + " ---");
		Thread thread = new Thread(t, "T" + t.getName());
		thread.start();
        
        return thread;
	}
	
	public void getTrainsAndStationsStatus(ArrayList<Train> trains)
	{
		while(true)
		{
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.println("---------------------S-T-A-T-U-S-------------------------------------");
			
			for(Object obj : trains)
			{
				Train train = (Train)obj;
				train.getTrainStatus();
			}
			
			int countOfPeopleNotReachedDestination = 0;
			for(Person person : persons)
			{
				if (!person.hasReachedDestination())
				{
					//person.getPersonStatus();
					countOfPeopleNotReachedDestination++;
				}
			}
			System.out.println("PERSON STATUS : number of persons yet to reach destination : " + countOfPeopleNotReachedDestination);
			
//			for(String station : StationFactory.getStationsArray())
//			{
//				StationFactory.getStationInstance(station).getStatus();
//			}
			
			System.out.println("-------------------------------------------------------------------");
		}
	}
	
	public void addPersons(int personsCount)
	{
		int i = 1;
		Random rn = new Random();
		
		while(i <= personsCount)
		{
			List<String> westernLineStationsList = LineFactory.getStationsWesternSlowLine();
			int listSize = westernLineStationsList.size();
			
			int index1 = rn.nextInt(listSize);
			int index2 = rn.nextInt(listSize);
			
			if (index1 != index2)
			{
				Station sourceStation = StationFactory.getStationInstance(westernLineStationsList.get(index1));
				Station destinationStation = StationFactory.getStationInstance(westernLineStationsList.get(index2));
				
				Person person = new Person("P" + i, sourceStation, destinationStation);
				sourceStation.enterStation(person);
				
				String lineName = LineFactory.getLineName(sourceStation, destinationStation);
				person.setTrainLine(lineName);
				person.setTrainDirectionUp(LineFactory.getDirection(lineName, sourceStation, destinationStation));
				
				persons.add(person);
				
				Thread thread = new Thread(person, "T" + person.getName());
				person.setThread(thread);
				
				thread.start();
			}
			
			i++;
		}
	}

}
