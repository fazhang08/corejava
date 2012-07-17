/**
 *
 */
package com.lexst.call;


import com.lexst.call.pool.*;
import com.lexst.data.Launcher;
import com.lexst.fixp.*;
import com.lexst.fixp.monitor.*;
import com.lexst.invoke.*;
import com.lexst.thread.*;
import com.lexst.util.host.*;


public class CallPacketInvoker implements PacketInvoker {

	private IPacketListener listener;

	private CallLauncher callInstance;

	/**
	 *
	 */
	public CallPacketInvoker(CallLauncher instance, IPacketListener reply) {
		this.callInstance = instance;
		this.listener = reply;
	}

	/* (non-Javadoc)
	 * @see com.lexst.invoke.PacketCall#invoke(com.lexst.fixp.Packet)
	 */
	@Override
	public Packet invoke(Packet packet) {
		Packet reply = null;
		Command cmd = packet.getCommand();
		if (cmd.isRequest()) {
			reply = apply(packet);
		} else if (cmd.isResponse()) {
			reply = reply(packet);
		}
		return reply;
	}

	/**
	 * apply a job
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
		} else if(cmd.isComeback()) {
			callInstance.comeback();	
		} else if(major == Request.NOTIFY && minor == Request.REFRESH_DATASITE) {
			DataPool.getInstance().refresh();
		} else if(major == Request.NOTIFY && minor == Request.REFRESH_WORKSITE) {
			WorkPool.getInstance().refresh();
		} else if (major == Request.NOTIFY && minor == Request.SCANHUB) {
			// start thread, scan home site
			HubCrawler crawler = new HubCrawler();
			crawler.detect(packet);
		} else if(major == Request.NOTIFY && minor == Request.TRANSFER_HUB) {
			this.transfer(packet);
		}

		return resp;
	}

	/**
	 * reply a job
	 * @param reply
	 * @return
	 */
	private Packet reply(Packet reply) {
		Packet resp = null;
		Command cmd = reply.getCommand();
		short code = cmd.getResponse();

		switch (code) {
		case Response.ISEE:
			callInstance.refreshEndTime();
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
		callInstance.stop();
	}
	
	private void transfer(Packet packet) {
		String server_address = packet.findChar(Key.LOCAL_ADDRESS);
		if (server_address == null)	return;

		try {
			SiteHost host = new SiteHost(server_address);
			Launcher.getInstance().setHub(host);
			Launcher.getInstance().setOperate(BasicLauncher.LOGIN);
		} catch (Throwable exp) {
			return;
		}
	}
}