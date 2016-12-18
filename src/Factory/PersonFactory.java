package Factory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import Concrete.Person;
import Concrete.Station;

public class PersonFactory {

	private static List<Person> personsList= new ArrayList<Person>();
	
	public static List<Person> getPersons() {
		return personsList;
	}
	
	public static void randomlyGeneratePersons(int personsCount, String lineName)
	{
		int i = 1;
		Random rn = new Random();
		PrintWriter writer = null;	
		List<Station> stationsList = LineFactory.getLineInstance(lineName).getStationsList();
		int listSize = stationsList.size();
		
		try
		{
		    writer = new PrintWriter("Logs/PersonsList.txt", "UTF-8");
		    
			while(i <= personsCount)
			{
				int index1 = rn.nextInt(listSize);
				int index2 = rn.nextInt(listSize);
				
				if (index1 != index2)
				{					
					Station sourceStation = stationsList.get(index1);
					Station destinationStation = stationsList.get(index2);
					
					Person person = new Person("P" + i, sourceStation, destinationStation);
					sourceStation.enterStation(person);
					
					writer.println(person.getName() + "\t" + sourceStation.getName() + " --> " + destinationStation.getName());
					
					person.setTrainLine(lineName);
					person.setTrainDirectionUp(LineFactory.getDirection(lineName, sourceStation, destinationStation));
					
					personsList.add(person);
					
					Thread thread = new Thread(person, "T" + person.getName());
					person.setThread(thread);
					
					thread.start();
				}
				
				i++;
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
