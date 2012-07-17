package thread;


public class TestThreadTool {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		WorkQueue wq = new WorkQueue(6);
		
		
		for(int i = 0 ; i < 6 ; i++)
		{
			Task t = new Task();
			
			wq.execute(t);
		}
	
	}

}
