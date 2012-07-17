/**
 *
 */
package com.lexst.live.window;

import java.awt.event.*;

import javax.swing.*;

public class SQLMenu {

	public JMenu file = new JMenu("File");
	public JMenu edit = new JMenu("Edit");
	public JMenu execute = new JMenu("Execute");
	public JMenu window = new JMenu("Window");
	public JMenu help = new JMenu("Help", false);
	
	public JMenuItem itemLogin = new JMenuItem("New Connection");
	public JMenuItem itemLogout = new JMenuItem("Close Connection");
	public JMenuItem itemRelogin = new JMenuItem("Reset Connection");

	public JMenuItem itemExit = new JMenuItem("Exit");

	public JMenuItem itemRefresh = new JMenuItem("Refresh Database");
	public JMenuItem itemCheckSyntax = new JMenuItem("Check SQL Syntax");
	public JMenuItem itemRun = new JMenuItem("Run");
	
	public JMenuItem itemView = new JMenuItem("View Window");
	public JMenuItem itemPreference = new JMenuItem("Preference");

	public JMenuItem itemHelp = new JMenuItem("Help");
	public JMenuItem itemAbout = new JMenuItem("About");

	public JMenu log = new JMenu("Log");
	public JRadioButtonMenuItem logDebug = new JRadioButtonMenuItem("Debug model");
	public JRadioButtonMenuItem logInfo = new JRadioButtonMenuItem("Infor model");
	public JRadioButtonMenuItem logWarn = new JRadioButtonMenuItem("Warning model");
	public JRadioButtonMenuItem logError = new JRadioButtonMenuItem("Error model");
	public JRadioButtonMenuItem logFatal = new JRadioButtonMenuItem("Fatal model");
	
	/**
	 *
	 */
	public SQLMenu() {
		super();
	}

	public void init(ActionListener listener) {
		JMenuItem[] items = { itemLogin, itemLogout, itemRelogin, itemExit,
				itemCheckSyntax, itemRefresh, itemRun };
		char[] keys = { 'N', 'C', 'R',  'X', 'C', 'R', 'R' };
		for (int i = 0; i < keys.length; i++) {
			items[i].addActionListener(listener);
			items[i].setMnemonic(keys[i]);
		}
		
		itemHelp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		itemRelogin.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
		itemRefresh.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
		itemCheckSyntax.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		itemRun.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));

		file.setMnemonic('F');
		edit.setMnemonic('E');
		execute.setMnemonic('X');
		window.setMnemonic('W');
		help.setMnemonic('H');

		file.add(itemLogin);
		file.add(itemLogout);
		file.add(itemRelogin);
		file.addSeparator();
		file.add(itemExit);

		edit.add(new JMenuItem("Cut"));
		edit.add(new JMenuItem("Copy"));
		edit.add(new JMenuItem("Paste"));
//		edit.addSeparator();
//		edit.add(new JMenuItem("SQL Option List"));

		execute.add(itemRefresh);
		execute.addSeparator();
		execute.add(itemCheckSyntax);
		execute.add(itemRun);
		
//		window.add(itemView);
//		window.addSeparator();
		window.add(log);
//		window.addSeparator();
//		window.add(itemPreference);
				
		items = new JMenuItem[] {logDebug, logInfo, logWarn, logError, logFatal};
		ButtonGroup group = new ButtonGroup();
		for (int i = 0; i < items.length; i++) {
			items[i].addActionListener(listener);
			log.add(items[i]);
			group.add(items[i]);
		}
		
		help.add(itemHelp);
//		help.addSeparator();
//		help.add(new JMenuItem("Debug"));
//		help.add(new JMenuItem("Report"));
//		help.add(new JMenuItem("Home Site"));
		help.addSeparator();
		help.add(itemAbout);

		file.setDelay(200);
		edit.setDelay(200);
		execute.setDelay(200);
		window.setDelay(200);
		help.setDelay(200);
	}

}