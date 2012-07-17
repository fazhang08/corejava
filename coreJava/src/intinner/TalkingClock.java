package intinner;

import java.awt.Toolkit;
import java.awt.event.*;
import java.util.*;

import javax.swing.JOptionPane;
import javax.swing.Timer;

public class TalkingClock {
	
	public static void main(String[] args)
	{
		TalkingClock clock = new TalkingClock(1000,true);
		
		clock.start();
		
		JOptionPane.showMessageDialog(null, "Quit programe? ");
		
		System.exit(0);
	}
	
	public TalkingClock(int interval, boolean beep)
	{
		this.interval = interval;
		
		this.beep = beep;
		
	}
	
	public void start()
	{
		ActionListener listener = new TimePrinter();
		
		Timer t = new Timer(interval,listener);
		
		t.start();
		
	}
	
	private int interval;
	
	private boolean beep;
	
	public class TimePrinter implements ActionListener
	{

		@Override
		public void actionPerformed(ActionEvent e) {
			
			Date now = new Date();
			
			System.out.println("At the tone, the time is " + now );
			
			if(beep)
				Toolkit.getDefaultToolkit().beep();
			
		}
		
	}

}
