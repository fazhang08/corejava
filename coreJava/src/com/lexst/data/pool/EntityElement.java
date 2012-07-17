/**
 * 
 */
package com.lexst.data.pool;

import java.util.*;
import java.io.*;

import com.lexst.db.schema.*;
import com.lexst.util.host.SocketHost;


public class EntityElement {
	
	private Space space;
	private long chunkid;
	
	private ArrayList<SocketHost> hosts = new ArrayList<SocketHost>();
	
	private LinkedList<ByteArrayOutputStream> streams = new LinkedList<ByteArrayOutputStream>();

	/**
	 * 
	 */
	public EntityElement(String db, String table, long id) {
		super();
		space = new Space(db, table);
		this.chunkid = id;
	}
	
	public Space getSpace() {
		return space;
	}
	
	public long getChunkId() {
		return chunkid;
	}
	
	public void add(byte[] b) {
		ByteArrayOutputStream out = new ByteArrayOutputStream(b.length);
		out.write(b, 0, b.length);
		streams.add(out);
	}
	
	public boolean addHost(SocketHost host) {
		if (hosts.contains(host)) return false;
		return hosts.add(host);
	}

	public List<SocketHost> listHosts() {
		return hosts;
	}
	
	public byte[] next() {
		if(streams.isEmpty()) return null;
		
		ByteArrayOutputStream out = streams.poll();
		return out.toByteArray();
	}
}
