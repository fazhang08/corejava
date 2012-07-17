/**
 *
 */
package com.lexst.live.window.query;

import java.awt.*;

import javax.swing.*;

import java.awt.Font;
import javax.swing.JPanel;
import java.awt.Rectangle;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JLabel;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.SwingConstants;

public class FontSelectDialog extends JDialog {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 界面设计需要
	 * */
	private JPanel root = null;

	private JScrollPane jspFont = null;

	private JPanel fontPane = null;

	private JLabel lblName = null;

	private JLabel lblStyle = null;

	private JLabel lblSize = null;

	private JTextField txtName = null;

	private JTextField txtStyle = null;

	private JTextField txtSize = null;

	private JList listName = null;

	private JList listStyle = null;

	private JList listSize = null;

	private JPanel showPane = null;

	private JButton cmdOK = null;

	private JButton cmdReset = null;

	private JButton cmdCancel = null;

	private JScrollPane jspName = null;

	private JScrollPane jspStyle = null;

	private JScrollPane jspSize = null;

	private static JLabel lblFont = null;

	/**
	 * 字体默认变量
	 * */
	private Font defaultFont = new Font("\u5b8b\u4f53", Font.PLAIN, 12);

	/**
	 * 返回字体变量
	 * */
	private static Font returnFont = null;

	/**
	 * Boolean 变量,判断是否正常返回,是否用户选择了字体
	 * */
	private static boolean okay = false;

	/**
	 * 以防止事件重复调用或不必要的更改,定义两个boolean变量分别
	 * 为:fontNameList和fontSizeList判断
	 * 等于true则循环调用,false则不
	 * */
	private boolean nameJuge = true;

	private boolean sizeJuge = true;

	public FontSelectDialog() {
		this(null);
	}

	public FontSelectDialog(JFrame frame) {
		this(frame, true);
	}

	public FontSelectDialog(JFrame frame, boolean boo) {
		this(frame, boo, null);
	}

	public FontSelectDialog(JFrame frame, boolean boo, Font font) {
		super(frame, boo);
		initialize();
		initializeFont(font);
		this.setLocationRelativeTo(frame);
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	}

	/**
	 * This method initializes this
	 *
	 */
	private void initialize() {
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		int width = (int) (size.getWidth() * 0.42);
		int height = (int) (size.getHeight() * 0.45);
		int x = (size.width - width) / 2;
		int y = (size.height - height) / 2;

		this.setLayout(new java.awt.BorderLayout());
		this.add(getRoot(), BorderLayout.CENTER);
		this.setBounds(new Rectangle(x, y, width, height));
		this.setTitle("Font Choose Dialog");

//		this.addWindowListener(new WindowAdapter() {
//			public void windowClosing(WindowEvent e) {
//				okay = false;
//				closeWindow();
//			}
//		});
	}

	public static Font showDialog(JFrame jframe, boolean boo) {
		return showDialog(jframe, boo, null);
	}

	public static Font showDialog(JFrame jframe, boolean boo, Font font) {
		JDialog jd = new FontSelectDialog(jframe, boo, font);
		jd.setVisible(true);
		if (okay) {
			returnFont = lblFont.getFont();
		}
		jd.dispose();
		return returnFont;
	}

	private JPanel getButton() {
		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(1, 3, 6, 0));
		p1.add(getOkButton());
		p1.add(getRegitButton());
		p1.add(getCancleButton());

		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.add(new JPanel(), BorderLayout.CENTER);
		p.add(p1, BorderLayout.EAST);

		return p;
	}

	/**
	 * This method initializes jPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getRoot() {
		if (root == null) {
			JPanel top = getFontPane();
			JPanel center = getShowPane();
			JPanel bottom = getButton();

			JPanel p1 = new JPanel();
			p1.setLayout(new BorderLayout(0, 8));
			p1.add(center, BorderLayout.CENTER);
			p1.add(bottom, BorderLayout.SOUTH);

			root = new JPanel();
			root.setLayout(new BorderLayout(0, 8));
			root.setBorder(new javax.swing.border.EmptyBorder(8, 8, 8, 8));
			root.add(top, BorderLayout.CENTER);
			root.add(p1, BorderLayout.SOUTH);
		}
		return root;
	}

	/**
	 * This method initializes jScrollPane
	 *
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getFontScrollPane() {
		if (jspFont == null) {
			lblFont = new JLabel();
			lblFont.setText("Hello Lexst!");
			lblFont.setHorizontalAlignment(SwingConstants.CENTER);
			lblFont.setHorizontalTextPosition(SwingConstants.CENTER);

			jspFont = new JScrollPane();
			jspFont.setViewportView(lblFont);
		}
		return jspFont;
	}

	/**
	 * This method initializes jPanel1
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getFontPane() {
		if (fontPane == null) {
			lblName = new JLabel("Name");
			lblStyle = new JLabel("Style");
			lblSize = new JLabel("Size");

			JPanel p1 = new JPanel();
			p1.setLayout(new GridLayout(2, 1, 0, 5));
			p1.add(lblName);
			p1.add(getFontNameText());

			JPanel p11 = new JPanel();
			p11.setLayout(new BorderLayout(0, 5));
			p11.add(p1, BorderLayout.NORTH);
			p11.add(getNameScrollPane(), BorderLayout.CENTER);

			JPanel p2 = new JPanel();
			p2.setLayout(new GridLayout(2, 1, 0, 5));
			p2.add(lblStyle);
			p2.add(getFontItalicText());

			JPanel p22 = new JPanel();
			p22.setLayout(new BorderLayout(0, 5));
			p22.add(p2, BorderLayout.NORTH);
			p22.add(getStyleScrollPane(), BorderLayout.CENTER);

			JPanel p3 = new JPanel();
			p3.setLayout(new java.awt.GridLayout(2, 1, 0, 5));
			p3.add(lblSize);
			p3.add(getFontSizeText());
			JPanel p33 = new JPanel();
			p33.setLayout(new BorderLayout(0, 5));
			p33.add(p3, BorderLayout.NORTH);
			p33.add(getSizeScrollPane(), BorderLayout.CENTER);

			fontPane = new JPanel();
			fontPane.setLayout(new GridLayout(1, 3, 6, 0));
			fontPane.add(p11);
			fontPane.add(p22);
			fontPane.add(p33);
		}

		return fontPane;
	}

	/**
	 * This method initializes fontNameText
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getFontNameText() {
		if (txtName == null) {
			txtName = new JTextField();
			txtName.addKeyListener(new KeyAdapter() {
				public void keyTyped(KeyEvent e) {
					String oldText = txtName.getText();
					String newText = "";
					if ("".equals(txtName.getSelectedText())
							&& null == txtName.getSelectedText()) {
						newText = txtName.getText() + e.getKeyChar();
					} else {
						newText = oldText.substring(0, txtName
								.getSelectionStart())
								+ e.getKeyChar()
								+ oldText.substring(txtName.getSelectionEnd());
					}
//					System.out.println("fontName:" + newText);
					nameJuge = false;
					listName.setSelectedValue(getLateIndex(listName, newText),
							true);
					nameJuge = true;
				}
			});
		}
		return txtName;
	}

	/**
	 * This method initializes fontItalicText
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getFontItalicText() {
		if (txtStyle == null) {
			txtStyle = new JTextField();
			txtStyle.setEnabled(false);
		}
		return txtStyle;
	}

	/**
	 * This method initializes fontSizeText
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getFontSizeText() {
		if (txtSize == null) {
			txtSize = new JTextField();
			txtSize.setColumns(4);
			txtSize.addKeyListener(new KeyAdapter() {
				public void keyTyped(KeyEvent e) {
					String oldText = txtSize.getText();
					String newText = "";
					if ("".equals(txtSize.getSelectedText())
							&& null == txtSize.getSelectedText()) {
						newText = txtSize.getText() + e.getKeyChar();
					} else {
						newText = oldText.substring(0, txtSize
								.getSelectionStart())
								+ e.getKeyChar()
								+ oldText.substring(txtSize.getSelectionEnd());
					}
					sizeJuge = false;
					listSize.setSelectedValue(getLateIndex(listSize, newText),
							true);
					if (newText.matches("(\\d)+")) {
						lblFont.setFont(new Font(lblFont.getFont()
								.getFontName(), lblFont.getFont().getStyle(),
								Integer.parseInt(newText)));
					}
					sizeJuge = true;
				}
			});

		}
		return txtSize;
	}

	/**
	 * This method initializes fontNameList
	 *
	 * @return javax.swing.JList
	 */
	private JList getFontNameList() {
		if (listName == null) {
			listName = new JList(GraphicsEnvironment
					.getLocalGraphicsEnvironment()
					.getAvailableFontFamilyNames());
			listName
					.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
						public void valueChanged(
								javax.swing.event.ListSelectionEvent e) {
							if (nameJuge) {
								txtName.setText(listName.getSelectedValue()
										.toString());
							}
							lblFont.setFont(new Font(listName
									.getSelectedValue().toString(), lblFont
									.getFont().getStyle(), lblFont.getFont()
									.getSize()));
						}
					});
		}
		return listName;
	}

	/**
	 * This method initializes fontItalicList
	 *
	 * @return javax.swing.JList
	 */
	private JList getFontItalicList() {
		if (listStyle == null) {
			listStyle = new JList(new String[] { "Plain", "Bold", "Italic",
					"Bold Italic" });
			listStyle
					.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
						public void valueChanged(
								javax.swing.event.ListSelectionEvent e) {
							txtStyle.setText(listStyle.getSelectedValue()
									.toString());
							lblFont.setFont(new Font(lblFont.getFont()
									.getFontName(), listStyle
									.getSelectedIndex(), lblFont.getFont()
									.getSize()));
						}
					});
		}
		return listStyle;
	}

	/**
	 * This method initializes fontSizeList
	 *
	 * @return javax.swing.JList
	 */
	private JList getFontSizeList() {
		if (listSize == null) {
			int begin = 6, end = 90;
			String[] s = new String[end - begin + 1];
			for (int i = 0; i < s.length; i++) {
				s[i] = String.format("%d", begin++);
			}
			listSize = new JList(s);

//			listSize = new JList(new String[] { "3", "4", "5", "6", "7", "8",
//					"9", "10", "11", "12", "13", "14", "15", "16", "17", "18",
//					"19", "20", "22", "24", "27", "30", "34", "39", "45", "51",
//					"60" });


			listSize.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
						public void valueChanged(
								javax.swing.event.ListSelectionEvent e) {
							if (sizeJuge) {
								txtSize.setText(listSize.getSelectedValue()
										.toString());
							}
							lblFont.setFont(new Font(lblFont.getFont()
									.getFontName(), lblFont.getFont()
									.getStyle(), Integer.parseInt(listSize
									.getSelectedValue().toString())));
						}
					});
		}
		return listSize;
	}

	/**
	 * This method initializes jPanel2
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getShowPane() {
		if (showPane == null) {
			showPane = new JPanel();
			showPane.setLayout(new BorderLayout());
			showPane.setPreferredSize(new Dimension(80, 80));
			showPane.add(getFontScrollPane(), BorderLayout.CENTER);
		}

		return showPane;
	}

	/**
	 * This method initializes okButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getOkButton() {
		if (cmdOK == null) {
			cmdOK = new JButton();
			cmdOK.setText("OK");
			cmdOK.setMnemonic('O');
			cmdOK.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					okay = true;
					closeWindow();
				}
			});
		}
		return cmdOK;
	}

	/**
	 * This method initializes regitButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getRegitButton() {
		if (cmdReset == null) {
			cmdReset = new JButton();
			cmdReset.setText("Reset");
			cmdReset.setMnemonic('R');
			cmdReset.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					initializeFont(null);
				}
			});
		}
		return cmdReset;
	}

	/**
	 * This method initializes cancleButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getCancleButton() {
		if (cmdCancel == null) {
			cmdCancel = new JButton();
			cmdCancel.setText("Cancel");
			cmdCancel.setMnemonic('C');
			cmdCancel.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					okay = false;
					closeWindow();
				}
			});
		}
		return cmdCancel;
	}

	/**
	 * This method initializes jScrollPane1
	 *
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getNameScrollPane() {
		if (jspName == null) {
			jspName = new JScrollPane();
			jspName.setBounds(new Rectangle(5, 50, 150, 120));
			jspName.setViewportView(getFontNameList());
		}
		return jspName;
	}

	/**
	 * This method initializes jScrollPane2
	 *
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getStyleScrollPane() {
		if (jspStyle == null) {
			jspStyle = new JScrollPane();
			jspStyle.setBounds(new Rectangle(160, 50, 120, 120));
			jspStyle.setViewportView(getFontItalicList());
		}
		return jspStyle;
	}

	/**
	 * This method initializes jScrollPane3
	 *
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getSizeScrollPane() {
		if (jspSize == null) {
			jspSize = new JScrollPane();
			jspSize.setBounds(new Rectangle(285, 50, 120, 120));
			jspSize.setViewportView(getFontSizeList());
		}
		return jspSize;
	}

	/**
	 * 默认的字体初始化方法
	 * */
	private void initializeFont(Font font) {
		if (font != null) {
			defaultFont = font;
			lblFont.setFont(defaultFont);
		}
		lblFont.setFont(defaultFont);
		listName.setSelectedValue(defaultFont.getFontName(), true);
		listSize.setSelectedValue(
				new Integer(defaultFont.getSize()).toString(), true);
		listStyle.setSelectedIndex(defaultFont.getStyle());
	}

	/**
	 * 判断里给定的值最近的索引
	 */
	private Object getLateIndex(JList jlist, String str) {
		ListModel list = jlist.getModel();
		if (str.matches("(\\d)+")) {
			for (int i = list.getSize() - 1; i >= 0; i--) {
				if (Integer.parseInt(list.getElementAt(i).toString()) <= Integer
						.parseInt(str)) {
					return list.getElementAt(i);
				}
			}
		} else {
			for (int i = list.getSize() - 2; i >= 0; i--) {
				if (str.compareToIgnoreCase(list.getElementAt(i).toString()) == 0) {
					return list.getElementAt(i);
				} else if (str.compareToIgnoreCase(list.getElementAt(i)
						.toString()) > 0) {
					return list.getElementAt(i + 1);
				}
			}
		}
		return list.getElementAt(0);
	}

	/**
	 * 窗体关闭方法!
	 * */
	private void closeWindow() {
		this.setVisible(false);
	}

}