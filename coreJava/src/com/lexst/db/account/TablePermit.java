/**
 *
 */
package com.lexst.db.account;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.*;

import com.lexst.db.schema.Space;
import com.lexst.xml.XML;
import com.lexst.xml.XMLocal;


public class TablePermit extends Permit {
	private static final long serialVersionUID = 1L;

	private Map<Space, Control> mapCtrl = new TreeMap<Space, Control>();

	/**
	 *
	 */
	public TablePermit() {
		super(Permit.TABLE_PERMIT);
	}

	public boolean isEmpty() {
		return mapCtrl.isEmpty();
	}

	public int size() {
		return mapCtrl.size();
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.db.account.Permit#isAllow(int)
	 */
	public boolean isAllow(int id) {
		for (Control ctrl : mapCtrl.values()) {
			if (ctrl.isAllow(id)) return true;
		}
		return false;
	}

	/**
	 * @param space
	 * @param ctrl
	 * @return
	 */
	public boolean add(Space space, Control ctrl) {
		Control old = mapCtrl.get(space);
		if(old ==null) {
			mapCtrl.put(space, ctrl);
		} else {
			old.add(ctrl);
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.lexst.db.account.Permit#add(com.lexst.db.account.Permit)
	 */
	@Override
	public boolean add(Permit other) {
		if( other.getClass() != TablePermit.class) {
			return false;
		}
		TablePermit permit = (TablePermit)other;
		for(Space space : permit.mapCtrl.keySet()) {
			Control ctrl = permit.mapCtrl.get(space);
			Control old = mapCtrl.get(space);
			if(old == null) {
				mapCtrl.put(space, ctrl);
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
	public boolean remove(Permit permit) {
		if( permit.getClass() != TablePermit.class) {
			return false;
		}
		TablePermit oth = (TablePermit)permit;
		for (Space space : oth.mapCtrl.keySet()) {
			Control ctrl = oth.mapCtrl.get(space);
			Control old = mapCtrl.get(space);
			if (old != null) {
				old.delete(ctrl);
				if (old.isEmpty()) mapCtrl.remove(space);
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

		for (Space space : mapCtrl.keySet()) {
			Control ctrl = mapCtrl.get(space);
			StringBuilder a = new StringBuilder(128);
			a.append(XML.cdata_element("db", space.getSchema()));
			a.append(XML.cdata_element("table", space.getTable()));
			for (int active : ctrl.list()) {
				String s = Control.translate(active);
				a.append(XML.cdata_element("ctrl", s));
			}
			b.append(XML.element("set", a.toString()));
		}
		return XML.element("permit", b.toString());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.lexst.db.account.Permit#parseXML(org.w3c.dom.Element)
	 */
	@Override
	public boolean parseXML(Element element) {
		XMLocal xml = new XMLocal();

		String level = xml.getXMLValue(element.getElementsByTagName("rank"));
		rank = Integer.parseInt(level);
		if (rank != Permit.TABLE_PERMIT) {
			return false;
		}

		NodeList list = element.getElementsByTagName("set");
		int len = list.getLength();
		for (int i = 0; i < len; i++) {
			Element elem = (Element) list.item(i);
			String db = xml.getXMLValue(elem.getElementsByTagName("db"));
			String table = xml.getXMLValue(elem.getElementsByTagName("table"));

			String[] all = xml.getXMLValues(elem.getElementsByTagName("ctrl"));
			Control ctrl = new Control();
			for (String s : all) {
				int id = Control.translate(s);
				if (id != -1) ctrl.add(id);
			}
			mapCtrl.put(new Space(db, table), ctrl);
		}
		return true;
	}

}