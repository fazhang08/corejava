/**
 *
 */
package com.lexst.db.account;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.lexst.xml.XML;
import com.lexst.xml.XMLocal;

import java.util.*;


public class SchemaPermit extends Permit {

	private static final long serialVersionUID = 1L;

	/* database name -> control */
	private Map<String, Control> mapCtrl = new TreeMap<String, Control>();

	/**
	 *
	 */
	public SchemaPermit() {
		super(Permit.SCHEMA_PERMIT);
	}

	public void add(String db, Control ctrl) {
		mapCtrl.put(db.toLowerCase(), ctrl);
	}

	public boolean remove(String db) {
		return mapCtrl.remove(db.toLowerCase()) != null;
	}

	public Set<String> keys() {
		return mapCtrl.keySet();
	}

	public Control find(String s) {
		return mapCtrl.get(s.toLowerCase());
	}

	public java.util.Collection<Control> list() {
		return mapCtrl.values();
	}

	public int size() {
		return mapCtrl.size();
	}

	public boolean isEmpty() {
		return mapCtrl.isEmpty();
	}

	public boolean isAllow(int id) {
		return false;
	}

	public boolean isAllow(String db, int id) {
		Control ctrl =	mapCtrl.get(db.toLowerCase());
		if(ctrl != null) {
			return ctrl.isAllow(id);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.lexst.db.account.Permit#add(com.lexst.db.account.Permit)
	 */
	@Override
	public boolean add(Permit other) {
		if( other.getClass() != SchemaPermit.class) {
			return false;
		}
		SchemaPermit permit = (SchemaPermit)other;
		for(String db : permit.mapCtrl.keySet()) {
			Control ctrl = permit.mapCtrl.get(db);
			Control old = mapCtrl.get(db);
			if(old == null) {
				mapCtrl.put(db, ctrl);
			} else {
				old.add(ctrl);
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.lexst.db.account.Permit#remove(com.lexst.db.account.Permit)
	 */
	@Override
	public boolean remove(Permit other) {
		if (other.getClass() != SchemaPermit.class) {
			return false;
		}
		SchemaPermit permit = (SchemaPermit) other;
		for (String db : permit.mapCtrl.keySet()) {
			Control ctrl = permit.mapCtrl.get(db);
			Control old = mapCtrl.get(db);
			if (old != null) {
				old.delete(ctrl);
				if (old.isEmpty()) mapCtrl.remove(db);
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.lexst.db.account.Permit#buildXML()
	 */
	@Override
	public String buildXML() {
		StringBuilder b = new StringBuilder(512);

		b.append(XML.element("rank", super.rank));

		for (String db : mapCtrl.keySet()) {
			Control ctrl = mapCtrl.get(db);
			StringBuilder a = new StringBuilder(128);
			a.append(XML.cdata_element("db", db));
			for (int active : ctrl.list()) {
				String s = Control.translate(active);
				a.append(XML.cdata_element("ctrl", s));
			}
			b.append(XML.element("set", a.toString()));
		}
		return XML.element("permit", b.toString());
	}

	/* (non-Javadoc)
	 * @see com.lexst.db.account.Permit#parseXML(org.w3c.dom.Element)
	 */
	@Override
	public boolean parseXML(Element element) {
		XMLocal xml = new XMLocal();

		String level = xml.getXMLValue(element.getElementsByTagName("rank"));
		rank = Integer.parseInt(level);
		if (rank != Permit.SCHEMA_PERMIT) {
			return false;
		}

		NodeList list = element.getElementsByTagName("set");
		int len = list.getLength();
		for (int i = 0; i < len; i++) {
			Element elem = (Element) list.item(i);
			String db = xml.getXMLValue(elem.getElementsByTagName("db"));

			String[] all = xml.getXMLValues(elem.getElementsByTagName("ctrl"));
			Control ctrl = new Control();
			for (String s : all) {
				int id = Control.translate(s);
				if (id != -1) ctrl.add(id);
			}
			mapCtrl.put(db, ctrl);
		}
		return true;
	}

}