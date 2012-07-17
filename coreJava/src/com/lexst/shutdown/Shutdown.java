/**
 *
 */
package com.lexst.shutdown;

import java.io.*;

import org.w3c.dom.*;

import com.lexst.fixp.*;
import com.lexst.fixp.client.*;
import com.lexst.util.*;
import com.lexst.util.host.*;
import com.lexst.xml.*;

/**
 *
 * send shutdown command
 *
 */
public class Shutdown {

	/**
	 *
	 */
	public Shutdown() {
		super();
	}

	public boolean send(String filename) {
		XMLocal xml = new XMLocal();
		Document doc = xml.loadXMLSource(filename);
		if (doc == null) return false;

		Element elem = (Element) doc.getElementsByTagName("local-site").item(0);
		String ip = xml.getXMLValue(elem.getElementsByTagName("ip"));
		if (ip == null || IP4Style.isLoopbackIP(ip)) {
			ip = IP4Style.getFirstPrivateAddress();
		}
		String udport = xml.getXMLValue(elem.getElementsByTagName("udp-port"));
		SocketHost host = new SocketHost(SocketHost.UDP, ip, Integer.parseInt(udport));

		Command cmd = new Command(Request.NOTIFY, Request.SHUTDOWN);
		Packet request = new Packet(cmd);
		request.setRemote(host);

		boolean success = false;
		FixpPacketClient client = new FixpPacketClient();
		// receive timeout
		client.setReceiveTimeout(6);
		// send and receive packet
		for (int i = 0; i < 3; i++) {
			try {
				Packet resp = client.execute(request);
				cmd = resp.getCommand();
				if (cmd.getResponse() == Response.OKAY) {
					success = true;
					break;
				}
			} catch (IOException exp) {
				exp.printStackTrace();
				client.close();
			}
		}
				
		SocketHost address = client.getLocal();
		if(success) {
			System.out.printf("%s send command to %s\n", (address == null ? "local" : address), host);
		} else {
			System.out.printf("%s cannot send command to %s\n", (address == null ? "local" : address), host);
		}

		client.close();
		return success;
	}

	public static void main(String[] args) {
		Shutdown shutdown = new Shutdown();
		if (args.length == 1) {
			String filename = args[0];
			shutdown.send(filename);
		} else {
			System.out.println("invalid!");
		}
	}

}