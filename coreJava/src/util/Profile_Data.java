package util;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JToolBar;
import javax.swing.JDesktopPane;
import javax.swing.JButton;
import java.awt.Color;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.ButtonGroup;
import javax.swing.JMenuBar;

public class Profile_Data {

	private JFrame frmProfiledata;
	private final ButtonGroup buttonGroup = new ButtonGroup();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Profile_Data window = new Profile_Data();
					window.frmProfiledata.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Profile_Data() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmProfiledata = new JFrame();
		frmProfiledata.setBackground(Color.WHITE);
		frmProfiledata.setTitle("Profile_Data");
		frmProfiledata.setBounds(100, 100, 450, 300);
		frmProfiledata.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel panel = new JPanel();
		frmProfiledata.getContentPane().add(panel, BorderLayout.CENTER);
		
		JMenu mnFile = new JMenu("File");
		panel.add(mnFile);
		
		JMenuItem mntmImport = new JMenuItem("Import");
		mnFile.add(mntmImport);
		
		JMenu mnConfig = new JMenu("Config");
		panel.add(mnConfig);
		
		JMenuBar menuBar = new JMenuBar();
		panel.add(menuBar);
	}

}
