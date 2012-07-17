/**
 * 
 */
package com.lexst.data.pool;

import java.util.*;
import java.io.*;

import com.lexst.data.*;
import com.lexst.db.schema.*;
import com.lexst.remote.client.home.*;
import com.lexst.site.data.*;
import com.lexst.util.lock.SingleLock;
import com.lexst.fixp.*;
import com.lexst.fixp.client.FixpPacketClient;
import com.lexst.util.host.*;
import com.lexst.visit.*;
import com.lexst.log.client.*;

/**
 *
 * distribute new data to slave site
 */
public class ChunkPool extends LocalPool {
	
	private static ChunkPool selfHandle = new ChunkPool();
	
	private SingleLock lock = new SingleLock();
	
	private Map<Long, EntityElement> mapEntity = new TreeMap<Long, EntityElement>();
	
	private LinkedList<Long> queue = new LinkedList<Long>();

	private FixpPacketClient client = new FixpPacketClient();

	/**
	 * 
	 */
	private ChunkPool() {
		// TODO Auto-generated constructor stub
		super();
	}
	
	/**
	 * @return
	 */
	public static ChunkPool getInstance() {
		return ChunkPool.selfHandle;
	}
	
	/**
	 * update data to local disk (slave site)
	 * @param db
	 * @param table
	 * @param chunkid
	 * @param data
	 * @return
	 */
	public boolean setChunkEntity(String db, String table, long chunkid, byte[] data) {
		// only slave site
		DataSite site = Launcher.getInstance().getLocal();
		if (!site.isSlave()) {
			Logger.error("ChunkPool.setChunkEntity, current site rank is %d", site.getRank());
			return false;
		}
		// write data to local disk
		int ret = Install.setChunkEntity(db.getBytes(), table.getBytes(), chunkid, data);
		return ret == 0;
	}
	
	/**
	 * only prime site
	 * @param db
	 * @param table
	 * @param chunkid
	 * @param data
	 */
	public boolean update(String db, String table, long chunkid, byte[] data) {
		boolean empty = queue.isEmpty();
		EntityElement element = null;
		lock.lock();
		try {
			element = mapEntity.get(chunkid);
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			lock.unlock();
		}
		if(element == null) {
			element = new EntityElement(db, table, chunkid);
			SiteHost home = Launcher.getInstance().getHome();
			HomeClient client = super.bring(home);
			if(client == null) {
				Logger.error("ChunkPool.update, cannot connect home site: %s", home);
				return false;
			}
			SiteHost[] sites = null;
			try {
				sites = client.findDataSite(db, table, chunkid);
			} catch (VisitException exp) {
				Logger.error(exp);
			}
			super.complete(client);
			
			if(sites == null) {
				Logger.error("ChunkPool.update, cannot find data site '%s-%s %X'", db, table, chunkid);
				return false;
			}
			
			SiteHost local = Launcher.getInstance().getLocal().getHost();
			for(SiteHost site : sites) {
				if(site.equals(local)) continue;
				element.addHost(site.getUDPHost());
			}
			
			lock.lock();
			try {
				mapEntity.put(chunkid, element);
			} catch (Throwable exp) {
				Logger.error(exp);
			} finally {
				lock.unlock();
			}
		}
		lock.lock();
		try {
			element.add(data);
			queue.add(chunkid);
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			lock.unlock();
		}
		if(empty) wakeup();
		return true;
	}
	
	/**
	 * send chunk data to data site
	 * @param remote
	 * @param db
	 * @param table
	 * @param chunkid
	 * @param data
	 * @return
	 */
	private boolean send(SocketHost remote, String db, String table, long chunkid, byte[] data) {
		Command cmd = new Command(Request.DATA, Request.SET_CHUNK_ENTITY);
		Packet packet = new Packet(remote, cmd);
		packet.addMessage(Key.SCHEMA, db);
		packet.addMessage(Key.TABLE, table);
		packet.addMessage(Key.CHUNK_ID, chunkid);
		packet.setData(data);
		
		Packet reply = null;
		try {
			reply = client.batch(packet);
		} catch(IOException exp) {
			Logger.error(exp);
		}
		if(reply != null) {
			cmd = reply.getCommand();
			if(cmd.getResponse() == Response.OKAY) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 
	 */
	private void subprocess() {
		// check send queue
		while (!queue.isEmpty()) {
			EntityElement element = null;
			byte[] data = null;
			lock.lock();
			try {
				long chunkid = queue.poll();
				element = mapEntity.get(chunkid);
				data = element.next();
			} catch (Throwable exp) {
				Logger.error(exp);
			} finally {
				lock.unlock();
			}
			if(element == null) continue;

			Space space = element.getSpace();
			for (SocketHost remote : element.listHosts()) {
				send(remote, space.getSchema(), space.getTable(), element.getChunkId(), data);
			}
		}
	}

	private boolean bind() {
		try {
			return client.bind();
		} catch (IOException exp) {
			Logger.error(exp);
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		// TODO Auto-generated method stub
		return bind();
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		while (!super.isInterrupted()) {
			if (queue.isEmpty()) {
				delay(1000);
			} else {
				subprocess();
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// TODO Auto-generated method stub
		client.close();
	}

}