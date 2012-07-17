/**
 * 
 */
package com.lexst.db.statement.dc;

import java.io.*;
import java.util.*;

import com.lexst.util.*;
import com.lexst.util.host.SiteHost;

public class DCArea implements Serializable {

	private static final long serialVersionUID = 2843537925715348030L;

	/* job identity */
	private long identity;

	/* data site */
	private SiteHost host;

	/* use time */
	private int timeout;

	private List<DCField> array = new ArrayList<DCField>();

	/**
	 * 
	 */
	public DCArea() {
		super();
	}

	public DCArea(long identity) {
		this();
		this.setIdentity(identity);
	}

	public DCArea(long identity, SiteHost host) {
		this();
		this.setIdentity(identity);
		this.setHost(host);
	}

	/**
	 * set job identity
	 * 
	 * @param i
	 */
	public void setIdentity(long i) {
		this.identity = i;
	}

	/**
	 * get job identity
	 * 
	 * @return
	 */
	public long getIdentity() {
		return this.identity;
	}

	/**
	 * set data site
	 * @param s
	 */
	public void setHost(SiteHost s) {
		host = new SiteHost(s);
	}

	/**
	 * get data site
	 * @return
	 */
	public SiteHost getHost() {
		return host;
	}

	/**
	 * set max save time
	 * @param i
	 */
	public void setTimeout(int i) {
		this.timeout = i;
	}

	/**
	 * get max save time
	 * @return
	 */
	public int getTimeout() {
		return this.timeout;
	}

	public boolean add(DCField field) {
		if (array.contains(field)) return true;
		return array.add(field);
	}

	public boolean remove(DCField filed) {
		return array.remove(filed);
	}

	public List<DCField> list() {
		return array;
	}

	public long length() {
		long count = 0;
		for (DCField field : array) {
			count += field.length();
		}
		return count;
	}

	public boolean isEmpty() {
		return array.isEmpty();
	}

	public int size() {
		return array.size();
	}

	public byte[] build() {
		ByteArrayOutputStream buff = new ByteArrayOutputStream();
		byte[] b = Numeric.toBytes(identity);
		buff.write(b, 0, b.length);
		byte[] ip = host.getIP().getBytes();
		b = Numeric.toBytes(ip.length);
		buff.write(b, 0, b.length);
		buff.write(ip, 0, ip.length);
		b = Numeric.toBytes(host.getTCPort());
		buff.write(b, 0, b.length);
		b = Numeric.toBytes(host.getUDPort());
		buff.write(b, 0, b.length);
		b = Numeric.toBytes(timeout);
		buff.write(b, 0, b.length);

		int count = 0;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (DCField field : array) {
			b = field.build();
			out.write(b, 0, b.length);
			count++;
		}
		b = Numeric.toBytes(count);
		buff.write(b, 0, b.length);
		if (count > 0) {
			b = out.toByteArray();
			buff.write(b, 0, b.length);
		}

		return buff.toByteArray();
	}

	public int resolve(byte[] data, int posi) {
		int off = posi;

		identity = Numeric.toLong(data, off, 8);
		off += 8;
		int size = Numeric.toInteger(data, off, 4);
		off += 4;
		String ip = new String(data, off, size);
		off += size;
		int tcport = Numeric.toInteger(data, off, 4);
		off += 4;
		int udport = Numeric.toInteger(data, off, 4);
		off += 4;
		host = new SiteHost(ip, tcport, udport);

		timeout = Numeric.toInteger(data, off, 4);
		off += 4;

		int count = Numeric.toInteger(data, off, 4);
		off += 4;
		for (int i = 0; i < count; i++) {
			DCField field = new DCField();
			int len = field.resolve(data, off);
			off += len;
			array.add(field);
		}

		return off - posi;
	}
	
	@Override
	public boolean equals(Object arg) {
		if (arg == null || !(arg instanceof DCArea)) {
			return false;
		} else if (arg == this) {
			return true;
		}

		DCArea a = (DCArea) arg;
		return identity == a.identity && host != null && host.equals(a.host);
	}

	@Override
	public int hashCode() {
		return (int) (identity >>> 32 & identity);
	}
}
