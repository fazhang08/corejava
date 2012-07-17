/**
 * 
 */
package com.lexst.home.effect;

import java.util.*;
import java.io.*;

import org.w3c.dom.*;

import com.lexst.db.charset.*;
import com.lexst.util.effect.*;
import com.lexst.util.Base64;
import com.lexst.xml.*;

public class UserCharset extends Effect {
	
	/* database name -> charset */
	private Map<String, SQLCharset> mapCharset = new TreeMap<String, SQLCharset>();

	/**
	 * 
	 */
	public UserCharset() {
		super();
	}

	/**
	 * add schema charset
	 * @param db
	 * @param charset
	 * @return
	 */
	public boolean add(String db, SQLCharset charset) {
		db = db.trim().toLowerCase();
		boolean success = false;
		super.lockSingle();
		try {
			if (!mapCharset.containsKey(db)) {
				mapCharset.put(db, charset);
				success = true;
			}
		} catch (Throwable exp) {

		} finally {
			super.unlockSingle();
		}
		return success;
	}
	
	/**
	 * remove schema charset
	 * @param db
	 * @return
	 */
	public SQLCharset remove(String db) {
		db = db.trim().toLowerCase();
		super.lockSingle();
		try {
			return mapCharset.remove(db);
		} catch (Throwable exp) {

		} finally {
			super.unlockSingle();
		}
		return null;
	}
	
	/**
	 * find schema charset
	 * @param db
	 * @return
	 */
	public SQLCharset find(String db) {
		db = db.trim().toLowerCase();
		super.lockMulti();
		try {
			return mapCharset.get(db);
		} catch (Throwable exp) {

		} finally {
			super.unlockMulti();
		}
		return null;
	}
	
	public Set<String> keys() {
		HashSet<String> set = new HashSet<String>();
		super.lockSingle();
		try {
			set.addAll(mapCharset.keySet());
		} catch (Throwable exp) {

		} finally {
			super.unlockSingle();
		}
		return set;
	}
	
	public boolean isEmpty() {
		return mapCharset.isEmpty();
	}

	public int size() {
		return mapCharset.size();
	}

	public void clear() {
		mapCharset.clear();
	}
	
	private String createTable(SQLCharset object) {
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
		
		for (String schema : mapCharset.keySet()) {
			String s1 = element("schema", schema);
			SQLCharset charset = mapCharset.get(schema);
			String s2 = element("object", createTable(charset));

			String s6 = element("charset", s1 + s2);
			buff.append(s6);
		}
		String body = element("app", buff.toString());
		return toUTF8(Effect.xmlHead + body);
	}

	private SQLCharset resolveCharset(String s) {
		byte[] b = Base64.decode(s.getBytes());
		try {
			ByteArrayInputStream in = new ByteArrayInputStream(b);
			ObjectInputStream input = new ObjectInputStream(in);
			SQLCharset table = (SQLCharset) input.readObject();
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
		if (doc == null) {
			return false;
		}

		NodeList list = doc.getElementsByTagName("charset");
		int len = list.getLength();
		for (int i = 0; i < len; i++) {
			Element elem = (Element) list.item(i);
			String schema = xml.getXMLValue(elem.getElementsByTagName("schema"));
			String object = xml.getXMLValue(elem.getElementsByTagName("object"));
			SQLCharset instance = this.resolveCharset(object);
			this.add(schema, instance);
		}
		return true;
	}

}