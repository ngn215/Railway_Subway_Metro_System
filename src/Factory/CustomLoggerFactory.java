package Factory;

import Concrete.AsynchronousLogger;

public class CustomLoggerFactory {

private final static AsynchronousLogger asynchronousLogger = new AsynchronousLogger();
	
	private CustomLoggerFactory()
	{
		//do nothing
	}
	
	public static AsynchronousLogger getAsynchronousLoggerInstance()
	{
		return asynchronousLogger;
	}
}
