/**
 * 
 */
package com.lexst.algorithm.diffuse;

import java.io.*;

import com.lexst.util.host.*;

public class DCResult {

	private SiteHost host;
	protected ByteArrayOutputStream buff;
	protected int count;

	/**
	 * @param host
	 * @param bufflen
	 */
	public DCResult(SiteHost host, int bufflen) {
		super();
		count = 0;
		this.setHost(host);
		if (bufflen < 16) bufflen = 16;
		buff = new ByteArrayOutputStream(bufflen);
	}
	
	public DCResult(SiteHost host) {
		this(host, 0);
	}

	public int count() {
		return this.count;
	}

	public void setHost(SiteHost s) {
		this.host = new SiteHost(s);
	}
	public SiteHost getHost() {
		return host;
	}
	
	public void flush(byte[] b, int off, int len) {
		buff.write(b, off, len);
	}

	public int size() {
		return buff.size();
	}

	public byte[] data() {
		if (buff.size() == 0) return null;
		return buff.toByteArray();
	}
}