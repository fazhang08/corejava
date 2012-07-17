/**
 *
 */
package com.lexst.home.effect;

import java.util.*;

import org.w3c.dom.*;

import com.lexst.util.effect.*;

public class IdentitySet extends Effect { 

	public final static String filename = "chunkid.xml";

	// chunkid set
	private LinkedList<Long> array = new LinkedList<Long>();
	
	/**
	 *
	 */
	public IdentitySet() {
		super();
	}
	
	public boolean isEmpty() {
		return array.isEmpty();
	}
	
	public int size() {
		return array.size();
	}
	
	public void clear() {
		array.clear();
	}

	/**
	 * poll a chunk identity
	 * @return
	 */
	public long poll() {
		super.lockSingle();
		try {
			if (!array.isEmpty()) {
				return array.poll();
			}
		} catch (Throwable exp) {

		} finally {
			super.unlockSingle();
		}
		return 0L;
	}
	
	public boolean add(long chunkId) {
		super.lockSingle();
		try {
			if (chunkId != 0 && !array.contains(chunkId)) {
				array.add(chunkId);
				return true;
			}
		} catch (Throwable exp) {

		} finally {
			super.unlockSingle();
		}
		return false;
	}

	public int add(long[] chunkIds) {
		int count = 0;
		for (int i = 0; i < chunkIds.length; i++) {
			if (array.add(chunkIds[i])) {
				count++;
			}
		}
		return count;
	}
	
	public long[] apply(int num) {
		if(num < 1) return null;
		super.lockSingle();
		try {
			if(array.isEmpty()) return null;
			int size = (array.size() > num ? num : array.size());
			long[] values = new long[size];
			for (int i = 0; i < size; i++) {
				values[i] = array.poll();
			}
			return values;
		} catch (Throwable exp) {

		} finally {
			super.unlockSingle();
		}
		return null;
	}

	public byte[] createXML() {
		StringBuilder buff = new StringBuilder(10240);
		super.lockSingle();
		try {
			for (long chunkid : array) {
				String s = element("chunkid", chunkid);
				buff.append(s);
			}
		} catch (Throwable exp) {

		} finally {
			super.unlockSingle();
		}
		String body = element("app", buff.toString());
		return toUTF8(Effect.xmlHead + body);
	}

	public byte[] buildXML() {
		StringBuilder buff = new StringBuilder(10240);
		super.lockSingle();
		try {
			while (!array.isEmpty()) {
				long chunkId = array.poll();
				String s = element("chunkid", chunkId);
				buff.append(s);
			}
		} catch (Throwable exp) {

		} finally {
			super.unlockSingle();
		}
		String body = element("app", buff.toString());
		return toUTF8(Effect.xmlHead + body);
	}

	public boolean parseXML(byte[] bytes) {
		com.lexst.xml.XMLocal xml = new com.lexst.xml.XMLocal();
		Document doc = xml.loadXMLSource(bytes);
		if(doc == null) {
			return false;
		}
		
		NodeList list =	doc.getElementsByTagName("chunkid");
		int len = list.getLength();
		for(int i = 0; i <len; i++) {
			Element elem = (Element) list.item(i);
			String s = elem.getTextContent();
			long chunkId = Long.parseLong(s);
			array.add(chunkId);
		}
		return true;
	}
	
}