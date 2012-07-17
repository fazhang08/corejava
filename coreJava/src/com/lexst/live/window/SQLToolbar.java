/**
 *
 */
package com.lexst.live.window;

import java.awt.event.ActionListener;

import javax.swing.*;

import com.lexst.live.window.util.*;

public class SQLToolbar {

	public JButton cmdCheck = new JButton();
	public JButton cmdGo = new JButton();
	public JButton cmdRefresh = new JButton();
	public JButton cmdHelp = new JButton();
	public JButton cmdExit = new JButton();

	/**
	 *
	 */
	public SQLToolbar() {
		super();
	}

	public void init(ActionListener listener) {
		JButton[] buts = { cmdCheck, cmdGo, cmdRefresh, cmdHelp, cmdExit };
		String[] names = { "check_32.png", "go_32.png", "refresh_32.png", "help_32.png", "exit_32.png" };
		String[] tips = { "SQL Syntax Check", "Run", "Refresh Database", "Help", "Exit" };

		Icon[] icons = new ImageIcon[names.length];
		for (int i = 0; i < names.length; i++) {
			icons[i] = InsideIcon.getIcon(getClass(), names[i]);
		}
		for (int i = 0; i < buts.length; i++) {
			buts[i].setIcon(icons[i]);
			buts[i].addActionListener(listener);
			buts[i].setToolTipText(tips[i]);
		}
	}

}