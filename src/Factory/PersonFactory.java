package Factory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import Concrete.Person;
import Concrete.Station;

public class PersonFactory {

	private static HashSet<Person> persons = new HashSet<Person>();
	
	public static HashSet<Person> getPersons() {
		return persons;
	}
	
	public static void randomlyGeneratePersons(int personsCount)
	{
		int i = 1;
		Random rn = new Random();
		PrintWriter writer = null;	
		List<Station> westernLineStationsList = LineFactory.getLineInstance("WesternSlow").getStationsList();
		List<Station> centralLineStationsList = LineFactory.getLineInstance("CentralSlow").getStationsList();
		int listSize;
		
		boolean flip = true; //flag to decide whether we are adding person to werstern or central line
		try
		{
		    writer = new PrintWriter("Logs/PersonsList.txt", "UTF-8");
		    
			while(i <= personsCount)
			{
				if (flip)
					listSize = westernLineStationsList.size();
				else
					listSize = centralLineStationsList.size();
				
				int index1 = rn.nextInt(listSize);
				int index2 = rn.nextInt(listSize);
				
				if (index1 != index2)
				{
					Station sourceStation, destinationStation;
					
					if (flip)
					{
						sourceStation = westernLineStationsList.get(index1);
						destinationStation = westernLineStationsList.get(index2);
					}
					else
					{
						sourceStation = centralLineStationsList.get(index1);
						destinationStation = centralLineStationsList.get(index2);
					}
					
					Person person = new Person("P" + i, sourceStation, destinationStation);
					sourceStation.enterStation(person);
					
					writer.println(person.getName() + "\t" + sourceStation.getName() + " --> " + destinationStation.getName());
					
					String lineName = LineFactory.getLineName(sourceStation, destinationStation);
					person.setTrainLine(lineName);
					person.setTrainDirectionUp(LineFactory.getDirection(lineName, sourceStation, destinationStation));
					
					persons.add(person);
					
					Thread thread = new Thread(person, "T" + person.getName());
					person.setThread(thread);
					
					thread.start();
				}
				
				i++;
				flip = !flip;
			}
		} //end try
		catch (IOException e) 
		{
		   System.out.println(e);
		}
		finally
		{
			if (writer != null)
				writer.close();
		}
	}
	
}
