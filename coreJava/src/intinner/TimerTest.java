package intinner;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;

public class TimerTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		ActionListener listener = new TimePrinter();
		
		Timer t = new Timer(10000,listener);
		
		t.start();
		
		JOptionPane.showMessageDialog(null, "Quit programe? ");
		
		System.exit(0);

	}

}

class TimePrinter implements ActionListener
{

	@Override
	public void actionPerformed(ActionEvent e) {

		Calendar ca = Calendar.getInstance();
		
		System.out.println("At the tone, the time is " + ca.getTime());
		
		Toolkit.getDefaultToolkit().beep();
	}
	
}
