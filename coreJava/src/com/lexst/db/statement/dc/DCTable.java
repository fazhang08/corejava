/**
 * 
 */
package com.lexst.db.statement.dc;

import java.io.*;
import java.util.*;

import com.lexst.util.host.SiteHost;

public class DCTable {

	private Map<SiteHost, DCArea> mapHost = new TreeMap<SiteHost, DCArea>();

	/**
	 * 
	 */
	public DCTable() {
		super();
	}

	/**
	 * @param table
	 */
	public DCTable(DCTable table) {
		this();
		this.add(table);
	}

	/**
	 * @param host
	 * @param identity
	 * @param field
	 * @return
	 */
	public boolean add(SiteHost host, long identity, DCField field) {
		DCArea area = mapHost.get(host);
		if (area == null) {
			area = new DCArea(identity, host);
			mapHost.put(host, area);
		}
		return area.add(field);
	}

	/**
	 * @param table
	 */
	public void add(DCTable table) {
		for (SiteHost host : table.mapHost.keySet()) {
			DCArea area = table.mapHost.get(host);
			DCArea org = mapHost.get(host);
			if (org == null) {
				mapHost.put(host, area);
			} else {
				for (DCField field : area.list()) {
					org.add(field);
				}
			}
		}
	}

	public Set<SiteHost> keySet() {
		return mapHost.keySet();
	}

	public DCArea get(SiteHost host) {
		return mapHost.get(host);
	}

	public long length() {
		long count = 0;
		for (DCArea file : mapHost.values()) {
			count += file.length();
		}
		return count;
	}

	public boolean isEmpty() {
		return mapHost.isEmpty();
	}

	public int size() {
		return mapHost.size();
	}

	/**
	 * @return
	 */
	public byte[] build() {
		ByteArrayOutputStream buff = new ByteArrayOutputStream();
		for (SiteHost host : mapHost.keySet()) {
			DCArea area = mapHost.get(host);
			byte[] b = area.build();
			buff.write(b, 0, b.length);
		}
		return buff.toByteArray();
	}

	/**
	 * @param data
	 * @param pos
	 * @param len
	 * @return
	 */
	public int resolve(byte[] data, int pos, int len) {
		int off = pos;

		for (int index = 0; index < len;) {
			DCArea area = new DCArea();
			int length = area.resolve(data, off);
			if (length < 1) return -1;
			off += length;
			index += length;

			SiteHost host = area.getHost();
			DCArea org = mapHost.get(host);
			if (org == null) {
				mapHost.put(host, area);
			} else {
				for (DCField field : area.list()) {
					org.add(field);
				}
			}
		}

		return off - pos;
	}
}