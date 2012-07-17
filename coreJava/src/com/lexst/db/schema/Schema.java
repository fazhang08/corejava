/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com, All rights reserved
 * 
 * lexst database configure
 * 
 * @author yj.liang lexst@126.com
 * 
 * @version 1.0 3/8/2009
 * 
 * @see com.lexst.db.schema
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.db.schema;

import java.io.*;
import java.util.*;

import org.w3c.dom.*;

import com.lexst.util.*;
import com.lexst.xml.*;

public class Schema implements Serializable {

	private static final long serialVersionUID = 1L;

	/* database name (ignore case) */
	private String name;
	
	/* all character set */
	private String charname;
	private String ncharname;
	private String wcharname;
	
	/* allow size, default is 0 */
	private long maxsize;

	/* space -> table object */
	private Map<Space, Table> mapTable = new TreeMap<Space, Table>();

	/* space -> optimize trigger */
	private Map<Space, TimeSwitch> mapSwitch = new HashMap<Space, TimeSwitch>();

	/**
	 * default
	 */
	public Schema() {
		super();
		maxsize = 0L;
	}

	/**
	 * @param name
	 */
	public Schema(String name) {
		this();
		setName(name);
	}

	public void setName(String s) {
		this.name = s;
	}
	public String getName() {
		return name;
	}
	
	public void setMaxSize(long size) {
		if (size < 0L) {
			throw new IllegalArgumentException("invalid maxsize:" + size);
		}
		this.maxsize = size;
	}

	public long getMaxSize() {
		return this.maxsize;
	}

	public void setCharset(String s) {
		this.charname = s;
	}
	public String getCharset() {
		return this.charname;
	}

	public void setNCharset(String s) {
		this.ncharname = s;
	}
	public String getNCharset() {
		return this.ncharname;
	}

	public void setWCharset(String s ) {
		this.wcharname = s;
	}
	public String getWCharset() {
		return this.wcharname;
	}

	public boolean exists(Space space) {
		return mapTable.get(space) != null;
	}
	
	public Set<Space> spaces() {
		TreeSet<Space> set = new TreeSet<Space>();
		if (!mapTable.isEmpty()) {
			set.addAll(mapTable.keySet());
		}
		return set;
	}

	public Table findTable(Space space) {
		return mapTable.get(space);
	}

	public TimeSwitch findTimeSwitch(Space space) {
		return mapSwitch.get(space);
	}

	/**
	 * find chunk size
	 * @param space
	 * @return
	 */
	public int findChunkSize(Space space) {
		Table table = mapTable.get(space);
		if(table != null) {
			return table.getChunkSize();
		}
		return -1;
	}
	
	/**
	 * set chunk size
	 * @param space
	 * @param size
	 * @return
	 */
	public boolean setChunkSize(Space space, int size) {
		Table table = mapTable.get(space);
		if (table == null) return false;
		table.setChunkSize(size);
		return true;
	}
	
	/**
	 * set chunk optimize rule
	 * @param space
	 * @param type
	 * @param time
	 * @return
	 */
	public boolean setOptimizeTime(Space space, int type, long time) {
		if(!mapTable.containsKey(space)) {
			return false;
		}
		TimeSwitch trigger = new TimeSwitch(space, type, time);
		mapSwitch.put(space, trigger);
		return true;
	}

	/**
	 * get expired space 
	 * @return
	 */
	public List<Space> getExpiredSpace() {
		List<Space> array = new ArrayList<Space>();
		for(Space space : mapSwitch.keySet()) {
			TimeSwitch trig = mapSwitch.get(space);
			if(trig.isExpired()) {
				array.add(space);
				trig.bring(); // make next time
			}
		}
		return array;
	}

	/**
	 * @param space
	 * @param table
	 * @return
	 */
	public boolean add(Space space, Table table) {
		if(mapTable.containsKey(space)) {
			return false;
		}
		return mapTable.put(space, table) == null;
	}

	/**
	 * delete a table schema
	 * @param space
	 * @return
	 */
	public boolean remove(Space space) {
		boolean success = (mapTable.remove(space) != null);
		this.mapSwitch.remove(space);
		return success;
	}

	public Collection<Table> listTable() {
		List<Table> array = new ArrayList<Table>();
		if(!mapTable.isEmpty()) {
			array.addAll(mapTable.values());
		}
		return array;
	}

	public String buildXML() {
		StringBuilder buff = new StringBuilder();
		buff.append(XML.element("name", name));
		buff.append(XML.element("char", charname));
		buff.append(XML.element("nchar", ncharname));
		buff.append(XML.element("wchar", wcharname));
		buff.append(XML.element("maxsize", maxsize));

		for (Space space : mapTable.keySet()) {
			StringBuilder a = new StringBuilder();
			Table table = mapTable.get(space);
			String tablename = table.getSpace().getTable();

			a.append(XML.element("name", tablename));
			// time trigger
			TimeSwitch trigger = mapSwitch.get(space);
			if (trigger != null) {
				StringBuilder bu = new StringBuilder();
				bu.append(XML.element("type", trigger.getType()));
				bu.append(XML.element("time", trigger.getTime()));
				a.append(XML.element("trigger", bu.toString()));
			}

			byte[] b = Base64.encode(table.build());
			String text = new String(b);
			MD5Encoder md5 = new MD5Encoder();
			String hex = md5.encode(text);
			String s = String.format("<schema encode=\"base64\" tag=\"%s\"><![CDATA[%s]]></schema>", hex, text);
			a.append(s);

			String body = XML.element("table", a.toString());
			buff.append(body);
		}
		return XML.element("database", buff.toString());
	}

	public boolean parseXML(XMLocal xml, Element element) {
		this.name = xml.getXMLValue(element.getElementsByTagName("name"));
		this.charname = xml.getXMLValue(element.getElementsByTagName("char"));
		this.ncharname = xml.getXMLValue(element.getElementsByTagName("nchar"));
		this.wcharname = xml.getXMLValue(element.getElementsByTagName("wchar"));
		try {
			String s = xml.getXMLValue(element.getElementsByTagName("maxsize"));
			maxsize = Long.parseLong(s);
		} catch (NumberFormatException exp) {
			return false;
		}

		NodeList nodes = element.getElementsByTagName("table");
		// not found , return true;
		if (nodes == null) return true;

		int len = nodes.getLength();
		for (int j = 0; j < len; j++) {
			Element sub = (Element) nodes.item(j);
			String tabname = xml.getXMLValue(sub.getElementsByTagName("name"));
			Element head = (Element) sub.getElementsByTagName("schema").item(0);
			String tag = head.getAttribute("tag");
			String encode = head.getAttribute("encode");
			String text = head.getTextContent();

			// check tag
			MD5Encoder md5 = new MD5Encoder();
			String hex = md5.encode(text);
			if (!hex.equals(tag)) {
				return false;
			}

			byte[] b = null;
			if ("base64".equalsIgnoreCase(encode)) {
				b = Base64.decode(text.getBytes());
			} else {
				return false;
			}

			Table table = new Table();
			int end = table.resolve(b, 0);
			if (end != b.length) {
				return false;
			}

			// save table
			Space space = new Space(this.name, tabname);
			if (mapTable.put(space, table) != null) {
				return false;
			}
			
			// save space trigger
			NodeList trig = sub.getElementsByTagName("trigger");
			if (trig != null && trig.getLength() == 1) {
				Element el = (Element) trig.item(0);
				String type = xml.getXMLValue(el.getElementsByTagName("type"));
				String time = xml.getXMLValue(el.getElementsByTagName("time"));
				TimeSwitch tt = new TimeSwitch(space, Integer.parseInt(type), Long.parseLong(time));
				mapSwitch.put(space, tt);
			}
		}
		return true;
	}

}