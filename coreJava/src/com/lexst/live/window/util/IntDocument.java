/**
 *
 */
package com.lexst.live.window.util;

import java.awt.Toolkit;

import javax.swing.text.*;

public class IntDocument extends PlainDocument {

	private static final long serialVersionUID = 1L;

	private JTextComponent handle;
	private int limitSize = 0;

	public IntDocument() {
		super();
	}

	public IntDocument(Content arg0) {
		super(arg0);
	}

	public IntDocument(JTextComponent obj, int limit) {
		this();
		this.handle = obj;
		this.limitSize = limit;
	}

	public void insertString(int offs, String str, AttributeSet aset)
			throws BadLocationException {
		if (str == null || str.length() < 1) {
			return;
		}

		int buffsize = 0;
		if (handle != null) {
			String line = handle.getText();
			if (line.length() > 0) {
				buffsize = line.getBytes().length;
			}
		}

		for (int i = 0; i < str.length(); i++) {
			char word = str.charAt(i);
			if (word >= '0' && word <= '9') {
				continue;
			} else {
				if (word == '-' && i == 0 && buffsize == 0) {
					continue;
				}
				Toolkit.getDefaultToolkit().beep();
				return;
			}
		}

		int size = str.getBytes().length;
		if (limitSize > 0 && buffsize + size > limitSize) {
			Toolkit.getDefaultToolkit().beep();
			return;
		}

		super.insertString(offs, str, aset);
	}

}
