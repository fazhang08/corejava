/**
 *
 */
package com.lexst.live.window.query;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.rtf.*;

public class SQLTextPane extends JTextPane {
	private static final long serialVersionUID = 1L;
	
	class SQLNode {
		String word;
		int begin;
		int size;
		
		public SQLNode(String s, int index, int size) {
			this.word = s;
			this.begin = index;
			this.size = size;
		}
	}

	protected StyleContext context;

	protected DefaultStyledDocument document;

	private MutableAttributeSet hot, normal;

	private MutableAttributeSet inputAttributes = new RTFEditorKit().getInputAttributes();

	private ArrayList<String> words = new ArrayList<String>();
	
	private ArrayList<Character> excludes = new ArrayList<Character>();

	/**
	 * SQL text panel
	 */
	public SQLTextPane() {
		super();
		// load sql keyword
		this.loadSQL();
		context = new StyleContext();
		document = new DefaultStyledDocument(context);
		this.setDocument(document);
		
//		String s = "_-";
		String s = "_-\\/";
		for (int i = 0; i < s.length(); i++) {
			excludes.add(s.charAt(i));
		}

		this.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent ke) {
				syntaxParse();
			}
		});

		Color color = new Color(0, 128, 64);
		// set keyword color
		hot = new SimpleAttributeSet();
		StyleConstants.setForeground(hot, color);
		// set normal text color
		normal = new SimpleAttributeSet();
		StyleConstants.setForeground(normal, Color.black);
	}

	private void loadSQL() {
		InputStream in = getClass().getResourceAsStream("sql.ini");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		try {
			while(true) {
				String s = reader.readLine();
				if(s == null) break;
				s = s.trim();
				if (s.length() > 0) {
					words.add(s.toLowerCase());
				}
			}
		} catch (IOException exp) {
			exp.printStackTrace();
		}
	}

//	public void syntaxParse2() {
//		try {
//			String s = null;
//			Element root = document.getDefaultRootElement();
//			// 取光标当前行
//			int cursorPos = this.getCaretPosition();
//			// 当前光标的位置
//			int line = root.getElementIndex(cursorPos);
//			// 取当前行
//			Element para = root.getElement(line);
//			int start = para.getStartOffset();
//			int end = para.getEndOffset() - 1;
//			// 删除\r字符
//			s = document.getText(start, end - start).toUpperCase();
//
//			int i = 0;
//			int xStart = 0;
//
//			// 分析关键字
//			document.setCharacterAttributes(start, s.length(), normal, false);
//			SQLTokenizer st = new SQLTokenizer(s);
//			while (st.hasMoreTokens()) {
//				s = st.nextToken();
//				if (s == null) return;
//
////				if (selectWords != null) {
////					for (i = 0; i < selectWords.length; i++) {
////						if (s.equals(selectWords[i])) break;
////					}
////					if (i >= selectWords.length) continue;
////				}
//				
//				if(!words.contains(s.toLowerCase())) {
//					continue;
//				}
//				
//				xStart = st.getCurrPosition();
//				// 设置关键字显示属性
//				document.setCharacterAttributes(start + xStart, s.length(), hot, false);
//			}
//			inputAttributes.addAttributes(normal);
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//	}
	
	private boolean isAlnum(char w) {
		if ('a' <= w && w <= 'z') return true;
		else if ('A' <= w && w <= 'Z') return true;
		else if ('0' <= w && w <= '9') return true;
		return false;
	}
		
	private Iterator<SQLNode> splitText(String text) {
		ArrayList<SQLNode> a = new ArrayList<SQLNode>();
		int begin = -1;
		for (int index = 0; index < text.length(); index++) {
			char w = text.charAt(index);
			if (w < 32 || w > 126) {
				begin = -1;
			} else if (isAlnum(w) || excludes.contains(w)) {
				if (begin == -1) begin = index;
			} else {
				if (begin == -1) continue;
				String s = text.substring(begin, index);
				if (words.contains(s.toLowerCase())) {
					a.add(new SQLNode(s, begin, index - begin));
				}
				begin = -1;
			}
		}
		if(begin > -1) {
			String s = text.substring(begin);
			if (words.contains(s.toLowerCase())) {
				a.add(new SQLNode(s, begin, text.length() - begin));
			}
		}
		return a.iterator();
	}

	public void syntaxParse() {
		String s = super.getText();
		if(s.isEmpty()) return;
		
		s = s.replaceAll("\r\n", "\n");
		
		document.setCharacterAttributes(0, s.length(), normal, false);
		Iterator<SQLNode> elems = splitText(s);
		while(elems.hasNext()) {
			SQLNode node = elems.next();	
			document.setCharacterAttributes(node.begin, node.size, hot, false);
		}
		inputAttributes.addAttributes(normal);
	}
}