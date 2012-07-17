/**
 *
 */
package com.lexst.db.account;

import java.io.Serializable;
import java.util.*;

public class Control implements Serializable {
	private static final long serialVersionUID = 1L;

	// table control option
	public final static int SELECT = 1;
	public final static int INSERT = 2;
	public final static int DELETE = 3;
	public final static int UPDATE = 4;
	public final static int DC = 5;
	public final static int ADC = 6;

	// database control option
	public final static int CREATE_TABLE = 10;
	public final static int DROP_TABLE = 11;

	// user control option
	public final static int GRANT = 20;
	public final static int REVOKE = 21;

	public final static int CREATE_SCHEMA = 22;
	public final static int DROP_SCHEMA = 23;

	public final static int CREATE_USER = 24;
	public final static int DROP_USER = 25;
	public final static int ALTER_USER = 26;

	public final static int DBA = 27;

	// common option
	public final static int ALL = 30;

	private final static int[] DBA_OPTIONS = { Control.DBA,
			Control.CREATE_USER, Control.DROP_USER, Control.ALTER_USER,
			Control.GRANT, Control.REVOKE, Control.CREATE_SCHEMA, Control.DROP_SCHEMA,
			Control.CREATE_TABLE, Control.DROP_TABLE, Control.SELECT,
			Control.DELETE, Control.INSERT, Control.UPDATE , Control.DC, Control.ADC};

	private final static int[] TABLE_OPTIONS = new int[] { Control.SELECT,
			Control.DELETE, Control.INSERT, Control.UPDATE, Control.DC, Control.ADC };

	private final static int[] SCHEMA_OPTIONS = new int[] { Control.CREATE_TABLE,
			Control.DROP_TABLE };

	private final static int[] USER_OPTIONS = { Control.CREATE_USER,
			Control.DROP_USER, Control.ALTER_USER, Control.GRANT,
			Control.REVOKE, Control.CREATE_SCHEMA, Control.DROP_SCHEMA,
			Control.CREATE_TABLE, Control.DROP_TABLE, Control.INSERT,
			Control.DELETE, Control.UPDATE, Control.SELECT, Control.DC, Control.ADC };

	// operate identity
	private Set<Integer> set = new TreeSet<Integer>();

	/**
	 *
	 */
	public Control() {
		super();
	}

	public boolean isAllow(int id) {
		return set.contains(new Integer(id));
	}

	/**
	 * set options
	 * @param level
	 * @param actives
	 * @return
	 */
	public boolean set(int level, int[] actives) {
		int index = 0;

		if(level == Permit.TABLE_PERMIT ) {
			// check actives
			boolean all = false;
			for (int active : actives) {
				for (index = 0; index < TABLE_OPTIONS.length; index++) {
					if (TABLE_OPTIONS[index] == active) break;
				}
				if(!all && active == Control.ALL) {
					all = true;
				} else if(index == TABLE_OPTIONS.length) {
					return false;
				}
			}
			if (all) {
				this.set(TABLE_OPTIONS);
			} else {
				this.set(actives);
			}
			return true;
		} else if(level == Permit.SCHEMA_PERMIT ) {
			boolean all = false;
			for(int active : actives) {
				for (index = 0; index < SCHEMA_OPTIONS.length; index++) {
					if (SCHEMA_OPTIONS[index] == active) break;
				}
				if(!all && active == Control.ALL) {
					all = true;
				} else if(index == SCHEMA_OPTIONS.length) {
					return false;
				}
			}
			if(all) {
				set(SCHEMA_OPTIONS);
			} else {
				set(actives);
			}
			return true;
		} else if(level == Permit.USER_PERMIT) {
			boolean dba = false, all = false;
			for(int active : actives) {
				for (index = 0; index < USER_OPTIONS.length; index++) {
					if(USER_OPTIONS[index] == active) break;
				}
				if(!all && active == Control.ALL) {
					all = true;
				} else if(!dba && active == Control.DBA) {
					dba = true;
				} else if(index == USER_OPTIONS.length) {
					return false;
				}
			}
			if(dba) {
				set(Control.DBA_OPTIONS);
			} else if(all) {
				set(Control.USER_OPTIONS);
			} else {
				set(actives);
			}
			return true;
		}
		return false;
	}


	private void set(int[] actives) {
		for(int id : actives) {
			set.add(id);
		}
	}

	public boolean add(int id) {
		return set.add(new Integer(id));
	}

	public boolean add(Collection<Integer> list) {
		return set.addAll(list);
	}

	public boolean add(Control ctrl) {
		return set.addAll(ctrl.set);
	}

	public boolean delete(int id) {
		return set.remove(new Integer(id));
	}

	public boolean delete(Collection<Integer> list) {
		return set.removeAll(list);
	}

	public boolean delete(Control ctrl) {
		return set.removeAll(ctrl.set);
	}

	public Collection<Integer> list() {
		return set;
	}

	public boolean isEmpty() {
		return set.isEmpty();
	}

	public int size() {
		return set.size();
	}

	public static String translate(int id) {
		String s = null;
		switch (id) {
		case Control.GRANT:
			s = "GRANT"; break;
		case Control.REVOKE:
			s = "REVOKE"; break;
		case Control.CREATE_USER:
			s = "CREATE USER";	break;
		case Control.DROP_USER:
			s = "DROP USER"; break;
		case Control.ALTER_USER:
			s = "ALTER USER";  break;
		case Control.CREATE_SCHEMA:
			s = "CREATE DATABASE"; break;
		case Control.DROP_SCHEMA:
			s = "DROP DATABASE"; break;
		case Control.CREATE_TABLE:
			s = "CREATE TABLE"; break;
		case Control.DROP_TABLE:
			s = "DROP TABLE"; break;
		case Control.SELECT:
			s = "SELECT"; break;
		case Control.INSERT:
			s = "INSERT"; break;
		case Control.DELETE:
			s = "DELETE"; break;
		case Control.UPDATE:
			s = "UPDATE"; break;
		case Control.DC:
			s = "DC"; break;
		case Control.ADC:
			s = "ADC"; break;
		case Control.ALL:
			s = "ALL"; break;
		case Control.DBA:
			s = "DBA"; break;
		}
		return s;
	}

	public static int translate(String s) {
		int id = -1;
		if ("CREATE USER".equalsIgnoreCase(s)) {
			id = Control.CREATE_USER;
		} else if ("DROP USER".equalsIgnoreCase(s)) {
			id = Control.DROP_USER;
		} else if("ALTER USER".equalsIgnoreCase(s)) {
			id = Control.ALTER_USER;
		} else if ("GRANT".equalsIgnoreCase(s)) {
			id = Control.GRANT;
		} else if ("REVOKE".equalsIgnoreCase(s)) {
			id = Control.REVOKE;

		} else if ("CREATE DATABASE".equalsIgnoreCase(s)) {
			id = Control.CREATE_SCHEMA;
		} else if("DROP DATABASE".equalsIgnoreCase(s)) {
			id = Control.DROP_SCHEMA;
		} else if ("CREATE TABLE".equalsIgnoreCase(s)) {
			id = Control.CREATE_TABLE;
		} else if("DROP TABLE".equalsIgnoreCase(s)) {
			id = Control.DROP_TABLE;

		}else if ("SELECT".equalsIgnoreCase(s)) {
			id = Control.SELECT;
		} else if ("INSERT".equalsIgnoreCase(s)) {
			id = Control.INSERT;
		} else if ("DELETE".equalsIgnoreCase(s)) {
			id = Control.DELETE;
		} else if ("UPDATE".equalsIgnoreCase(s)) {
			id = Control.UPDATE;
		} else if ("DC".equalsIgnoreCase(s)) {
			id = Control.DC;
		} else if ("ADC".equalsIgnoreCase(s)) {
			id = Control.ADC;
		} else if ("ALL".equalsIgnoreCase(s)) {
			id = Control.ALL;
		} else if ("DBA".equalsIgnoreCase(s)) {
			id = Control.DBA;
		}
		return id;
	}

}