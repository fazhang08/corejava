/**
 * 
 */
package com.lexst.home.effect;

import java.util.*;
import java.io.*;

import org.w3c.dom.*;

import com.lexst.db.schema.*;
import com.lexst.log.client.*;
import com.lexst.util.effect.*;
import com.lexst.xml.*;
import com.lexst.util.Base64;

public class UserSpace extends Effect { 

	public final static String filename = "spaces.xml";

	// user space set
	private Map<Space, Table> mapSpace = new HashMap<Space, Table>();

	/**
	 * 
	 */
	public UserSpace() {
		super();
	}

	public boolean add(Space s, Table table) {
		super.lockSingle();
		try {
			return mapSpace.put(s, table) == null;
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockSingle();
		}
		return false;
	}
	
	public boolean remove(Space s) {
		super.lockSingle();
		try {
			return mapSpace.remove(s) != null;
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockSingle();
		}
		return false;
	}
	
	public boolean exists(Space space) {
		super.lockMulti();
		try {
			return mapSpace.containsKey(space);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockMulti();
		}
		return false;
	}

	public Table find(Space space) {
		super.lockMulti();
		try {
			return mapSpace.get(space);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockMulti();
		}
		return null;
	}
	
	/**
	 * @param space
	 * @return
	 */
	public int findChunkSize(Space space) {
		int size = -1;
		super.lockMulti();
		try {
			Table table = mapSpace.get(space);
			if(table != null) {
				size = table.getChunkSize();
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockMulti();
		}
		return size;
	}
	
	public void setChunkSize(Space space, int size) {
		super.lockSingle();
		try {
			Table table = mapSpace.get(space);
			if (table != null) {
				table.setChunkSize(size);
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockSingle();
		}
	}

	public Set<Space> keys() {
		HashSet<Space> set = new HashSet<Space>();
		super.lockSingle();
		try {
			set.addAll(mapSpace.keySet());
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockSingle();
		}
		return set;
	}

	public boolean isEmpty() {
		return mapSpace.isEmpty();
	}

	public int size() {
		return mapSpace.size();
	}
	
	public void clear() {
		mapSpace.clear();
	}
	
	public byte[] buildXML() {
		StringBuilder buff = new StringBuilder(10240);
		for (Space s : mapSpace.keySet()) {
			String s1 = element("db", s.getSchema());
			String s2 = element("table", s.getTable());
			String s3 = element("space", s1 + s2);
			buff.append(s3);
		}
		String body = element("app", buff.toString());
		return toUTF8(Effect.xmlHead + body);
	}

	public boolean parseXML(byte[] bytes) {
		XMLocal xml = new XMLocal();
		Document doc = xml.loadXMLSource(bytes);
		if(doc == null) {
			return false;
		}
		
		NodeList list =	doc.getElementsByTagName("space");
		int len = list.getLength();
		for(int i = 0; i <len; i++) {
			Element elem = (Element) list.item(i);
			String db =	xml.getXMLValue(elem.getElementsByTagName("db"));
			String table =	xml.getXMLValue(elem.getElementsByTagName("table"));
			Space s = new Space(db, table);
			this.add(s, null);
		}
		return true;
	}

	private String createTable(Table object) {
		try {
			ByteArrayOutputStream buff = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(buff);
			out.writeObject(object);
			byte[] b = buff.toByteArray();
			out.close();
			buff.close();
			byte[] s = Base64.encode(b);
			return new String(s);
		} catch (IOException exp) {

		}
		return null;
	}
		
	public byte[] createXML() {
		StringBuilder buff = new StringBuilder(10240);
		for (Space s : mapSpace.keySet()) {
			String s1 = element("db", s.getSchema());
			String s2 = element("table", s.getTable());
			Table table = mapSpace.get(s2);
			String s3 = element("object", createTable(table));

			String s6 = element("space", s1 + s2 + s3);
			buff.append(s6);
		}
		String body = element("app", buff.toString());
		return toUTF8(Effect.xmlHead + body);
	}

	private Table resolveTable(String s) {
		byte[] b = Base64.decode(s.getBytes());
		try {
			ByteArrayInputStream in = new ByteArrayInputStream(b);
			ObjectInputStream input = new ObjectInputStream(in);
			Table table = (Table) input.readObject();
			input.close();
			in.close();
			return table;
		} catch (IOException exp) {

		} catch (ClassNotFoundException exp) {

		}
		return null;
	}

	public boolean resolveXML(byte[] bytes) {
		XMLocal xml = new XMLocal();
		Document doc = xml.loadXMLSource(bytes);
		if(doc == null) {
			return false;
		}
		
		NodeList list =	doc.getElementsByTagName("space");
		int len = list.getLength();
		for (int i = 0; i < len; i++) {
			Element elem = (Element) list.item(i);
			String db = xml.getXMLValue(elem.getElementsByTagName("db"));
			String table = xml.getXMLValue(elem.getElementsByTagName("table"));
			String object = xml.getXMLValue(elem.getElementsByTagName("object"));
			Table instance = this.resolveTable(object);
			Space s = new Space(db, table);
			this.add(s, instance);
		}
		return true;
	}
}