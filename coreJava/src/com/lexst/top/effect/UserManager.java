/**
 *
 */
package com.lexst.top.effect;

import java.util.*;

import org.w3c.dom.*;

import com.lexst.db.account.*;
import com.lexst.util.effect.*;
import com.lexst.xml.*;

public class UserManager extends Effect {

	public final static String filename = "account.xml";

	/* super user */
	private Administrator dba = new Administrator();

	/* normal account (username -> account) */
	private Map<String, Account> mapUser = new TreeMap<String, Account>();
	
	/**
	 *
	 */
	public UserManager() {
		super();
	}

	/**
	 * @param dba
	 */
	public UserManager(Administrator dba) {
		this();
		this.setDBA(dba);
	}

	public void setDBA(Administrator admin) {
		dba.set(admin);
	}

	public boolean isDBA(User user) {
		return dba.isMatchUsername(user.getUsername())
				&& dba.isMatchPassword(user.getPassword());
	}

	public Administrator getDBA() {
		return this.dba;
	}

//	public boolean allow(User user, int auth) {
//		// 如果是DBA用户,允许执行所有操作
//		if (isDBA(user)) {
//			return true;
//		}
//		String s = user.getUsername().toLowerCase();
//		Account account = mapUser.get(s);
//		if (account == null) {
//			return account.allow(auth);
//		}
//		return false;
//	}

	public boolean exists(Collection<String> users) {
		for (String s : users) {
			if (mapUser.get(s.toLowerCase()) == null) {
				return false;
			}
		}
		return true;
	}

	public Account findAccount(String name) {
		String s = name.toLowerCase();
		return mapUser.get(s);
	}

	public boolean addAccount(Account account) {
		User user = account.getUser();
//		String username = user.getUsername().toLowerCase();
		String username = user.getHexUsername().toLowerCase();
		// save account
		Account old = mapUser.get(username);
		if (old == null) {
			mapUser.put(username, account);
		} else {
			for (Permit auth : account.list()) {
				old.add(auth);
			}
		}
		return true;
	}

	public boolean deleteAccount(String username) {
		String s = username.toLowerCase();
		return mapUser.remove(s) != null;
	}

	public boolean deleteAccount(Account account) {
		User user = account.getUser();
//		String s = u.getUsername().toLowerCase();
		String s = user.getHexUsername().toLowerCase();
		Account org = mapUser.get(s);
		if(org == null) {
			return false;
		}
		for(Permit auth : account.list()) {
			org.remove(auth);
		}
		return true;
	}

	/**
	 * add a user
	 * @param user
	 * @return
	 */
	public boolean create(User user) {
//		String username = user.getUsername().toLowerCase();
		String username = user.getHexUsername().toLowerCase();
		Account account = mapUser.get(username);
		if (account != null) {
			return false; // user existed, error!
		}
		account = new Account(user);
		return mapUser.put(username, account) == null;
	}

	/**
	 * delete a user
	 * @param username
	 * @return
	 */
	public boolean drop(String username) {
		String s = username.toLowerCase();
		return mapUser.remove(s) != null;
	}

	public void clear() {
		mapUser.clear();
	}

	public boolean isEmpty() {
		return mapUser.isEmpty();
	}

	public int size() {
		return mapUser.size();
	}

	public byte[] buildXML() {
		StringBuilder buff = new StringBuilder(10240);
		for (Account account : mapUser.values()) {
			String text = account.buildXML();
			buff.append(text);
		}
		String body = XML.element("application", buff.toString());
		return toUTF8(Effect.xmlHead + body);
	}

	public boolean parseXML(byte[] bytes) {
		XMLocal xml = new XMLocal();
		Document doc = xml.loadXMLSource(bytes);
		if(doc == null) {
			return false;
		}

		NodeList list = doc.getElementsByTagName("account");
		int size = list.getLength();
		for (int i = 0; i < size; i++) {
			Account account = new Account();
			Element elem = (Element) list.item(i);
			boolean success = account.parseXML(xml, elem);
			if(!success) return false;
//			String s = account.getUser().getUsername().toLowerCase();
			String s = account.getUser().getHexUsername().toLowerCase();
			mapUser.put(s, account);
		}
		return true;
	}

}