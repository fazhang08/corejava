/**
 *
 */
package com.lexst.db.account;

import java.util.*;
import java.io.Serializable;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.lexst.db.schema.Space;
import com.lexst.xml.XML;
import com.lexst.xml.XMLocal;

/**
 *
 * user account, contains a table or any table
 */
public class Account implements Serializable {

	private static final long serialVersionUID = 1L;

	// account user
	private User user = new User();
	// database name -> space set
	private Map<String, SpaceSet> mapSpace = new TreeMap<String, SpaceSet>();
	// operator option
	private Map<Integer, Permit> mapPermit = new TreeMap<Integer, Permit>();

	/**
	 *
	 */
	public Account() {
		super();
	}

	/**
	 * @param user
	 */
	public Account(User user) {
		this();
		this.setUser(user);
	}

	/**
	 * set a user
	 * @param s
	 */
	public void setUser(User s) {
		user.set(s);
	}

//	/**
//	 * @param username
//	 * @param password
//	 */
//	public void setUser(String username, String password) {
//		user.setUsername(username);
//		user.setTextPassword(password);
//	}

	/**
	 * @return
	 */
	public User getUser() {
		return user;
	}

	/**
	 * save a database configure
	 * @param db
	 * @return
	 */
	public boolean addSchema(String db) {
		String low = db.toLowerCase();
		SpaceSet set = mapSpace.get(low);
		if (set != null) {
			return false;
		}
		set = new SpaceSet();
		return mapSpace.put(low, set) == null;
	}

	/**
	 * delete a database configure
	 * @param db
	 * @return
	 */
	public boolean deleteSchema(String db) {
		String low = db.toLowerCase();
		SpaceSet set = mapSpace.remove(low);
		return set != null;
	}

	public boolean addSpace(Space space) {
		String low = space.getSchema().toLowerCase();
		SpaceSet set = mapSpace.get(low);
		if (set == null) {
			return false;
		}
		return set.add(space);
	}

	public boolean deleteSpace(Space space) {
		String low = space.getSchema().toLowerCase();
		SpaceSet set = mapSpace.remove(low);
		if (set != null) {
			return set.remove(space);
		}
		return false;
	}

	public Set<String> dbKeys() {
		return mapSpace.keySet();
	}

	public List<Space> findSpaces(String db) {
		ArrayList<Space> a = new ArrayList<Space>();
		String low = db.toLowerCase();
		SpaceSet set = mapSpace.get(low);
		if (set != null) {
			a.addAll(set.list());
		}
		return a;
	}

	/**
	 * check create authorithn
	 * @return
	 */
	public boolean allowCreateUser() {
		for (Permit permit : mapPermit.values()) {
			if (permit.getRank() == Permit.USER_PERMIT) {
				return permit.isAllow(Control.CREATE_USER);
			}
		}
		return false;
	}

	public boolean allowDropUser() {
		for (Permit permit : mapPermit.values()) {
			if (permit.getRank() == Permit.USER_PERMIT) {
				return permit.isAllow(Control.DROP_USER);
			}
		}
		return false;
	}

	public boolean allowGrant() {
		for(Permit permit : mapPermit.values()) {
			if (permit.getRank() == Permit.USER_PERMIT) {
				return permit.isAllow(Control.GRANT);
			}
		}
		return false;
	}

	public boolean allowRevoke() {
		for(Permit permit : mapPermit.values()) {
			if (permit.getRank() == Permit.USER_PERMIT) {
				return permit.isAllow(Control.REVOKE);
			}
		}
		return false;
	}

	public boolean allowCreateSchema() {
		for (Permit permit : mapPermit.values()) {
			if (permit.getRank() == Permit.USER_PERMIT) {
				return permit.isAllow(Control.CREATE_SCHEMA);
			}
		}
		return false;
	}

	public boolean allowDropSchema() {
		for (Permit permit : mapPermit.values()) {
			if (permit.getRank() == Permit.USER_PERMIT) {
				return permit.isAllow(Control.DROP_SCHEMA);
			}
		}
		return false;
	}

	public boolean allowCreateTable(String db) {
		boolean success = false;
		for (Permit permit : mapPermit.values()) {
			if (permit.getRank() == Permit.USER_PERMIT) {
				success = permit.isAllow(Control.CREATE_TABLE);
				if (success) break;
			} else if (permit.getRank() == Permit.SCHEMA_PERMIT) {
				success = ((SchemaPermit) permit).isAllow(db, Control.CREATE_TABLE);
				if (success) break;
			}
		}
		return success;
	}

	public boolean allowDropTable(String db) {
		boolean success = false;
		for(Permit permit : mapPermit.values()) {
			if(permit.getRank() == Permit.USER_PERMIT) {
				success = permit.isAllow(Control.DROP_TABLE);
				if(success) break;
			} else if(permit.getRank() == Permit.SCHEMA_PERMIT) {
				success = ((SchemaPermit) permit).isAllow(db, Control.DROP_TABLE);
				if(success) break;
			}
		}
		return success;
	}

	/**
	 * @param permit
	 * @return
	 */
	public boolean add(Permit permit) {
		int rank = permit.getRank();
		Permit old = mapPermit.get(rank);
		if (old == null) {
			return mapPermit.put(rank, permit) == null;
		}
		return old.add(permit);
	}

	/**
	 * @param permit
	 * @return
	 */
	public boolean remove(Permit permit) {
		int rank = permit.getRank();
		Permit old = mapPermit.get(rank);
		if (old != null) {
			old.remove(permit);
			if (old.isEmpty()) {
				mapPermit.remove(rank);
			}
			return true;
		}
		return false;
	}

	/**
	 * all permit
	 * @return
	 */
	public Collection<Permit> list() {
		return mapPermit.values();
	}

	public boolean isEmpty() {
		return mapPermit.isEmpty();
	}

	public int size() {
		return mapPermit.size();
	}

	/**
	 * change to xml string
	 * @return
	 */
	public String buildXML() {
		StringBuilder buff = new StringBuilder(512);
		buff.append(XML.cdata_element("username", user.getHexUsername()));
		buff.append(XML.cdata_element("password", user.getHexPassword()));
		buff.append(XML.cdata_element("maxsize", user.getMaxSize()));

		for (String db : mapSpace.keySet()) {
			SpaceSet set = mapSpace.get(db);

			StringBuilder a = new StringBuilder(512);
			for (Space space : set.list()) {
				a.append(XML.cdata_element("table", space.getTable()));
			}
			StringBuilder b = new StringBuilder(512);
			b.append(XML.cdata_element("db", db));
			b.append(XML.element("all", a.toString()));

			buff.append(XML.element("schema", b.toString()));
		}
		// all permit
		for(Permit permit : mapPermit.values()) {
			String s = permit.buildXML();
			buff.append(s);
		}
		return XML.element("account", buff.toString());
	}

	/**
	 * parse xml docment
	 * @param xml
	 * @param elem
	 * @return
	 */
	public boolean parseXML(XMLocal xml, Element elem) {
		String s = xml.getXMLValue(elem.getElementsByTagName("username"));
		user.setHexUsername(s);
		s = xml.getXMLValue(elem.getElementsByTagName("password"));
		user.setHexPassword(s);
		s = xml.getXMLValue(elem.getElementsByTagName("maxsize"));
		user.setMaxSize(Long.parseLong(s));

		NodeList list = elem.getElementsByTagName("schema");
		int len = list.getLength();
		for (int i = 0; i < len; i++) {
			Element element = (Element) list.item(i);
			String dbname = xml.getXMLValue(element.getElementsByTagName("db"));
			String[] tables = xml.getXMLValues(element.getElementsByTagName("table"));
			this.addSchema(dbname);
			for (int j = 0; tables != null && j < tables.length; j++) {
				this.addSpace(new Space(dbname, tables[j]));
			}
		}

		list = elem.getElementsByTagName("permit");
		// not found , return true;
		if (list == null) return true;
		len = list.getLength();
		for (int i = 0; i < len; i++) {
			Element element = (Element) list.item(i);
			s = xml.getXMLValue(element.getElementsByTagName("rank"));
			int rank = Integer.parseInt(s);
			Permit permit = null;
			switch(rank) {
			case Permit.TABLE_PERMIT:
				permit = new TablePermit();
				break;
			case Permit.SCHEMA_PERMIT:
				permit = new SchemaPermit();
				break;
			case Permit.USER_PERMIT:
				permit = new UserPermit();
				break;
			default:
				return false;
			}
			boolean success = permit.parseXML(element);
			if (success) {
				mapPermit.put(permit.getRank(), permit);
			}
		}
		return true;
	}

}