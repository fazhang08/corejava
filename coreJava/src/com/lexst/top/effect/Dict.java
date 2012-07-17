/**
 *
 */
package com.lexst.top.effect;

import java.util.*;

import org.w3c.dom.*;

import com.lexst.db.schema.*;
import com.lexst.log.client.*;
import com.lexst.util.effect.*;
import com.lexst.xml.*;

/**
 *
 * database dictionary
 */
public class Dict extends Effect {

	public final static String filename = "dict.xml";

	// database name (lower case) -> database instance
	private Map<String, Schema> mapSchema = new TreeMap<String, Schema>();

	/**
	 *
	 */
	public Dict() {
		super();
	}

	public boolean exists(Space space) {
		super.lockMulti();
		try {
			String db = space.getSchema().toLowerCase();
			Schema schema = mapSchema.get(db);
			if (schema == null) {
				return false;
			}
			return schema.exists(space);
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			super.unlockMulti();
		}
		return false;
	}

	public Set<String> keys() {
		super.lockSingle();
		try {
			TreeSet<String> set = new TreeSet<String>(mapSchema.keySet());
			return set;
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			super.unlockSingle();
		}
		return null;
	}

	public Schema findSchema(String name) {
		super.lockMulti();
		try {
			String s = name.toLowerCase();
			return mapSchema.get(s);
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			super.unlockMulti();
		}
		return null;
	}

	public Table findTable(Space space) {
		super.lockMulti();
		try {
			String db = space.getSchema().toLowerCase();
			Schema base = mapSchema.get(db);
			if (base == null) {
				return null;
			}
			return base.findTable(space);
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			super.unlockMulti();
		}
		return null;
	}
	
	/**
	 * find chunk size
	 * @param space
	 * @return
	 */
	public int findChunkSize(Space space) {
		int size = -1;
		super.lockMulti();
		try {
			String db = space.getSchema().toLowerCase();
			Schema base = mapSchema.get(db);
			if(base != null) {
				size = base.findChunkSize(space);
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockMulti();
		}
		return size;
	}

	/**
	 * set chunk size
	 * @param space
	 * @param size
	 * @return
	 */
	public boolean setChunkSize(Space space, int size) {
		boolean success = false;
		super.lockSingle();
		try {
			String db = space.getSchema().toLowerCase();
			Schema base = mapSchema.get(db);
			if (base != null) {
				success = base.setChunkSize(space, size);
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockSingle();
		}
		return success;
	}
	
	/**
	 * set space trigger
	 * @param space
	 * @param type
	 * @param time
	 * @return
	 */
	public boolean setOptimizeTime(Space space, int type, long time) {
		boolean success = false;
		super.lockSingle();
		try {
			String db = space.getSchema().toLowerCase();
			Schema base = mapSchema.get(db);
			if (base != null) {
				success = base.setOptimizeTime(space, type, time);
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockSingle();
		}
		return success;
	}

	public boolean addSchema(Schema db) {
		super.lockSingle();
		try {
			String s = db.getName().toLowerCase();
			if (mapSchema.containsKey(s)) {
				return false;
			}
			return mapSchema.put(s, db) == null;
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			super.unlockSingle();
		}
		return false;
	}

	public Schema deleteSchema(String name) {
		super.lockSingle();
		try {
			String s = name.toLowerCase();
			return mapSchema.remove(s);
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			super.unlockSingle();
		}
		return null;
	}

	public boolean addTable(Space space, Table table) {
		super.lockSingle();
		try {
			String db = space.getSchema().toLowerCase();
			Schema base = mapSchema.get(db);
			// not found database
			if (base == null) {
				return false;
			}
			return base.add(space, table);
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			super.unlockSingle();
		}
		return false;
	}

	public boolean deleteTable(Space space) {
		super.lockSingle();
		try {
			String db = space.getSchema().toLowerCase();
			Schema base = mapSchema.get(db);
			// not found
			if (base == null) {
				return false;
			}
			return base.remove(space);
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			super.unlockSingle();
		}
		return false;
	}

	public void clear() {
		mapSchema.clear();
	}

	public boolean isEmpty() {
		return mapSchema.isEmpty();
	}

	public int size() {
		return mapSchema.size();
	}

	public byte[] buildXML() {
		StringBuilder buff = new StringBuilder();
		for (Schema db : mapSchema.values()) {
			String s = db.buildXML();
			buff.append(s);
		}
		String body = element("dict", buff.toString());
		return toUTF8(Effect.xmlHead + body);
	}

	public boolean parseXML(byte[] bytes) {
		XMLocal xml = new XMLocal();
		Document doc = xml.loadXMLSource(bytes);
		if(doc == null) {
			return false;
		}

		NodeList list = doc.getElementsByTagName("database");
		int size = list.getLength();
		for (int i = 0; i < size; i++) {
			Schema base = new Schema();
			Element elem = (Element) list.item(i);
			boolean success = base.parseXML(xml, elem);
			if (!success) return false;
			String s = base.getName().toLowerCase();
			this.mapSchema.put(s, base);
		}
		return true;
	}

}