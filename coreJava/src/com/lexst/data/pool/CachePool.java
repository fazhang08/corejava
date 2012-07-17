/**
 * 
 */
package com.lexst.data.pool;

import java.io.*;
import java.util.*;

import org.w3c.dom.*;

import com.lexst.data.*;
import com.lexst.db.schema.*;
import com.lexst.fixp.*;
import com.lexst.fixp.client.FixpPacketClient;
import com.lexst.log.client.*;
import com.lexst.remote.client.home.*;
import com.lexst.util.host.*;
import com.lexst.util.lock.SingleLock;
import com.lexst.visit.*;
import com.lexst.xml.*;


public class CachePool extends LocalPool {
	
	private static CachePool selfHandle = new CachePool();
	
	private SingleLock lock = new SingleLock();

	private Map<Long, EntityElement> mapEntity = new TreeMap<Long, EntityElement>();
	
	private LinkedList<Long> queue = new LinkedList<Long>();
		
	private LinkedList<EntityIdentify> deletes = new LinkedList<EntityIdentify>();
	
	private FixpPacketClient client = new FixpPacketClient();

	/**
	 * 
	 */
	private CachePool() {
		super();
	}
	
	/**
	 * @return
	 */
	public static CachePool getInstance() {
		return CachePool.selfHandle;
	}
	
	/**
	 * save temp data to local disk
	 * @param db
	 * @param table
	 * @param chunkid
	 * @param data
	 */
	public boolean setCacheEntity(String db, String table, long chunkid, byte[] data) {
		int ret = Install.setCacheEntity(db.getBytes(), table.getBytes(), chunkid, data);
//		if(ret == 0) {
//			// save it
//			inputs.add(new EntityIdentify(db, table, chunkid));
//		}
		return ret == 0;
	}

	/**
	 * delete temp data from local disk
	 * @param db
	 * @param table
	 * @param chunkid
	 * @return
	 */
	public boolean deleteCacheEntity(String db, String table, long chunkid) {
		int ret = Install.deleteCacheEntity(db.getBytes(), table.getBytes(), chunkid);
		return ret == 0;
	}

	/**
	 * save a cache entity, and delete it
	 * @param chunkid
	 * @return
	 */
	public boolean delete(String db, String table, long chunkid) {
		lock.lock();
		try {
			return deletes.add(new EntityIdentify(db, table, chunkid));
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			lock.unlock();
		}
		return false;
	}
	
	/**
	 * @param hosts
	 * @return
	 */
	private SocketHost[] choose(SiteHost[] hosts, int limit) {
		Map<SocketHost, Integer> map1 = new HashMap<SocketHost, Integer>(32);

		for (EntityElement element : mapEntity.values()) {
			for (SocketHost host : element.listHosts()) {
				Integer value = map1.get(host);
				if (value == null) {
					map1.put(host, new Integer(1));
				} else {
					map1.put(host, new Integer(value.intValue() + 1));
				}
			}
		}
		
		Map<Integer, ArrayList<SocketHost>> map2 = new TreeMap<Integer, ArrayList<SocketHost>>();
		SiteHost local = Launcher.getInstance().getLocal().getHost();
		for(SiteHost host : hosts) {
			if (local.equals(host)) continue; // ignore local host
			SocketHost udp = host.getUDPHost();
			Integer value = map1.get(udp);
			if(value == null) {
				ArrayList<SocketHost> a = new ArrayList<SocketHost>();
				a.add(udp);
				map2.put(new Integer(0), a);
			} else {
				ArrayList<SocketHost> a = map2.get(value);
				if(a == null) {
					a = new ArrayList<SocketHost>();
					map2.put(value, a);
				}
				a.add(udp);
			}
		}
		
		ArrayList<SocketHost> array = new ArrayList<SocketHost>();
		for (Integer value : map2.keySet()) {
			if (array.size() >= limit) break;
			for (SocketHost host : map2.get(value)) {
				array.add(host);
				if (array.size() >= limit) break;
			}
		}
		
		int size = array.size();
		if(size == 0) return null;
		SocketHost[] s = new SocketHost[size];
		return array.toArray(s);
	}
	
	/**
	 * save a cache data, only prime site
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
			// 去HOME节点找DATA节点地址
			SiteHost home = Launcher.getInstance().getHome();
			HomeClient client = super.bring(home);
			if (client == null) {
				Logger.error("CachePool.update, cannot connect to home site: %s", home);
				return false;
			}
			SiteHost[] sites = null;
			try {
				sites = client.findDataSite(db, table); //, RankSite.PRIME_SITE);
			} catch (VisitException exp) {
				Logger.error(exp);
			} catch (Throwable exp) {
				Logger.fatal(exp);
			}
			super.complete(client);
			
			if (sites == null || sites.length == 0) {
				Logger.error("CachePool.update, cannot find data site");
				return false;
			}
			// choose site, and save socket host
			SocketHost[] hosts = choose(sites, 2);
			if (hosts == null) {
				Logger.error("CachePool.update, cannot choose data host");
				return false;
			}
			for (int i = 0; i < hosts.length; i++) {
				element.addHost(hosts[i]);
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
		// save cache data
		lock.lock();
		try {
			element.add(data);
			queue.add(chunkid);
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			lock.unlock();
		}
		if(empty) this.wakeup();
		return true;
	}
	
	/**
	 * send packet to other node
	 * 
	 * @param remote
	 * @param data
	 * @return
	 */
	private boolean send(SocketHost remote, String db, String table, long chunkid, byte[] data) {
		Command cmd = new Command(Request.DATA, Request.SET_CACHE_ENTITY);
		Packet packet = new Packet(remote, cmd);
		packet.addMessage(Key.SCHEMA, db);
		packet.addMessage(Key.TABLE, table);
		packet.addMessage(Key.CHUNK_ID, chunkid);
		packet.setData(data);
		
		Packet reply = null;
		try {
			reply = client.batch(packet);
		} catch (IOException exp) {
			Logger.error(exp);
		}
		if (reply != null) {
			cmd = reply.getCommand();
			if (cmd.getResponse() == Response.OKAY) {
				// success
				return true;
			}
		}
		return false;
	}
	
	/**
	 * send packet to remote site, delete a cache entity
	 * @param remote
	 * @param db
	 * @param table
	 * @param chunkid
	 * @return
	 */
	private boolean delete(SocketHost remote, String db, String table, long chunkid) {
		Command cmd = new Command(Request.DATA, Request.DELETE_CACHE_ENTITY);
		Packet packet = new Packet(remote, cmd);
		packet.addMessage(Key.SCHEMA, db);
		packet.addMessage(Key.TABLE, table);
		packet.addMessage(Key.CHUNK_ID, chunkid);
		
		Packet reply = null;
		try {
			reply = client.batch(packet);
		} catch (IOException exp) {
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
	 * execute send and delete job
	 */
	private void subprocess() {
		// check send queue
		while(!queue.isEmpty()) {
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
			
			// send cache data to data site
			Space space = element.getSpace();
			for(SocketHost remote : element.listHosts()) {
				send(remote, space.getSchema(), space.getTable(), element.getChunkId(), data);
			}
		}
		
		// check delete element
		while (!deletes.isEmpty()) {
			EntityIdentify ei = null;
			EntityElement element = null;
			lock.lock();
			try {
				ei = deletes.poll();
				element = mapEntity.remove(ei.getChunkId());
			} catch (Throwable exp) {
				Logger.error(exp);
			} finally {
				lock.unlock();
			}
			if (element == null) continue;

			Space space = element.getSpace();
			for (SocketHost remote : element.listHosts()) {
				this.delete(remote, space.getSchema(), space.getTable(), element.getChunkId());
			}
		}
	}
	
	/**
	 * bind to local address
	 * @return
	 */
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
		// read configure file
		return bind();
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		while(!super.isInterrupted()) {
			if(queue.isEmpty() && deletes.isEmpty()) {
				this.delay(1000);
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
		// close socket
		client.close();
	}
	
	/**
	 * resolve xml data
	 * @param data
	 * @return
	 */
	public boolean resolve(byte[] data) {
		XMLocal xml = new XMLocal();
		Document doc = xml.loadXMLSource(data);
		if(doc == null) return false;

		NodeList list = doc.getElementsByTagName("element");
		int size = list.getLength();
		for(int i = 0; i < size; i++) {
			Element element = (Element)list.item(i);
			String db = xml.getValue(element, "db");
			String table = xml.getValue(element, "table");
			String chunkid = xml.getValue(element, "chunkid");
			
			long id = Long.parseLong(chunkid);
			EntityElement entity = new EntityElement(db, table, id);

			NodeList sub = element.getElementsByTagName("host");
			int len = sub.getLength();
			for(int j = 0; j < len; j++) {
				Element elem = (Element)sub.item(j);
				String ip = xml.getValue(elem, "ip");
				String port = xml.getValue(elem, "port");
				SocketHost host = new SocketHost(SocketHost.UDP, ip, Integer.parseInt(port));
				entity.addHost(host);
			}
			
			mapEntity.put(id, entity);
		}
		
		return true;
	}

	/**
	 * build xml
	 * @return
	 */
	public byte[] build() {
		StringBuilder buff = new StringBuilder();
		
		for(long id : mapEntity.keySet()) {
			EntityElement element = mapEntity.get(id);
			String db = XML.element("db", element.getSpace().getSchema());
			String table = XML.element("table", element.getSpace().getTable());
			String space = XML.element("space", db + table);
			String chunkid = XML.element("chunkid", element.getChunkId());
			
			StringBuilder b = new StringBuilder();
			for(SocketHost host : element.listHosts()) {
				String ip = XML.element("ip", host.getIP());
				String port = XML.element("port", host.getPort());
				String address = XML.element("host", ip + port);
				b.append(address);
			}
			String s = XML.element("host-list", b.toString());
			
			String text = XML.element("element", space + chunkid + s);
			
			buff.append(text);
		}
		String app = XML.element("list", buff.toString());
		
		return XML.toUTF8(XML.head_utf8 + app);
	}
	
}