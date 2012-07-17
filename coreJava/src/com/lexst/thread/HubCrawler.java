/**
 * 
 */
package com.lexst.thread;

import java.io.*;
import com.lexst.fixp.*;
import com.lexst.fixp.client.*;
import com.lexst.log.client.*;
import com.lexst.util.host.*;

public class HubCrawler extends VirtualThread {

	private Packet packet;
	
	/**
	 * 
	 */
	public HubCrawler() {
		super();
	}
	
	/**
	 * @param packet
	 */
	public void detect(Packet packet) {
		this.packet = packet;
		this.start();
	}
	
	/**
	 * scan hub site(top, home), in job site
	 */
	private void scan() {
		Message msg = packet.findMessage(Key.SERVER_ADDRESS);
		if (msg == null) return;
		String server_address = msg.stringValue();

		msg = packet.findMessage(Key.LOCAL_ADDRESS);
		if (msg == null) return;
		String reply_address = msg.stringValue();
		
		msg = packet.findMessage(Key.PACKET_IDENTIFY);
		if (msg == null) return;
		int packetId = msg.intValue();
		
		int timeout = 20;
		msg = packet.findMessage(Key.TIMEOUT);
		if (msg != null) timeout = msg.intValue();
		
		SiteHost host = new SiteHost(server_address);
		
		Command cmd = new Command(Request.NOTIFY, Request.HELOHUB);
		Packet request = new Packet(host.getUDPHost(), cmd);
		request.addMessage(Key.SPEAK, "hello boss!");

		// receive packet, from hub site
		Packet resp = null;
		FixpPacketClient client = new FixpPacketClient();
		client.setReceiveTimeout(timeout);
		short code = Response.NOTFOUND;
		
		boolean success = false;
		try {
			if (!client.bind()) return;
			// send packet
			for(int i = 0; i < 3; i++) {
				client.send(request);
			}
			// receive packet
			resp = client.receive();
			if (resp != null && resp.getCommand().getResponse() == Response.OKAY) {
				code = Response.OKAY;
			}
			success = true;
		} catch (IOException exp) {
			Logger.error(exp);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		}

		// send packet to hub site(friend site)
		SocketHost remote = new SocketHost(reply_address);
		resp = new Packet(remote, new Command(code));
		resp.addMessage(Key.PACKET_IDENTIFY, packetId);
		resp.addMessage(Key.SPEAK, "hello!");
		try {
			if (!success) {
				client.close();
				if (!client.bind()) return;
			}
			for (int i = 0; i < 3; i++) {
				client.send(resp);
			}
		} catch (IOException exp) {
			Logger.error(exp);
		}
		// close socket
		client.close();
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		this.scan();
		this.setInterrupted(true);
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// TODO Auto-generated method stub

	}

}