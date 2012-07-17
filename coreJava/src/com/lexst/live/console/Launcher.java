/**
 * 
 */
package com.lexst.live.console;

import java.io.*;

import com.lexst.algorithm.collect.*;
import com.lexst.live.*;
import com.lexst.live.pool.*;
import com.lexst.log.client.*;
import com.lexst.site.live.*;
import com.lexst.thread.*;
import com.lexst.util.*;
import com.lexst.util.host.*;

public class Launcher extends BasicLauncher implements LiveListener {

	private static Launcher selfHandle = new Launcher();

	// local server address
	private LiveSite local = new LiveSite();
	
	private Terminal terminal = new Terminal();
	
	/**
	 * default constructor
	 */
	private Launcher() {
		super();
		super.setExitVM(true);
		super.setLogging(true);
		this.initIP();
		// cannot print log
		fixpStream.setPrint(false);
		fixpPacket.setPrint(false);
		// init invoker
		streamImpl = new LiveStreamInvoker();
		packetImpl = new LivePacketInvoker(this, fixpPacket);
		
		Logger.setLevel(LogLevel.none);
	}

	/**
	 * @return
	 */
	public static Launcher getInstance() {
		return Launcher.selfHandle;
	}
	
	/**
	 * @return
	 */
	public Console getConsole() {
		return terminal.getConsole();
	}

	/**
	 * get local site 
	 * @return
	 */
	public LiveSite getLocal() {
		return this.local;
	}
	
	private void initIP() {
		String ip = IP4Style.getFirstPrivateAddress();
		local.setHost(ip, 0, 0);
	}
	
	/**
	 * load top pool
	 * @return
	 */
	private boolean loadPool() {
		return TopPool.getInstance().start();
	}

	/**
	 * stop top pool
	 */
	private void stopPool() {
		TopPool.getInstance().stop();
		while (TopPool.getInstance().isRunning()) {
			this.delay(200);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		if(!terminal.initialize()) {
			System.out.println("cannot init system console");
			return false;
		}
		
		TopPool.getInstance().setLocal(local);
		TopPool.getInstance().setLiveListener(this);

		//1. load pool
		boolean success = loadPool();
		//2. load listen
		if (success) {
			success = loadListen(null, local.getHost());
			if (success) {
				SocketHost host = fixpStream.getLocal();
				local.getHost().setTCPort(host.getPort());
				host = fixpPacket.getLocal();
				local.getHost().setUDPort(host.getPort());
			} else {
				stopPool();
				return false;
			}
		}
		//3. show window, and login
		if (success) {
			success = terminal.login();
			if(!success) {
				stopListen();
				stopPool();
			}
		}
		//4. start collect pool
		success = CollectTaskPool.getInstance().start();
		if(!success) {
			stopListen();
			stopPool();
		}

		return success;
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		while(!isInterrupted()) {
			boolean exit = terminal.check();
			if (exit) {
				setInterrupted(true);
				break;
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		this.stopPool();
		CollectTaskPool.getInstance().stop();
		this.stopListen();
	}

	/* (non-Javadoc)
	 * @see com.lexst.live.LiveListener#flicker()
	 */
	@Override
	public void flicker() {
		// TODO Auto-generated method stub
		TopPool.getInstance().replyActive();
	}

	/* (non-Javadoc)
	 * @see com.lexst.live.LiveListener#shutdown()
	 */
	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		this.stop();
	}
	
	/* (non-Javadoc)
	 * @see com.lexst.live.LiveListener#disconnect()
	 */
	@Override
	public void disconnect() {
		// TODO Auto-generated method stub
		System.out.println("connect interrupted!");
	}

	/* (non-Javadoc)
	 * @see com.lexst.live.LiveListener#active(int, com.lexst.util.host.SocketHost)
	 */
	@Override
	public void active(int num, SocketHost topsite) {
		this.hello(num, local.getType(), topsite);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Launcher.getInstance().start();
	}

}