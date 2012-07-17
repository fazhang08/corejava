package com.lexst.live.window;

import java.awt.*;
import java.awt.event.*;
import java.net.*;

import javax.swing.*;
import javax.swing.border.*;

import com.lexst.fixp.*;
import com.lexst.live.pool.*;
import com.lexst.live.window.util.*;
import com.lexst.site.live.*;
import com.lexst.util.*;
import com.lexst.util.host.*;

public class SQLDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = 1L;

	private JLabel lblUsername = new JLabel("Username", SwingConstants.RIGHT);
	private JLabel lblPassword = new JLabel("Password", SwingConstants.RIGHT);
	private JLabel lblSecure = new JLabel("Secure", SwingConstants.RIGHT);
	private JLabel lblIP = new JLabel("IP  Address", SwingConstants.RIGHT);
	private JLabel lblPort = new JLabel("Port", SwingConstants.RIGHT);

	private JTextField txtUser = new JTextField();
	private JPasswordField txtPwd = new JPasswordField();
	private JComboBox cbxAlgo = new JComboBox();

	private JTextField txtIP = new JTextField();
	private JTextField txtPort = new JTextField();

	private JButton cmdOK = new JButton("Login");
	private JButton cmdCancel = new JButton("Cancel");

	private boolean canceled;
	private String selectUIClass;

	/**
	 * @param owner
	 * @throws HeadlessException
	 */
	public SQLDialog(Frame owner) throws HeadlessException {
		super(owner);
		this.canceled = false;
	}

	/**
	 * close window
	 */
	private void cancel() {
		this.canceled = true;
		this.dispose();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == cmdOK) {
			boolean success = login();
			if (success) {
				// start home client thread
				TopPool.getInstance().start();
				// dispatch window
				this.dispose();
			}
		} else if (e.getSource() == cmdCancel) {
			this.cancel();
		}
	}

	public boolean isCanceled() {
		return canceled;
	}

	public String getUIClass() {
		return this.selectUIClass;
	}

	private void showWarning(String msg, String tip) {
		JOptionPane.showMessageDialog(this, msg, tip, JOptionPane.WARNING_MESSAGE);
	}
	
	private String translate(String ip) {
		if(IP4Style.isLoopbackIP(ip)) {
			ip = IP4Style.getFirstPrivateAddress();
		} else if(!IP4Style.isIPv4(ip)) {
			try {
				InetAddress address = InetAddress.getByName(ip);
				if(address == null) return null;
				String v4 = address.getHostAddress();
				if(v4.equalsIgnoreCase(ip)) {
					return null;
				}
				ip = v4;
			} catch (UnknownHostException exp) {
				return null;
			}
		}
		return ip;
	}

	/**
	 * @return
	 */
	private boolean login() {
		String username = txtUser.getText().trim();
		String password = new String(txtPwd.getPassword()).trim();
		String ip = txtIP.getText().trim();
		String port = txtPort.getText().trim();
		String algo = (String)cbxAlgo.getSelectedItem();
		
		if (username.length() == 0) {
			showWarning("Please entry a user name!", "Username");
			txtUser.requestFocus();
			return false;
		} else if (password.length() == 0) {
			showWarning("Please entry a password!", "Password");
			txtPwd.requestFocus();
			return false;
		} else if (ip.length() == 0 ) {
			showWarning("Please entry a server address!", "Server address");
			txtIP.requestFocus();
			return false;
		} else if (port.length() == 0) {
			showWarning("Please entry a server port!", "Server port");
			txtPort.requestFocus();
			return false;
		}
		String ipv4 = translate(ip);
		if (ipv4 == null) {
			showWarning("Please check server address!", "Server address");
			txtIP.requestFocus();
			return false;
		}

		SiteHost remote = new SiteHost(ipv4, Integer.parseInt(port), Integer.parseInt(port));
		LiveSite local = Launcher.getInstance().getLocal();
		local.setUser(username, password);
		local.setAlgorithm("None".equalsIgnoreCase(algo) ? null : algo);

		// login
		boolean success = TopPool.getInstance().login(remote, local);
		if(!success) {
			String s = String.format("login to \'%s:%s\' failed, check address or account, please!", ip, port);
			showWarning(s, "Login error");
		}
		return success;
	}

	private void initControl() {
		JTextField[] cs = { txtUser, txtPwd, txtIP, txtPort };
		for (int i = 0; i < cs.length; i++) {
			cs[i].setPreferredSize(new Dimension(120, 25));
		}
		txtUser.setDocument(new ASCIIDocument(txtUser, 16));
		txtPwd.setDocument(new ASCIIDocument(txtPwd, 32));
		txtIP.setDocument(new ASCIIDocument(txtIP, 128));
		txtPort.setDocument(new IntDocument(txtPort, 5));

		cmdOK.addActionListener(this);
		cmdCancel.addActionListener(this);
		cmdOK.setMnemonic('L');
		cmdCancel.setMnemonic('C');
	}

	private JPanel initAddress() {
		lblIP.setDisplayedMnemonic('a');
		lblIP.setLabelFor(txtIP);

		lblPort.setDisplayedMnemonic('o');
		lblPort.setLabelFor(txtPort);
		txtPort.setColumns(6);

		JPanel p1 = new JPanel();
		p1.setLayout(new BorderLayout(5, 0));
		p1.add(lblIP, BorderLayout.WEST);
		p1.add(txtIP, BorderLayout.CENTER);

		JPanel p2 = new JPanel();
		p2.setLayout(new BorderLayout(5, 0));
		p2.add(lblPort, BorderLayout.WEST);
		p2.add(txtPort, BorderLayout.CENTER);

		JPanel p3 = new JPanel();
		p3.setLayout(new BorderLayout(5, 0));
		p3.add(p1, BorderLayout.CENTER);
		p3.add(p2, BorderLayout.EAST);
		return p3;
	}

	private JPanel initAccount() {
		lblUsername.setDisplayedMnemonic('U');
		lblPassword.setDisplayedMnemonic('P');
		lblSecure.setDisplayedMnemonic('S');
		lblUsername.setLabelFor(this.txtUser);
		lblPassword.setLabelFor(this.txtPwd);
		lblSecure.setLabelFor(this.cbxAlgo);

		String[] ciphers = new String[] { "None", Cipher.translate(Cipher.AES),
				Cipher.translate(Cipher.DES), Cipher.translate(Cipher.DES3),
				Cipher.translate(Cipher.BLOWFISH),
				Cipher.translate(Cipher.MD5), Cipher.translate(Cipher.SHA1) };
		for (int i = 0; i < ciphers.length; i++) {
			this.cbxAlgo.addItem(ciphers[i]);
		}
		
		JPanel p1 = new JPanel();
		p1.setLayout(new BorderLayout(8, 0));
		p1.add(lblSecure, BorderLayout.WEST);
		p1.add(cbxAlgo, BorderLayout.CENTER);

		JPanel p2 = new JPanel();
		p2.setLayout(new BorderLayout(8, 0));
		p2.add(txtPwd, BorderLayout.CENTER);
		p2.add(p1, BorderLayout.EAST);

		JPanel left = new JPanel();
		left.setLayout(new GridLayout(2, 1, 0, 8));
		left.add(lblUsername);
		left.add(lblPassword);

		JPanel right = new JPanel();
		right.setLayout(new GridLayout(2, 1 , 0, 8));
		right.add(txtUser);
		right.add(p2);

		JPanel p = new JPanel();
		p.setLayout(new BorderLayout(10, 0));
		p.setBorder(new TitledBorder(null, "", TitledBorder.LEFT, TitledBorder.TOP));
		p.add(left, BorderLayout.WEST);
		p.add(right, BorderLayout.CENTER);
		return p;
	}

	private JPanel initButton() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 2, 6, 2));
		panel.add(cmdOK);
		panel.add(cmdCancel);

		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.add(panel, BorderLayout.EAST);
		return p;
	}

	public void showDialog(String title) {
		// init controls
		initControl();

		JPanel address = this.initAddress();
		JPanel account = this.initAccount();
		JPanel buttons = this.initButton();

		account.setBorder(new CompoundBorder(new TitledBorder("Account"), new EmptyBorder(1, 6, 3, 6)));
		address.setBorder(new CompoundBorder(new TitledBorder("Server"), new EmptyBorder(1, 6, 3, 6)));

		JPanel p1 = new JPanel();
		p1.setLayout(new BorderLayout(0, 5));
		p1.add(address, BorderLayout.CENTER);
		p1.add(account, BorderLayout.SOUTH);

		JPanel p2 = new JPanel();
		p2.setLayout(new BorderLayout(0, 5));
		p2.setBorder(new EmptyBorder(6, 8, 8, 8));
		p2.add(p1, BorderLayout.NORTH);
		p2.add(buttons, BorderLayout.SOUTH);

		JScrollPane pane = new JScrollPane(p2);
		pane.setBorder(new EmptyBorder(0, 0, 0, 0));

		Container canvas = this.getContentPane();
		canvas.setLayout(new BorderLayout(0, 0));
		canvas.add(pane, BorderLayout.CENTER);

		// set window bound
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		int width = (int)(size.getWidth() * 0.39);
		int height = (int)(size.getHeight() * 0.36);
		int x = (size.width - width)/2;
		int y = (size.height - height)/2;
		this.setBounds(new Rectangle(x, y, width, height));
		this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		// set Nimbus UI
		String clsname = findUIClass("Nimbus");
		if(clsname != null) {
			selectUIClass = clsname;
			this.updateUI(clsname);
		}
		this.setTitle(title);
		this.setMinimumSize(new Dimension(380, 180));
		this.setAlwaysOnTop(true);
		this.setVisible(true);
	}

	private String findUIClass(String clsname) {
		UIManager.LookAndFeelInfo[] total = UIManager.getInstalledLookAndFeels();
		for (int i = 0; total != null && i < total.length; i++) {
			final String class_name = total[i].getClassName();

			int index = class_name.lastIndexOf('.');
			if (index == -1) continue;
			String suffix = class_name.substring(index + 1);
			index = suffix.indexOf("LookAndFeel");
			if (index > -1) {
				suffix = suffix.substring(0, index);
				if(suffix.equalsIgnoreCase(clsname)) {
					return class_name;
				}
			}
		}
		return null;
	}
	
	private void updateUI(String className) {
		if (className == null) return;
	    try {
	    	UIManager.setLookAndFeel(className);
	    	SwingUtilities.updateComponentTreeUI(getContentPane());
		} catch (UnsupportedLookAndFeelException exp) {
			//exp.printStackTrace();
		} catch (Exception exp) {
			//exp.printStackTrace();
		}
	}
}