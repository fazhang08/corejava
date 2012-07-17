/**
 * 
 */
package com.lexst.live.window.query;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import com.lexst.log.client.*;

public class LogPanel extends JPanel implements ActionListener, LogPrinter {

	private static final long serialVersionUID = 1L;
	
	private JTextArea txtTip = new JTextArea();
	
	private int maxlog;
	private java.util.List<String> array = new ArrayList<String>();

	/**
	 * 
	 */
	public LogPanel() {
		super();
		setMaxLog(2000);
	}
	
	/**
	 * @param value
	 */
	public void setMaxLog(int value) {
		this.maxlog = value;
	}

	public int getMaxLog() {
		return this.maxlog;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg) {
		// TODO Auto-generated method stub
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.log.client.LogPrinter#print(java.lang.String)
	 */
	@Override
	public void print(String text) {
		if (array.size() >= maxlog) {
			array.remove(0);
			StringBuilder buffer = new StringBuilder();
			for (String s : array) {
				buffer.append(s);
			}
			txtTip.setText(buffer.toString());
		}
		
		array.add(text);
		txtTip.append(text);
	}

	public void init() {
		Font f1 = txtTip.getFont();
//		Font font = new Font(f1.getName(), f1.getStyle(), f1.getSize() + 4);
//		font = new Font(Font.DIALOG_INPUT, f1.getStyle(), f1.getSize() + 2);
		Font font = new Font(Font.DIALOG_INPUT, f1.getStyle(), f1.getSize() );
		txtTip.setFont(font);
		
		txtTip.setEditable(false);
		txtTip.setRows(3);
		txtTip.setToolTipText("Log console");
		txtTip.setForeground(Color.blue);
		txtTip.setBorder(new EmptyBorder(2, 2, 2, 2));
		
		setLayout(new BorderLayout());
//		setBorder(new EmptyBorder(2, 2, 2, 2));
		JScrollPane bottom = new JScrollPane(txtTip);
		add(bottom, BorderLayout.CENTER);
	}
}