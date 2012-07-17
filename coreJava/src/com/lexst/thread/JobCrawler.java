/**
 * 
 */
package com.lexst.thread;

import java.io.*;
import java.net.*;
import java.util.*;

import com.lexst.fixp.*;
import com.lexst.fixp.client.*;
import com.lexst.log.client.*;
import com.lexst.util.*;
import com.lexst.util.host.*;
import com.lexst.util.lock.*;

public class JobCrawler extends VirtualThread {
	
	public final static int UNDEFINE = 1;
	public final static int EXISTED = 2;
	public final static int NOTFOUND = 3;

	private SingleLock lock = new SingleLock();

	private FixpPacketClient sender;

	private FixpPacketClient receiver;

	private Set<Integer> success = new TreeSet<Integer>();

	private Set<Integer> fail = new TreeSet<Integer>();
	
	public static String explain(int id) {
		switch (id) {
		case JobCrawler.UNDEFINE:
			return "Undefine";
		case JobCrawler.EXISTED:
			return "Existed";
		case JobCrawler.NOTFOUND:
			return "NotFound";
		}
		return "Invalid";
	}

	/**
	 * 
	 */
	public JobCrawler() {
		super();
	}
	
	private void receive() throws IOException {
		Packet resp = receiver.receive();
		
		Command cmd = resp.getCommand();
		short code = cmd.getResponse();
		Integer packetId = resp.findInt(Key.PACKET_IDENTIFY);
		if (packetId == null) return;
		
		lock.lock();
		try {
			if (code == Response.OKAY) {
				success.add(packetId);
			} else {
				fail.add(packetId);
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			lock.unlock();
		}
	}
	
	private boolean bindReceiver() {
		String ip = IP4Style.getFirstPrivateAddress();
		InetSocketAddress local = new InetSocketAddress(ip, 0);

		this.closeReceiver();
		// bind local address
		receiver = new FixpPacketClient();
		try {
			receiver.bind(local);
			return true;
		} catch (IOException exp) {
			Logger.error(exp);
		}
		return false;
	}
	
	private void closeReceiver() {
		if (receiver != null) {
			receiver.close();
			receiver = null;
		}
	}
	
	private boolean bindSender() {
		String ip =  IP4Style.getFirstPrivateAddress() ;
		InetSocketAddress local = new InetSocketAddress(ip, 0);
		
		this.closeSender();
		// bind local address
		sender = new FixpPacketClient();
		try {
			sender.bind(local);
			return true;
		} catch (IOException exp) {
			Logger.error(exp);
		}
		return false;
	}
	
	private void closeSender() {
		if (sender != null) {
			sender.close();
			sender = null;
		}
	}
	
	private void close() {
		this.closeSender();
		this.closeReceiver();
	}
	
	/**
	 * 检查运行节点是否正在运行
	 * @param targets
	 * @param detectsite
	 * @param detectime
	 * @return
	 */
	public int detect(List<SiteHost> targets, SiteHost detectsite, long detectime) {
		int allsize = targets.size();
		if (allsize < 1) return JobCrawler.UNDEFINE;
		
		// bind sender
		if(!bindSender()) {
			this.close();
			return JobCrawler.UNDEFINE;
		}
		// bind receiver
		if(!bindReceiver()) {
			this.close();
			return JobCrawler.UNDEFINE;
		}
		
		Map<Integer, SiteHost> map = new HashMap<Integer, SiteHost>(allsize * 2);
		int packetId = 1;
		for (SiteHost host : targets) {
			map.put(packetId++, host);
		}
		
		long unitime = 30 * 1000;
		long endtime = System.currentTimeMillis() + detectime;
		Map<Integer, SiteHost> array = new HashMap<Integer, SiteHost>(allsize * 2);

		// start thread
		this.start();
		this.delay(1000);

		while (System.currentTimeMillis() <= endtime) {
			long last = System.currentTimeMillis() + unitime;
			array.clear();
			
			lock.lock();
			try {
				for (int pid : success) {
					map.remove(pid);
				}
				for (int pid : fail) {
					map.remove(pid);
				}
				array.putAll(map);
			} catch (Throwable exp) {
				Logger.fatal(exp);
			} finally {
				lock.unlock();
			}
			
			if(map.isEmpty()) break;
			
			for (int pid : array.keySet()) {
				SiteHost host = array.get(pid);
				try {
					this.send(host, detectsite, pid);
				} catch (IOException exp) {
					Logger.error(exp);
				}
			}
			
			if (allsize == success.size() + fail.size()) break;

			// sleep time
			long left = last - System.currentTimeMillis();
			if(left > 0) {
				this.delay(left);
			}
		}
		
		this.setInterrupted(true);
		this.close();
			
		if(success.size() == allsize) return JobCrawler.EXISTED;
		else if(fail.size() == allsize) return JobCrawler.NOTFOUND;
		return JobCrawler.UNDEFINE;
	}

	/**
	 * scan job site
	 * @param packetId
	 * @param host
	 * @throws IOException
	 */
	private void send(SiteHost host, SiteHost detectsite, int packetId) throws IOException {
		SocketHost local = receiver.getLocal();
		Command cmd = new Command(Request.NOTIFY, Request.SCANHUB);
		Packet packet = new Packet(host.getUDPHost(), cmd);
		packet.addMessage(Key.PACKET_IDENTIFY, packetId);
		packet.addMessage(Key.SERVER_ADDRESS, detectsite.toString());
		packet.addMessage(Key.LOCAL_ADDRESS, local.toString());
		packet.addMessage(Key.TIMEOUT, 20); //receiver timeout:20 second
		// send to job site(log, data, work, build, call)
		sender.send(packet);
	}

	/* (non-Javadoc)
	 * @see com.dwms.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.dwms.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		while (!super.isInterrupted()) {
			if (receiver == null) {
				if (!bindReceiver()) {
					this.delay(2000);
					continue;
				}
			}
			try {
				if (receiver != null) {
					this.receive();
				}
			} catch (IOException exp) {
				this.closeReceiver();
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.dwms.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// TODO Auto-generated method stub
	}

}