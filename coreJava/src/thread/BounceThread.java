package thread;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;


public class BounceThread {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		EventQueue.invokeLater(new Runnable()  
		{

			@Override
			public void run() 
			{
				JFrame frame = new BounceFrame();
				
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				
				frame.setVisible(true);
				
			}
			
		});

	}

}

class BallRunnable implements Runnable
{
	public BallRunnable(Ball aBall, Component aComponent)
	{
		ball = aBall;
		
		component = aComponent;
	}
	
	@Override
	public void run()
	{
		try {

			for (int i = 1; i <= STEPS; i++) 
			{
				ball.move(component.getBounds());

				component.repaint();

				Thread.sleep(DELAY);

			}
		} catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}
	
	private Ball ball ;
	
	private Component component;
	
	public static final int STEPS = 1000;
	
	public static final int DELAY = 5;
}

class BounceFrame extends JFrame
{
	public BounceFrame()
	{
		setSize(DEFAULT_WIDTH,DEFAULT_HIGHT);
		
		setTitle("BounceThread");
		
		
		comp = new BallComponent();
		
		add(comp,BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel();
		
		addButton(buttonPanel,"Start",new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e) 
			{
				addBall();
				
			}
			
		});
		
		addButton(buttonPanel,"Close",new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e) 
			{
				System.exit(0);
				
			}
			
		});
		
		add(buttonPanel,BorderLayout.SOUTH);
	}
	
	
	
	private void addButton(Container c, String title, ActionListener listener) 
	{
		JButton button = new JButton(title);
		
		c.add(button);
		
		button.add(c);
		
	}
	
	public void addBall()
	{
		Ball b = new Ball();
		
		comp.add(b);
		
		Runnable r = new BallRunnable(b,comp);
		
		Thread t = new Thread(r);
		
		t.start();
	}



	private BallComponent comp;
	
	public static final int DEFAULT_WIDTH = 450;
	
	public static final int DEFAULT_HIGHT = 350;
	
	public static final int STEPS = 1000;
	
	public static final int DELAY = 3;
}