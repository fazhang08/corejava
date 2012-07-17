/**
 *
 */
package com.lexst.db.account;

import java.util.*;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.lexst.xml.XML;
import com.lexst.xml.XMLocal;

public class UserPermit extends Permit {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/* username -> control */
	private Map<String, Control> mapCtrl = new TreeMap<String, Control>();

	/**
	 *
	 */
	public UserPermit() {
		super(Permit.USER_PERMIT);
	}

	public void add(String user, Control ctrl) {
		mapCtrl.put(user.toLowerCase(), ctrl);
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

	public Collection<Control> list() {
		return mapCtrl.values();
	}

	public int size() {
		return mapCtrl.size();
	}

	/**
	 *
	 */
	public boolean isAllow(int id) {
		for (Control ctrl : mapCtrl.values()) {
			if (ctrl.isAllow(id)) return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see com.lexst.db.account.Permit#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return mapCtrl.isEmpty();
	}

	/* (non-Javadoc)
	 * @see com.lexst.db.account.Permit#add(com.lexst.db.account.Permit)
	 */
	@Override
	public boolean add(Permit other) {
		if (other.getClass() != UserPermit.class) {
			return false;
		}
		UserPermit permit = (UserPermit) other;
		for (String username : permit.mapCtrl.keySet()) {
			Control ctrl = permit.mapCtrl.get(username);
			Control old = mapCtrl.get(username);
			if (old == null) {
				mapCtrl.put(username, ctrl);
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
		if (other.getClass() != UserPermit.class) {
			return false;
		}
		UserPermit permit = (UserPermit) other;
		for (String username : permit.mapCtrl.keySet()) {
			Control ctrl = permit.mapCtrl.get(username);
			Control old = mapCtrl.get(username);
			if (old != null) {
				old.delete(ctrl);
				if (old.isEmpty()) mapCtrl.remove(username);
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

		for (String username : mapCtrl.keySet()) {
			Control ctrl = mapCtrl.get(username);
			StringBuilder a = new StringBuilder(128);
			a.append(XML.cdata_element("username", username));
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
		if (rank != Permit.USER_PERMIT) {
			return false;
		}
		NodeList list = element.getElementsByTagName("set");
		int len = list.getLength();
		for (int i = 0; i < len; i++) {
			Element elem = (Element) list.item(i);
			String username = xml.getXMLValue(elem.getElementsByTagName("username"));

			String[] all = xml.getXMLValues(elem.getElementsByTagName("ctrl"));
			Control ctrl = new Control();
			for (String s : all) {
				int id = Control.translate(s);
				if (id != -1) ctrl.add(id);
			}
			mapCtrl.put(username, ctrl);
		}
		return true;
	}
}