package Factory;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import Concrete.AsynchronousLogger;
import Concrete.Person;
import Concrete.Station;

public class PersonFactory {

	private final static List<Person> personsList= new ArrayList<Person>();
	private final static String PERSONSLISTFILE = "Logs/PersonsList.txt";
	private final static String ENCODINGFILE = "UTF-8";
	private final static PrintWriter writer = createPersonsListFile(PERSONSLISTFILE, ENCODINGFILE);
	private final static AsynchronousLogger asyncLogger = CustomLoggerFactory.getAsynchronousLoggerInstance();
	
	private PersonFactory()
	{
		//do nothing
	}
	
	public static List<Person> getPersonsList() {
		return personsList;
	}
	
	public static void randomlyGeneratePersons(int personsCount, String lineName)
	{
		int i = 1;
		int currentPersonsCount = personsList.size();
		Random rn = new Random();
		List<Station> stationsList = LineFactory.getLineInstance(lineName).getStationsList();
		int listSize = stationsList.size();
		
		while(i <= personsCount)
		{
			int index1 = rn.nextInt(listSize);
			int index2 = rn.nextInt(listSize);
			
			if (index1 != index2)
			{					
				Station sourceStation = stationsList.get(index1);
				Station destinationStation = stationsList.get(index2);
				
				Person person = new Person("P" + (i + currentPersonsCount), sourceStation, destinationStation);
				
				writer.println(person.getName() + "\t" + sourceStation.getName() + " --> " + destinationStation.getName());
				
				personsList.add(person);
				
				person.startPersonThread();
				
				i++;
			}
		}
	}
	
	private static PrintWriter createPersonsListFile(String file, String encoding)
	{
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(file, encoding);
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			asyncLogger.log(Arrays.toString(e.getStackTrace()));
		}
		
		return writer;
	}
	
	public static void closePersonsListFile()
	{
		if (writer != null)
			writer.close();
	}
	
}
