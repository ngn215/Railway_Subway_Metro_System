package LockerClasses;

import java.util.concurrent.locks.ReentrantReadWriteLock;


public class ReentrantLockerUnlocker {
	
	/*public static ReentrantReadWriteLock getReentrantReadWriteLockInstance(boolean fair)
	{
		return (new ReentrantReadWriteLock(fair));
	}*/
	
	public void readLock(ReentrantReadWriteLock readWriteLock)
	{
		readWriteLock.readLock().lock();
	}
	
	public void readUnlock(ReentrantReadWriteLock readWriteLock)
	{
		readWriteLock.readLock().unlock();
	}
	
	public void writeLock(ReentrantReadWriteLock readWriteLock)
	{	
		//if (Thread.currentThread().getName().substring(0, 2).equals("TW"))
			//System.out.println("waiting for write  lock " + Thread.currentThread().getName() + " " + System.currentTimeMillis());
		
		readWriteLock.writeLock().lock();
		
		//if (Thread.currentThread().getName().substring(0, 2).equals("TW"))
			//System.out.println("locking write " + Thread.currentThread().getName() + " " + System.currentTimeMillis());
	}
	
	public void writeUnlock(ReentrantReadWriteLock readWriteLock)
	{
		//if (Thread.currentThread().getName().substring(0, 2).equals("TW"))
			//System.out.println("waiting to unlock write " + Thread.currentThread().getName() + " " + System.currentTimeMillis());
		
		readWriteLock.writeLock().unlock();
		
		//if (Thread.currentThread().getName().substring(0, 2).equals("TW"))
			//System.out.println("unlocking write " + Thread.currentThread().getName() + " " + System.currentTimeMillis());
	}
	
}
