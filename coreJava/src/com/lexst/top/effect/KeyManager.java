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
 * pid pool
 */
public class KeyManager extends Effect {

	public final static String filename = "pid.xml";

	// table space -> primary key(short, int, long, float, double)
	private Map<Space, Number> mapKey = new HashMap<Space, Number>();

	/**
	 *
	 */
	public KeyManager() {
		super();
	}
	
	public boolean isEmpty() {
		return mapKey.isEmpty();
	}

	public int size() {
		return mapKey.size();
	}

	public void clear() {
		mapKey.clear();
	}
	
	/**
	 * @param space
	 * @param value
	 * @return
	 */
	public boolean set(Space space, Number value) {
		super.lockSingle();
		try {
			if (mapKey.containsKey(space)) {
				return false;
			}
			return mapKey.put(space, value) == null;
		} catch (Throwable exp) {

		} finally {
			super.unlockSingle();
		}
		return false;
	}

	/**
	 * get keys
	 * @param space
	 * @param count
	 * @return
	 */
	public Number[] pull(Space space, int count) {
		ArrayList<Number> array = new ArrayList<Number>();

		super.lockSingle();
		try {
			if (!mapKey.containsKey(space)) {
				return null;
			}
			Number digit = mapKey.get(space);
			if (digit.getClass() == Short.class) {
				short value = digit.shortValue(); // ((Short) digit).shortValue();
				for (int i = 0; i < count; i++) {
					array.add(new Short(value++));
				}
				mapKey.put(space, new Short(value));
			} else if (digit.getClass() == Integer.class) {
				int value = digit.intValue(); // ((Integer) digit).intValue();
				for (int i = 0; i < count; i++) {
					array.add(new Integer(value++));
				}
				mapKey.put(space, new Integer(value));
			} else if (digit.getClass() == Long.class) {
				long value = digit.longValue(); // ((Long) digit).longValue();
				for (int i = 0; i < count; i++) {
					array.add(new Long(value++));
				}
				mapKey.put(space, new Long(value));
			} else if (digit.getClass() == Float.class) {
				float value = digit.floatValue(); // ((Float) digit).floatValue();
				for (int i = 0; i < count; i++) {
					array.add(new Float(value++));
				}
				mapKey.put(space, new Float(value));
			} else if (digit.getClass() == Double.class) {
				double value = digit.doubleValue(); // ((Double) digit).doubleValue();
				for (int i = 0; i < count; i++) {
					array.add(new Double(value++));
				}
				mapKey.put(space, new Double(value));
			}
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockSingle();
		}
		
//		Logger.info("Apply pid by '%s', size %d", space, array.size());
		
		Number[] nums = new Number[array.size()];
		return array.toArray(nums);
	}
	
	private String buildType(Number num) {
		String type = "";
		if (num.getClass() == Short.class) type = "SHORT";
		else if (num.getClass() == Integer.class) type = "INT";
		else if (num.getClass() == Long.class) type = "LONG";
		else if (num.getClass() == Float.class) type = "FLOAT";
		else if (num.getClass() == Double.class) type = "DOUBLE";
		return element("type", type);
	}
	
	private String buildValue(Number num) {
		String value = "";
		if (num.getClass() == Short.class) value = String.valueOf(num.shortValue());
		else if (num.getClass() == Integer.class) value = String.valueOf(num.intValue());
		else if (num.getClass() == Long.class) value = String.valueOf(num.longValue());
		else if (num.getClass() == Float.class) value = String.valueOf(num.floatValue());
		else if (num.getClass() == Double.class) value = String.valueOf(num.doubleValue());
		return element("value", value);
	}

	/**
	 * build to 
	 * @return
	 */
	public byte[] buildXML() {
		StringBuilder buff = new StringBuilder(10240);
		for (Space space : mapKey.keySet()) {
			String s1 = element("db", space.getSchema());
			String s2 = element("table", space.getTable());
			String s3 = element("space", s1 + s2);
			Number num = mapKey.get(space);
			String type = buildType(num);
			String value = buildValue(num);
			String key = element("key", s3 + type + value);
			buff.append(key);
		}
		String body = XML.element("application", buff.toString());
		return toUTF8(Effect.xmlHead + body);
	}
	
	/**
	 * parse data
	 * @param bytes
	 * @return
	 */
	public boolean parseXML(byte[] bytes) {
		XMLocal xml = new XMLocal();
		Document doc = xml.loadXMLSource(bytes);
		if(doc == null) {
			return false;
		}
		NodeList list = doc.getElementsByTagName("key");
		int len = list.getLength();
		for (int i = 0; i < len; i++) {
			Element elem = (Element) list.item(i);
			String type = xml.getValue(elem, "type");
			String value = xml.getValue(elem, "value");
			String db = xml.getValue(elem, "db");
			String table = xml.getValue(elem, "table");
			// split value
			Number num = null;
			if ("SHORT".equalsIgnoreCase(type)) num = Short.valueOf(value);
			else if ("INT".equalsIgnoreCase(type)) num = Integer.valueOf(value);
			else if ("LONG".equalsIgnoreCase(type)) num = Long.valueOf(value);
			else if ("FLOAT".equalsIgnoreCase(type)) num = Float.valueOf(value);
			else if ("DOUBLE".equalsIgnoreCase(type)) num = Double.valueOf(value);
			// save
			Space space = new Space(db, table);
			this.set(space, num);
		}
		return true;
	}
	
}