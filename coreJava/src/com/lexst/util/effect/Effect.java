/**
 * 
 */
package com.lexst.util.effect;


import com.lexst.util.lock.*;
import com.lexst.xml.*;

public class Effect  {

	protected static String xmlHead = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
	
	// lock handle
	private MutexLock lock = new MutexLock();

	/**
	 * 
	 */
	public Effect() {
		super();
	}

	protected boolean lockSingle() {
		return lock.lockSingle();
	}

	protected boolean unlockSingle() {
		return lock.unlockSingle();
	}

	protected boolean lockMulti() {
		return lock.lockMulti();
	}

	protected boolean unlockMulti() {
		return lock.unlockMulti();
	}
	
	protected String element(String key, String value) {
		return XML.element(key, value);
	}
	
	protected String cdata_element(String key, String value) {
		return XML.cdata_element(key, value);
	}

	protected String element(String key, short value) {
		return XML.element(key, String.valueOf(value));
	}

	protected String element(String key, int value) {
		return XML.element(key, String.valueOf(value));
	}

	
	protected String element(String key, long value) {
		return XML.element(key, String.valueOf(value));
	}
	
	protected String element(String key, float value) {
		return XML.element(key, String.valueOf(value));
	}

	protected String element(String key, double value) {
		return XML.element(key, String.valueOf(value));
	}


	protected String cdata_element(String key, long value) {
		return XML.cdata_element(key, String.valueOf(value));
	}

	protected byte[] toUTF8(String text) {
		return XML.toUTF8(text);
	}

}
