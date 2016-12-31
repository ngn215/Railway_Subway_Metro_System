package Factory;

import java.util.concurrent.locks.ReentrantReadWriteLock;


public class LockFactory {
	
	private volatile static int lockCount = 0;

	public static ReentrantReadWriteLock getReentrantReadWriteLockInstance(boolean fair)
	{
		incrementLockCount();
		return new ReentrantReadWriteLock(fair);
	}
	
	private static void incrementLockCount()
	{
		lockCount++;
	}
	
	public int getLockCount()
	{
		return lockCount;
	}
}
