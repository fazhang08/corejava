package thread;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Bank
{

	
	public Bank(int n, double initialBalance)
	{
		accounts = new double[n];
		
		for(int i = 0 ; i <accounts.length ; i++)
		{
			accounts[i] = initialBalance;
		}
		
		bankLock = new ReentrantLock();
		
		sufficientFunds = bankLock.newCondition();
	}
	
	public void transfer(int from, int to, double amount)
	{
		try
		{
			bankLock.lock();
			
			while(accounts[from] < amount)
			{
				sufficientFunds.await();
			}
			System.out.println(Thread.currentThread());

			accounts[from] -= amount;
			System.out.printf(" %10.2f from %d to %d", amount, from, to);

			accounts[to] += amount;
			System.out.printf(" Total Balance: %10.2f%n", getTotalBalance());
			
			sufficientFunds.signalAll();
			
		}catch(InterruptedException e)
		{
			e.printStackTrace();
		} finally
		{
			bankLock.unlock();
		}
	}
	
	public double getTotalBalance()
	{
		double sum = 0;
		
		for(double a : accounts)
		{
			sum += a;
		}
		
		return sum;
			
	}
	
	public int size()
	{
		return accounts.length;
	}
	
	private final double[] accounts;
	
	private Lock bankLock = new ReentrantLock();
	
	private Condition sufficientFunds;

}
