/**
 *
 */
package com.lexst.data.effect;

import java.util.*;

import org.w3c.dom.*;

import com.lexst.db.schema.*;
import com.lexst.util.effect.*;
import com.lexst.xml.*;


public class SpacePuddle extends Effect {

	public final static String filename = "space.xml";

	// database space
	private List<Space> array;

	/**
	 * default constrctor
	 */
	public SpacePuddle() {
		super();
		array = new ArrayList<Space>(20);
	}

	public boolean add(Space space) {
		super.lockSingle();
		try {
			return array.add(space);
		} catch (Throwable exp) {

		} finally {
			super.unlockSingle();
		}
		return false;
	}

	public boolean exists(Space space) {
		super.lockMulti();
		try {
			return array.contains(space);
		} catch (Throwable exp) {

		} finally {
			super.unlockMulti();
		}
		return false;
	}

	public boolean remove(Space space) {
		boolean success = false;
		super.lockSingle();
		try {
			success = array.remove(space);
		} catch (Throwable exp) {

		} finally {
			super.unlockSingle();
		}
		return success;
	}

	public List<Space> list() {
		List<Space> all = new ArrayList<Space>();
		super.lockSingle();
		try {
			all.addAll(array);
		} catch (Throwable exp) {

		} finally {
			super.unlockSingle();
		}
		return all;
	}

	public boolean isEmpty() {
		return array.isEmpty();
	}

	public int size() {
		return array.size();
	}

	public byte[] buildXML() {
		StringBuilder buff = new StringBuilder();
		for (Space space : list()) {
			String db = cdata_element("db", space.getSchema());
			String table = cdata_element("table", space.getTable());
			String s = element("space", db + table);
			buff.append(s);
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
			add(new Space(db, table));
		}
		return true;
	}

}