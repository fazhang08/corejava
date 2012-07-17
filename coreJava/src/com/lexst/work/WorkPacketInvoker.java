/**
 *
 */
package com.lexst.work;

import java.io.*;

import com.lexst.algorithm.aggregate.*;
import com.lexst.db.statement.*;
import com.lexst.db.statement.dc.*;
import com.lexst.fixp.*;
import com.lexst.fixp.monitor.*;
import com.lexst.invoke.*;
import com.lexst.log.client.*;
import com.lexst.remote.*;
import com.lexst.thread.*;
import com.lexst.util.host.*;

public class WorkPacketInvoker implements PacketInvoker {

	private IPacketListener listener;

	private TaskTrigger trigger;
	
	/**
	 * @param reply
	 */
	public WorkPacketInvoker(IPacketListener reply) {
		super();
		this.listener = reply;
	}
	
	/**
	 * @param reply
	 * @param instance
	 */
	public WorkPacketInvoker(IPacketListener reply, TaskTrigger instance) {
		super();
		this.listener = reply;
		this.trigger = instance;
	}
	
	public void setTrigger(TaskTrigger instance) {
		this.trigger = instance;
	}
	
	/* (non-Javadoc)
	 * @see com.lexst.invoke.PacketInvoker#invoke(com.lexst.fixp.Packet)
	 */
	@Override
	public Packet invoke(Packet packet) {
		Command cmd = packet.getCommand();
		if (cmd.isRequest()) {
			return this.apply(packet);
		} else if (cmd.isResponse()) {
			return this.reply(packet);
		}
		return null;
	}

	private Packet apply(Packet packet) {
		Packet resp = null;
		Command cmd = packet.getCommand();
		byte major = cmd.getMajor();
		byte minor = cmd.getMinor();

		if (cmd.isShutdown()) {
			Logger.debug("WorkPacketInvoker. shutdown work site!");
			shutdown(packet);
		} else if (cmd.isComeback()) {
			Launcher.getInstance().comeback();
		} else if (major == Request.SQL && minor == Request.SQL_DC) {
			resp = this.dc(packet);
		} else if (major == Request.SQL && minor == Request.SQL_ADC) {
			resp = this.adc(packet);
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
	 * @param packet
	 * @return
	 */
	private Packet reply(Packet packet) {
		Packet reply = null;
		Command cmd = packet.getCommand();
		short code = cmd.getResponse();
		
		switch (code) {
		case Response.ISEE:
			Launcher.getInstance().refreshEndTime();
			break;
		case Response.NOTLOGIN:
			Launcher.getInstance().setOperate(BasicLauncher.LOGIN);
			break;
		}
		
		return reply;
	}

	private void shutdown(Packet request) {
		SocketHost remote = request.getRemote();
		for (int i = 0; i < 3; i++) {
			Command cmd = new Command(Response.OKAY);
			Packet resp = new Packet(cmd);
			resp.addMessage(Key.SPEAK, "goodbye!");
			// send shutdown reply
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
			return;
		}
	}

	/**
	 * "dc" command
	 * @param request
	 * @return
	 */
	private Packet dc(Packet request) {
		DCPair object = new DCPair(request);
		// execute dc
		this.trigger.dc(object);
		// waiting...
		object.waiting();

		Packet resp = (Packet) object.getResponse();
		if (resp == null) {
			Command cmd = new Command(Response.NOTFOUND);
			resp = new Packet(cmd);
		}
		return resp;
	}
	
	/**
	 * "adc" command
	 * 
	 * @param request
	 * @param resp
	 * @throws IOException
	 */
	private Packet adc(Packet request) {
		ByteArrayOutputStream buff = new ByteArrayOutputStream();
		for (int index = 0; true; index++) {
			byte[] b = request.findBinary(Key.ADC_OBJECT, index);
			if (b == null) break;
			buff.write(b, 0, b.length);
		}

		ADC adc = null;
		try {
			Apply apply = Apply.resolve(buff.toByteArray());
			adc = (ADC) apply.getParameters()[0];
		} catch (IOException exp) {
			Logger.error(exp);
		} catch (ClassNotFoundException exp) {
			Logger.error(exp);
		}
		if(adc == null) {
			// this is error
			return new Packet(new Command(Response.DC_SERVERERR));
		}

		byte[] data = request.getData();
		DCTable table = new DCTable();
		table.resolve(data, 0, data.length);
		
		// "adc" command
		Packet resp = (Packet)trigger.adc(adc, table, false);
		if(resp == null) {
			Command cmd = new Command(Response.NOTFOUND);
			resp = new Packet(cmd);
		}
		return resp;
	}
}