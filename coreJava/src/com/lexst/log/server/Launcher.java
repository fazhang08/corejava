package com.lexst.log.server;

import java.io.*;
import java.util.*;

import org.w3c.dom.*;

import com.lexst.fixp.monitor.*;
import com.lexst.remote.client.home.*;
import com.lexst.site.*;
import com.lexst.site.log.*;
import com.lexst.thread.*;
import com.lexst.util.*;
import com.lexst.util.datetime.*;
import com.lexst.util.host.*;
import com.lexst.visit.*;
import com.lexst.xml.*;

public final class Launcher extends BasicLauncher {
	private static Launcher selfHandle = new Launcher();
	
	private ArrayList<FixpPacketMonitor> array = new ArrayList<FixpPacketMonitor>();

	// root log directory
	private String path;
	// home site host
	private SiteHost home = new SiteHost();
	// local log site
	private LogSite local = new LogSite();

	/**
	 *
	 */
	private Launcher() {
		super();
		super.setExitVM(true);

		fixpStream.setPrint(false);
		fixpPacket.setPrint(false);
		packetImpl = new LogPacketInvoker(fixpPacket);
		streamImpl = new LogStreamInvoker();
	}

	/**
	 *
	 * @return
	 */
	public static Launcher getInstance() {
		return Launcher.selfHandle;
	}

	/**
	 * set home site
	 * @param host
	 */
	public void setHub(SiteHost host) {
		this.home.set(host);
	}
	
	public SiteHost getHub() {
		return this.home;
	}

	/**
	 * return a home client handle
	 * @return
	 */
	private LogHomeClient fetch(SiteHost home) {
		SocketHost host = home.getTCPHost();
		LogHomeClient client = new LogHomeClient(true, host);
		for (int i = 0; i < 3; i++) {
			try {
				client.reconnect();
				return client;
			} catch (IOException exp) {
				exp.printStackTrace();
			}
			client.close();
			this.delay(1000);
		}
		return null;
	}

	/**
	 * @param client
	 */
	private void complete(LogHomeClient client) {
		if(client == null) return;
		try {
			client.exit();
			client.close();
		} catch (IOException exp) {
			exp.printStackTrace();
		} 
	}

	/**
	 * @param siteType
	 * @param client
	 * @return
	 */
	protected boolean loadTimeout(int siteType, LogHomeClient client) {
		boolean success = false;
		try {
			int second = client.getSiteTimeout(siteType);
			this.setSiteTimeout(second);
			System.out.printf("Launcher.loadTimeout, site timeout %d\n", second);
			success = true;
		} catch (VisitException exp) {
			exp.printStackTrace();
		} catch (Throwable exp) {
			exp.printStackTrace();
		}
		return success;
	}

	/**
	 * login to home site
	 * @return
	 */
	private boolean login(LogHomeClient client) {
		boolean nullable = (client == null);
		if(nullable) client = fetch(home);
		if(client == null) return false;
		boolean success = false;
		try {
			success = client.login(this.local);
		} catch (VisitException exp) {
			exp.printStackTrace();
		}
		if (nullable) complete(client);
		return success;
	}

	/**
	 * logour from home site
	 * @return
	 */
	private boolean logout(LogHomeClient client) {
		boolean nullable = (client == null);
		if (nullable) client = fetch(home);
		if (client == null) return false;
		boolean success = false;
		try {
			success = client.logout(local.getType(), local.getHost());
		} catch (VisitException exp) {
			exp.printStackTrace();
		}
		System.out.printf("Launcher.logout, from %s %s\n", home, (success ? "success" : "failed"));
		if (nullable) complete(client);
		return success;
	}
	
	/**
	 * @return
	 */
	private boolean relogin(LogHomeClient client) {
		boolean nullable = (client == null);
		if (nullable) client = fetch(home);
		if (client == null) return false;
		boolean success = false;
		try {
			success = client.relogin(local);
		} catch (VisitException exp) {
			exp.printStackTrace();
		}
		if (nullable) complete(client);
		return success;
	}

	/**
	 * start log service
	 * @return
	 */
	private boolean startService() {
		char c = File.separatorChar;
		// local path
		path = path.replace('\\', c);
		path = path.replace('/', c);
		if (path.charAt(path.length() - 1) != c) path += c;
		// 设置日志目录
		String localIP = local.getIP();
		// 如果是自回路地址,必须绑定一个实际的本机内网地址
		if (localIP == null || IP4Style.isLoopbackIP(localIP)) {
			localIP = IP4Style.getFirstPrivateAddress();
		}
		local.setIP(localIP);
		
		for(LogNode node : local.list()) {
			String tag = node.getTag();
			int port = node.getPort();
			
			String logpath = path + tag;
			
			LogPacketWriter writer = new LogPacketWriter(logpath);
			FixpPacketMonitor monitor = new FixpPacketMonitor(1);
			monitor.setLocal(localIP, port);
			monitor.setPacketCall(writer);
			boolean success = monitor.start();
			if(success) {
				array.add(monitor);
			}
		}
		
		return true;
	}

	/**
	 * stop log listen
	 */
	private void stopService() {
		// stop monitor
		for(FixpPacketMonitor monitor : array) {
			monitor.stop();
		}
	}

	/**
	 * @param client
	 */
	private boolean loadTime(LogHomeClient client) {
		boolean nullable = (client == null);
		if (nullable) client = fetch(home);
		if (client == null) return false;

		boolean success = false;
		for (int i = 0; i < 3; i++) {
			try {
				if(client.isClosed()) client.reconnect();
				long time = client.currentTime();
				System.out.printf("Launcher.loadTime, set time:%d\n", time);
				if (time != 0L) {
					int ret = SystemTime.set(time);
					success = (ret == 0);
					break;
				}
			} catch (VisitException exp) {
				exp.printStackTrace();
			} catch (Throwable exp) {
				exp.printStackTrace();
			}
			client.close();
			this.delay(500);
		}
		if(nullable) complete(client);
		
		return success;
	}
	/*
	 * start log service
	 * @see com.lexst.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		LogHomeClient client = fetch(home);
		if (client == null) return false;
		//1. set system time
		boolean success = loadTime(client);
		// 2. get site timeout
		if (success) {
			success = loadTimeout(local.getType(), client);
		}
		//3. start fixp listen
		if (success) {
			success = loadListen(null, local.getHost());
		}
		//4. start log listen
		if (success) {
			success = startService();
			if (!success) {
				this.stopService();
				this.stopListen();
			}
		}
		//5. login to home site
		if (success) {
			success = login(client);
			System.out.printf("Launcher.init, login to %s %s\n", home, (success ? "success" : "failed"));
			if (!success) {
				this.stopService();
				this.stopListen();
			}
		}
		complete(client);
		return success;
	}

	/*
	 * release log service
	 * @see com.lexst.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// logout site
		this.logout(null);
		// stop fixp service
		this.stopListen();
		// stop log service
		this.stopService();
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		System.out.printf("Launcher.process, into...\n");
		while (!isInterrupted()) {
			long end = System.currentTimeMillis() + 1000;
			if (isLoginOperate()) {
				this.setOperate(BasicLauncher.NONE);
				this.refreshEndTime();
				this.login(null);
			} else if (isMaxSiteTimeout()) {
				refreshEndTime();
				relogin(null);
			} else if (isSiteTimeout()) {
				hello(local.getType(), home);
			}
			
			long timeout = end - System.currentTimeMillis();
			if (timeout > 0) this.delay(timeout);
		}
	}

	private boolean loadLocal(String filename) {
		XMLocal xml = new XMLocal();
		Document doc = xml.loadXMLSource(filename);
		if (doc == null) {
			return false;
		}

		NodeList list = doc.getElementsByTagName("log-path");
		Element elem = (Element) list.item(0);
		path = xml.getString(elem.getTextContent()).trim();
		SiteHost host = super.splitHome(doc);
		if(host == null) {
			return false;
		}
		home.set(host);
		host = super.splitLocal(doc);
		if(host == null) {
			return false;
		}
		local.setHost(host);
		if(!loadShutdown(doc)) {
			return false;
		}

		elem = (Element)doc.getElementsByTagName("listen-list").item(0);
		list = elem.getElementsByTagName("node");
		int size = list.getLength();
		for (int i = 0; i < size; i++) {
			Element e = (Element) list.item(i);
			String tag = xml.getValue(e, "tag");
			String port = xml.getValue(e, "port");
			
			int port1 = Integer.parseInt(port);
			LogNode node = null;
			if ("TOP".equalsIgnoreCase(tag)) {
				node = new LogNode(Site.TOP_SITE, port1);
			} else if ("HOME".equalsIgnoreCase(tag)) {
				node = new LogNode(Site.HOME_SITE, port1);
			} else if ("DATA".equalsIgnoreCase(tag)) {
				node = new LogNode(Site.DATA_SITE, port1);
			} else if ("CALL".equalsIgnoreCase(tag)) {
				node = new LogNode(Site.CALL_SITE, port1);
			} else if ("WORK".equalsIgnoreCase(tag)) {
				node = new LogNode(Site.WORK_SITE, port1);
			} else if("BUILD".equalsIgnoreCase(tag)) {
				node = new LogNode(Site.BUILD_SITE, port1);
			}
			// save log node configure
			if(node == null) {
				System.out.printf("Launcher.loadLocal, unknown log node: '%s'\n", tag);
				return false;
			}
			local.add(node);
		}
		return true;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length > 0) {
			String filename = args[0];
			boolean success = Launcher.getInstance().loadLocal(filename);
			if (success) {
				Launcher.getInstance().start();
			}
		}
	}
}