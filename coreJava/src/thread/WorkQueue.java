package thread;

import java.util.LinkedList;

public class WorkQueue {
	private final int nThreads;
	private final PoolWorker[] threads;
	private final LinkedList queue;

	public WorkQueue(int nThreads) {
		this.nThreads = nThreads;
		queue = new LinkedList();
		threads = new PoolWorker[nThreads];
		for (int i = 0; i < nThreads; i++) {
			threads[i] = new PoolWorker();
			threads[i].start();
		}
	}

	public void execute(Task t) 
	{
		synchronized (queue) 
		{
			queue.addLast(t);
			queue.notify();
		}
	}

	private class PoolWorker extends Thread 
	{
		public void run() 
		{
			Task t;
			while (true) 
			{
				synchronized (queue) 
				{
					if (queue.isEmpty()) 
					{
						try 
						{
							//System.out.println("This thread prority is : "+ this.getPriority());
							//System.out.println(this.getName());
							System.out.println(this.getName() + " - " +"Queue wait for data !");
							queue.wait();
						} catch (InterruptedException ignored) 
						{
						}
					}
					
						t = (Task) queue.removeFirst();
						
						// If we don't catch RuntimeException,
						// the pool could leak threads
						try 
						{
							t.run();
						} catch (RuntimeException e) 
						{
							// You might want to log something here
						}
					}

				
				
			}
		}
	}
}
