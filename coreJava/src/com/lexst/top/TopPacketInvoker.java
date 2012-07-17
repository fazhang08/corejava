/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2010 lexst.com. All rights reserved
 * 
 * this file is part of lexst
 * 
 * @author yj.liang lexst@126.com
 * 
 * @version 1.0 5/12/2010
 * @see com.lexst.top
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.top;

import com.lexst.fixp.*;
import com.lexst.fixp.monitor.*;
import com.lexst.invoke.*;
import com.lexst.site.*;
import com.lexst.top.pool.*;
import com.lexst.util.host.*;

public class TopPacketInvoker implements PacketInvoker {

	private IPacketListener listener;

	/**
	 *
	 */
	public TopPacketInvoker(IPacketListener reply) {
		super();
		this.listener = reply;
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

	/**
	 * (non-Javadoc)
	 * @see com.lexst.invoke.PacketInvoker#invoke(com.lexst.fixp.Packet)
	 */
	public Packet invoke(Packet packet) {
		Packet resp = null;
		Command cmd = packet.getCommand();
		if (cmd.isRequest()) {
			resp = this.apply(packet);
		} else if (cmd.isResponse()) {
			resp = this.reply(packet);
		}
		return resp;
	}

	/**
	 * @param packet
	 * @return
	 */
	private Packet reply(Packet packet) {
		Packet resp = null;
		Command cmd = packet.getCommand();
		short code = cmd.getResponse();
		
		if(code == Response.ISEE) {

		}
		return resp;
	}

	private Packet apply(Packet packet) {
		Packet resp = null;
		Command cmd = packet.getCommand();
		byte major = cmd.getMajor();
		byte minor = cmd.getMinor();

		if (cmd.isShutdown()) {
			this.shutdown(packet);
		} else if (cmd.isActive()) { // hello command
			resp = refresh(packet);
		} else if (cmd.isComeback()) {
			Launcher.getInstance().comeback();
		} else if (major == Request.NOTIFY && minor == Request.HELOHUB) {
			cmd = new Command(Response.OKAY);
			resp = new Packet(packet.getRemote(), cmd);
			resp.addMessage(Key.SPEAK, "yes! i am top!");
		}

		return resp;
	}

	private void shutdown(Packet request) {
		SocketHost remote = request.getRemote();
		// send shutdown reply
		for (int i = 0; i < 3; i++) {
			Command cmd = new Command(Response.OKAY);
			Packet resp = new Packet(cmd);
			resp.addMessage(Key.SPEAK, "goodbye!");
			listener.send(remote, resp);
		}
		// stop launcher
		Launcher.getInstance().stop();
	}

	private Packet refresh(Packet request) {	
		Message msg = request.findMessage(Key.SITE_TYPE);
		int type = (msg == null ? 0 : msg.intValue());
		msg = request.findMessage(Key.IP);
		String ip = (msg == null ? "127.0.0.1" : msg.stringValue());
		msg = request.findMessage(Key.TCPORT);
		int tcport = (msg == null ? 0 : msg.intValue());
		msg = request.findMessage(Key.UDPORT);
		int udport = (msg == null ? 0 : msg.intValue());

		short code = Response.NOTLOGIN;
		SiteHost host = new SiteHost(ip, tcport, udport);
		boolean executed = true;
		switch (type) {
		case Site.HOME_SITE:
			code = HomePool.getInstance().refresh(host);
			break;
		case Site.LIVE_SITE:
			code = LivePool.getInstance().refresh(host);
			break;
		default:
			executed = false;
			break;
		}

		Packet resp = null;
		if (executed) {
			resp = buildResp(request.getRemote(), code);
			resp.addMessage(Key.SPEAK, "hello");
		}
		return resp;
	}
}