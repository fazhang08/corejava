/**
 *
 */
package com.lexst.live.window;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import com.lexst.live.pool.*;
import com.lexst.live.window.query.*;
import com.lexst.live.window.util.*;
import com.lexst.log.client.*;
import com.lexst.site.live.*;
import com.lexst.util.host.*;

public class SQLWindow extends JFrame implements ChangeListener, ActionListener {
	private static final long serialVersionUID = 1L;

	private final String title = "Lexst SQLive";

	private boolean systemTray;
	private TrayIcon trayIcon;
	private PopupMenu popup = new PopupMenu();
	private MenuItem mnuExit = new MenuItem("Exit");
	private MenuItem mnuHide = new MenuItem("Hide Window");
	private MenuItem[] mnuFeel;
	private Menu mnuGUI = new Menu("Window UI");

	private SQLMenu menu = new SQLMenu();
	private SQLToolbar tool = new SQLToolbar();

	private JMenuBar menuBar = new JMenuBar();
	private JToolBar toolBar = new JToolBar();

	private LeftPanel left = new LeftPanel();
	private RightPanel right = new RightPanel();

	private JLabel lblTop = new JLabel();
	private JLabel lblRank = new JLabel();
	private JLabel lblTip = new JLabel();

	/**
	 *
	 * @throws HeadlessException
	 */
	public SQLWindow() throws HeadlessException {
		super();
	}

	public class WindowCloseAdapter extends WindowAdapter {
        public void windowClosing(WindowEvent e) {
        	if (systemTray) showHide();
    	}
	}

	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub
	}
	
	private synchronized void delay(long timeout) {
		try {
			this.wait(timeout);
		} catch (java.lang.InterruptedException exp) {
			
		}
	}

	/**
	 * top icon flicker
	 */
	public void flicker() {
		String[] images = new String[] {"color_yellow.png", "color_yellow2.png"};
		ImageIcon[] icons = new ImageIcon[images.length];
		for (int i = 0; i < images.length; i++) {
			icons[i] = InsideIcon.getIcon(getClass(), images[i]);
		}

		for (int j = 0; j < 3; j++) {
			for (int i = 0; i < images.length; i++) {
				lblTop.setIcon(icons[i]);
				this.delay(800);
			}
		}

		ImageIcon icon = InsideIcon.getIcon(getClass(), "color_green.png");
		lblTop.setIcon(icon);
	}

	/**
	 * show close icon
	 */
	public void disconnect() {
		String image = "color_gray.png";
		ImageIcon icon = InsideIcon.getIcon(getClass(), image);
		lblTop.setIcon(icon);
		setTip(lblTop, new String[] { "service interrupted" });
	}

	public void setTop(String text) {
		if (text == null || text.length() == 0) {
			setTip(lblTop, new String[] { "not top site" });
		} else {
			setTip(lblTop, new String[] { text });
		}
	}
	
	private void setTip(JLabel label, String[] s) {
		if (s == null || s.length == 0) {
			label.setToolTipText(null);
			return;
		}
		StringBuilder buff = new StringBuilder();
		buff.append("<html>");
		for (int i = 0; i < s.length; i++) {
			if (i > 0) buff.append("<br>");
			buff.append(String.format("<font color=blue>%s</font>", s[i]));
		}
		buff.append("</html>");
		label.setToolTipText(buff.toString());
	}

	public void setDBA() {
		ImageIcon icon = InsideIcon.getIcon(getClass(), "dba_16.png");
		lblRank.setIconTextGap(8);
		lblRank.setToolTipText("Database Administrator");
		lblRank.setIcon(icon);
	}

	public void setUser() {
		ImageIcon icon = InsideIcon.getIcon(getClass(), "client_16.png");
		lblRank.setIconTextGap(8);
		lblRank.setToolTipText("Database Operator");
		lblRank.setIcon(icon);
	}

	public void setUnknown() {
		ImageIcon icon = InsideIcon.getIcon(getClass(), "unclient_16.png");
		lblRank.setIconTextGap(8);
		lblRank.setToolTipText("Unidentified");
		lblRank.setIcon(icon);
	}

	private boolean login(boolean init) {
		// open login window
		SQLDialog dialog = new SQLDialog(this);
		dialog.setModal(true);
		dialog.showDialog(this.title);
		dialog.dispose();
		// select close or open windows
		if(dialog.isCanceled()) {
			return false;
		}
		// when init
		if (init) {
			String classUI = dialog.getUIClass();
			if (classUI != null) {
				this.updateUI(classUI);
			}
			if (!loadTrayIcon()) {
				String err = "Could not load system tray icon. Program will exit.";
				JOptionPane.showMessageDialog(this, err, "Tray icon", JOptionPane.ERROR_MESSAGE);
				this.dispose();
				return false;
			}
		}

		SiteHost host = TopPool.getInstance().getRemote();
		setTop(String.format("top site: %s:%d", host.getIP(), host.getTCPort()));
		
		menu.itemLogin.setEnabled(false);
		// set user admin
		if (TopPool.getInstance().isDBA()) {
			setDBA();
		} else if (TopPool.getInstance().isClient()) {
			setUser();
		} else {
			this.setUnknown();
		}
		// refresh data
		left.review(TopPool.getInstance().listSchema(), TopPool.getInstance().listTable());
		
		menu.itemLogin.setEnabled(false);
		menu.itemLogout.setEnabled(true);
		menu.itemRelogin.setEnabled(true);

		return true;
	}

	private void logout() {
		LiveSite local = Launcher.getInstance().getLocal();
		TopPool.getInstance().logout(local);
		left.clear();
		setUnknown();
		disconnect();

		menu.itemLogin.setEnabled(true);
		menu.itemLogout.setEnabled(false);
		menu.itemRelogin.setEnabled(false);
	}

	private void relogin() {
		logout();
		login(false);
	}

	private void exit() {
		String tip = "Logout";
		String s = "Quit Lexst SQLive?";
		int id = JOptionPane.showConfirmDialog(this, s, tip,
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (id != JOptionPane.YES_OPTION) return;
		unloadTrayIcon();
		setVisible(false);
		// notify main thread
		Launcher.getInstance().stop();
	}
	
	/**
	 * dispose window
	 */
	@Override
	public void dispose() {
		Logger.stopService();
		super.dispose();
	}

	/**
	 * refresh database configure
	 */
	public void refresh() {
		TopPool.getInstance().resetUser();
		LiveSite local = Launcher.getInstance().getLocal();
		// load charset and check identified
		TopPool.getInstance().loadCharmap(null, local);
		TopPool.getInstance().checkIdentified(null, local);
		int ret1 = TopPool.getInstance().getPermits(null, local);
		int ret2 = TopPool.getInstance().getSchemas(null, local);
		int ret3 = TopPool.getInstance().getTables(null, local);
		if (ret1 >= 0 && ret2 >= 0 && ret3 >= 0) {
			// refresh table configure
			left.review(TopPool.getInstance().listSchema(), TopPool.getInstance().listTable());
		} else {
			SiteHost host = TopPool.getInstance().getRemote();
			String msg = String.format("Cannot connect to %s:%d", host.getIP(), host.getTCPort());
			this.showError(msg, "Check network, please!");
		}
	}

	private void showError(String msg, String tip) {
		JOptionPane.showMessageDialog(this, msg, tip, JOptionPane.ERROR_MESSAGE);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == menu.itemLogin) {
			login(false);
		} else if (e.getSource() == menu.itemLogout) {
			logout();
		} else if (e.getSource() == menu.itemRelogin) {
			relogin();
		} else if (e.getSource() == tool.cmdRefresh
				|| e.getSource() == menu.itemRefresh) {
			refresh();
		} else if (e.getSource() == tool.cmdCheck
				|| e.getSource() == menu.itemCheckSyntax) {
			right.check();
		} else if (e.getSource() == tool.cmdGo || e.getSource() == menu.itemRun) {
			right.execute();
		} else if (e.getSource() == menu.logDebug) {
			Logger.setLevel(LogLevel.debug);
		} else if (e.getSource() == menu.logInfo) {
			Logger.setLevel(LogLevel.info);
		} else if (e.getSource() == menu.logWarn) {
			Logger.setLevel(LogLevel.warning);
		} else if (e.getSource() == menu.logError) {
			Logger.setLevel(LogLevel.error);
		} else if (e.getSource() == menu.logFatal) {
			Logger.setLevel(LogLevel.fatal);
		} else if (e.getSource() == menu.itemExit
				|| e.getSource() == tool.cmdExit || e.getSource() == mnuExit) {
			this.exit();
		} else if (e.getSource() == trayIcon || e.getSource() == mnuHide) {
			this.showHide();
		} else {
			for (int i = 0; mnuFeel != null && i < mnuFeel.length; i++) {
				if (e.getSource() == mnuFeel[i]) {
					String clsname = mnuFeel[i].getName();
					this.updateUI(clsname);
				}
			}
		}
	}

	private void initMenu() {
		menu.init(this);
		menuBar.add(menu.file);
		menuBar.add(menu.edit);
		menuBar.add(menu.execute);
		menuBar.add(menu.window);
		menuBar.add(menu.help);
	}

	private void initToolBar() {
		tool.init(this);
		toolBar.add(tool.cmdRefresh);
		toolBar.add(tool.cmdCheck);
		toolBar.add(tool.cmdGo);
		toolBar.add(tool.cmdHelp);
		toolBar.add(tool.cmdExit);
		toolBar.setFloatable(false);
	}

	/**
	 * load local log configure
	 * @return
	 */
	private boolean loadLog() {
		byte[] b = new byte[256];
		InputStream in = getClass().getResourceAsStream("log.xml");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			while (true) {
				int len = in.read(b, 0, b.length);
				if (len < 1) break;
				out.write(b, 0, len);
			}
		} catch (IOException exp) {
			return false;
		}

		byte[] data = out.toByteArray();
		if (!Logger.loadXML(data)) return false;
		// set log printer handle
		Logger.setLogPrinter(right.getLogPrinter());
		// start log service
		return Logger.loadService(null);
	}
	
	public boolean showWindow() {
		loadLog();
		initMenu();
		initToolBar();
		left.init();
		right.init();
		JPanel bottom = initBottom();
		
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setContinuousLayout(true);
        split.setOneTouchExpandable(true);
        split.setResizeWeight(0.3);
        split.setBorder(new EmptyBorder(0, 0, 0, 0));

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
        panel.add(toolBar, BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);
        panel.add(bottom, BorderLayout.SOUTH);
        panel.setBorder(new javax.swing.border.EmptyBorder(2, 1, 2, 1));

		Container root = getContentPane();
		root.setLayout(new BorderLayout());
		root.add(panel, BorderLayout.CENTER);

		this.setTitle(title);
		ImageIcon icon = InsideIcon.getIcon(getClass(), "app.png");
		this.setIconImage(icon.getImage());
		this.setJMenuBar(menuBar);
		this.addWindowListener(new WindowCloseAdapter());
		// login...
		if(!login(true)) {
			this.dispose();
			return false;
		}

		this.pack();
		this.setBounds(getBound());
		super.setVisible(true);
		return true;
	}
	
	private Rectangle getBound() {
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		int xSpace = (int)(size.getWidth() * 0.06);
		int ySpace = (int)(size.getHeight()* 0.06);
		int width = size.width - (xSpace * 4);
		int height = size.height - (ySpace * 5);
		int x = (size.width - width) / 2;
		int y = (size.height - height) / 2;
		Rectangle rect = new Rectangle(x, y, width, height);
		return rect;
	}

	private JPanel initSplit(JComponent com) {
		JPanel p1 = new JPanel();
		p1.setLayout(new BorderLayout(6, 0));
		p1.setBorder( new EmptyBorder(0, 3, 0, 0) );

		p1.add(com, BorderLayout.CENTER);
		JSeparator s1 = new JSeparator(SwingConstants.VERTICAL);
		p1.add(s1, BorderLayout.EAST);

		return p1;
	}
	
	private JPanel initBottom() {
		JLabel[] subs = new JLabel[] { lblRank, lblTop };
//		String[] images = new String[] { "unclient_16.png", "home_16.png" };
		String[] images = new String[] { "unclient_16.png", "color_green.png" };

		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(1, 2, 0, 0));
		for (int i = 0; i < subs.length; i++) {
			ImageIcon icon = InsideIcon.getIcon(getClass(), images[i]);
			subs[i].setIconTextGap(10);
			subs[i].setIcon(icon);
			
			JPanel child = new JPanel();
			child.setLayout(new BorderLayout());
			child.setBorder(new EmptyBorder(0, 10, 0, 5));
			child.add(subs[i], BorderLayout.CENTER);
			p1.add(child);
		}

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(new EmptyBorder(3, 0, 0, 0));
		panel.add(initSplit(lblTip), BorderLayout.CENTER);
		panel.add(p1, BorderLayout.EAST);
		return panel;
	}

//	private JPanel initBottom2() {
//		JLabel[] subs = new JLabel[] { lblTop, lblCall, lblLocal };
//		String[] images = new String[] { "home_16.png", "call_16.png", "local_16.png" };
//
//		JPanel p1 = new JPanel();
//		p1.setLayout(new GridLayout(1, 2, 6, 0));
//		for (int i = 0; i < subs.length; i++) {
//			ImageIcon icon = InsideIcon.getIcon(getClass(), images[i]);
//			subs[i].setIconTextGap(10);
//			subs[i].setIcon(icon);
//			JPanel subpane = initSplit(subs[i]);
//			p1.add(subpane);
//		}
//
//		// set local address
//		LiveSite local = Launcher.getInstance().getLocal();
//		String s = String.format("live:%s:%d_%d", local.getIP(), local.getTCPort(), local.getUDPort());
//		setTip(lblLocal, new String[] { s });
//
//		JPanel p2 = new JPanel();
//		p2.setLayout(new BorderLayout(6, 0));
//		p2.add(initSplit(lblTip), BorderLayout.CENTER);
//		p2.add(lblRank, BorderLayout.EAST);
//
//		JPanel p3 = new JPanel();
//		p3.setLayout(new BorderLayout(2, 0));
//		p3.setBorder(new EmptyBorder(1, 5, 2, 5));
//		p3.add(p1, BorderLayout.WEST);
//		p3.add(p2, BorderLayout.CENTER);
//
//		JSeparator split = new JSeparator(SwingConstants.HORIZONTAL);
//		JPanel panel = new JPanel();
//		panel.setLayout(new BorderLayout(0, 1));
//		panel.setBorder(new EmptyBorder(3, 0, 0, 0));
//		panel.add(split, BorderLayout.NORTH);
//		panel.add(p3, BorderLayout.CENTER);
//		return panel;
//	}

	/**
	 * refresh window interface
	 * @param clsName
	 */
	private void updateUI(String clsName) {
		try {
			UIManager.setLookAndFeel(clsName);
		} catch (UnsupportedLookAndFeelException exp) {
			exp.printStackTrace();
		} catch (Exception exp) {
			exp.printStackTrace();
		}
		SwingUtilities.updateComponentTreeUI(getContentPane());
		SwingUtilities.updateComponentTreeUI(menuBar);
	}

	private void showHide() {
		boolean visible = this.isVisible();
		this.setVisible(!visible);
		mnuHide.setLabel(visible ? "Show Window" : "Hide Window");
	}

	private void loadFeel() {
		ArrayList<MenuItem> a = new ArrayList<MenuItem>();

		UIManager.LookAndFeelInfo[] clses = UIManager.getInstalledLookAndFeels();
		for (int i = 0; clses != null && i < clses.length; i++) {
			final String clsName = clses[i].getClassName();
			int index = clsName.lastIndexOf('.');
			if (index == -1) continue;
			String name = clsName.substring(index + 1);
			index = name.indexOf("LookAndFeel");
			 if (index > -1) {
				name = name.substring(0, index);
			}

			MenuItem mi = new MenuItem(name);
			mi.setName(clsName);
			a.add(mi);
		}

		if(a.isEmpty()) return;
		mnuFeel = new MenuItem[a.size()];
		a.toArray(mnuFeel);
	}

	private boolean loadTrayIcon() {
		// uncompress icon
		ImageIcon icon = InsideIcon.getIcon(getClass(), "app.png", 16, 16);
		if (icon == null) return false;
		// load system tray
		systemTray = SystemTray.isSupported();
		if (!systemTray) return false;
		SystemTray tray = SystemTray.getSystemTray();

		this.loadFeel();
		for (int i = 0; mnuFeel != null && i < mnuFeel.length; i++) {
			mnuFeel[i].addActionListener(this);
			mnuGUI.add(mnuFeel[i]);
		}

		mnuExit.addActionListener(this);
		mnuHide.addActionListener(this);
		popup.add(mnuGUI);
		popup.addSeparator();
		popup.add(mnuHide);
		popup.add(mnuExit);

		// construct a TrayIcon
		trayIcon = new TrayIcon(icon.getImage(), this.title, popup);
		// set the TrayIcon properties
		trayIcon.addActionListener(this);
		try {
			tray.add(trayIcon);
			return true;
		} catch (AWTException e) {

		}
		return false;
	}

	private void unloadTrayIcon() {
		if (trayIcon != null) {
			SystemTray.getSystemTray().remove(trayIcon);
		}
	}

}