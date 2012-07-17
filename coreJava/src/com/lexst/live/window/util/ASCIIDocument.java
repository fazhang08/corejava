/**
 *
 */
package com.lexst.live.window.util;

import java.awt.Toolkit;

import javax.swing.text.*;

public class ASCIIDocument extends PlainDocument {

	private static final long serialVersionUID = 1L;

	private JTextComponent handle;
	private int limitSize = 0;

	public ASCIIDocument() {
		super();
	}

	public ASCIIDocument(Content arg0) {
		super(arg0);
	}

	public ASCIIDocument(JTextComponent obj, int limit) {
		this();
		this.handle = obj;
		this.limitSize = limit;
	}

	public void insertString(int offs, String str, AttributeSet aset)
			throws BadLocationException {
		if (str == null || str.length() < 1) return;

		for (int i = 0; i < str.length(); i++) {
			char word = str.charAt(i);
			if (word > 32 && word < 127) {
				continue;
			} else {
				Toolkit.getDefaultToolkit().beep();
				return;
			}
		}

		int size = str.getBytes().length;
		int buffsize = 0;
		if (handle != null) {
			String line = handle.getText();
			if (line.length() > 0) {
				buffsize = line.getBytes().length;
			}
		}

		// 如果设置了限制长度,那么检查总长度
		if (limitSize > 0 && buffsize + size > limitSize) {
			Toolkit.getDefaultToolkit().beep();
			return;
		}

		super.insertString(offs, str, aset);
	}

}
