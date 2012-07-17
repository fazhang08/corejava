/**
 *
 */
package com.lexst.home;

import com.lexst.fixp.*;
import com.lexst.fixp.monitor.*;
import com.lexst.home.pool.*;
import com.lexst.invoke.*;
import com.lexst.log.client.*;
import com.lexst.site.*;
import com.lexst.thread.*;
import com.lexst.util.host.*;

public class HomePacketInvoker implements PacketInvoker {

	private IPacketListener listener;

	/**
	 * @param listener
	 */
	public HomePacketInvoker(IPacketListener listener) {
		super();
		this.listener = listener;
	}
	
	/**
	 * build response packet
	 * @param code
	 * @return
	 */
	private Packet buildResp(SocketHost remote, short code) {
		Command cmd = new Command(code);
		return new Packet(remote, cmd);
	}

	/* (non-Javadoc)
	 * @see com.lexst.invoke.PacketCall#invoke(com.lexst.fixp.Packet)
	 */
	@Override
	public Packet invoke(Packet packet) {
		Packet resp = null;
		Command cmd = packet.getCommand();
		if (cmd.isRequest()) {
			resp = apply(packet);
		} else if (cmd.isResponse()) {
			resp = reply(packet);
		}
		return resp;
	}

	/**
	 * invoke app
	 * @param packet
	 * @return
	 */
	private Packet apply(Packet packet) {
		Packet resp = null;
		Command cmd = packet.getCommand();
		byte major = cmd.getMajor();
		byte minor = cmd.getMinor();
		
		if (cmd.isShutdown()) {
			shutdown(packet);
		} else if (cmd.isActive()) {
			resp = refresh(packet);
		} else if (cmd.isComeback()) {
			Launcher.getInstance().comeback();
		} else if (major == Request.NOTIFY && minor == Request.HELOHUB) {
			cmd = new Command(Response.OKAY);
			resp = new Packet(packet.getRemote(), cmd);
			resp.addMessage(Key.SPEAK, "yes! i am home!");
		} else if(major == Request.NOTIFY && minor == Request.SCANHUB) {
			// start thread, scan top site
			HubCrawler crawler = new HubCrawler();
			crawler.detect(packet);
		} else if(major == Request.NOTIFY && minor == Request.TRANSFER_HUB) {
			this.transfer(packet);
		}

		return resp;
	}
	
	/**
	 * invoke app
	 * @param packet
	 * @return
	 */
	private Packet reply(Packet packet) {
		Packet resp = null;
		Command cmd = packet.getCommand();
		short code = cmd.getResponse();

		switch (code) {
		case Response.HOME_ISEE:
			Launcher.getInstance().refreshEndTime();
			break;
		case Response.NOTLOGIN:
			Launcher.getInstance().setOperate(BasicLauncher.LOGIN);
			break;
		}
		
		return resp;
	}

	private void shutdown(Packet request) {
		SocketHost remote = request.getRemote();
		for (int i = 0; i < 3; i++) {
			Command cmd = new Command(Response.OKAY);
			Packet resp = new Packet(cmd);
			resp.addMessage(Key.SPEAK, "goodbye!");
			listener.send(remote, resp);
		}
		// stop launcher
		Launcher.getInstance().stop();
	}

	private void transfer(Packet packet) {
		String server_address = packet.findChar(Key.LOCAL_ADDRESS);
		if (server_address == null)	return;

		try {
			SiteHost host = new SiteHost(server_address);
			Launcher.getInstance().setHub(host);
			Launcher.getInstance().setOperate(BasicLauncher.LOGIN);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}
	}
	
	private Packet refresh(Packet request) {
		Message msg = request.findMessage(Key.SITE_TYPE);
		int type = (msg == null ? 0 : msg.intValue());
		msg = request.findMessage(Key.IP);
		String ip = (msg == null ? "127.0.1.1" : msg.stringValue());
		msg = request.findMessage(Key.TCPORT);
		int tcport = (msg == null ? 0 : msg.intValue());
		msg = request.findMessage(Key.UDPORT);
		int udport = (msg == null ? 0 : msg.intValue());

		SiteHost host = new SiteHost(ip, tcport, udport);
		short code = Response.NOTLOGIN;
		switch (type) {
		case Site.LOG_SITE:
			code = LogPool.getInstance().refresh(host);
			break;
		case Site.WORK_SITE:
			code = WorkPool.getInstance().refresh(host);
			break;
		case Site.DATA_SITE:
			code = DataPool.getInstance().refresh(host);
			break;
		case Site.CALL_SITE:
			code = CallPool.getInstance().refresh(host);
			break;
		case Site.BUILD_SITE:
			code = BuildPool.getInstance().refresh(host);
			break;
		}

		Packet resp = null;
		if (code == Response.ISEE) {
			resp = buildResp(request.getRemote(), code);
			resp.addMessage(new Message(Key.SPEAK, "i accepted"));
		}
		return resp;
	}
	
}