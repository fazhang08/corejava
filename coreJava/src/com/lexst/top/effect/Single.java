/**
 *
 */
package com.lexst.top.effect;

import org.w3c.dom.*;

import com.lexst.util.effect.*;
import com.lexst.xml.*;

public final class Single extends Effect {

	public final static String filename = "chunkid.xml";

	// min value is 1, 0 is invalid
	private long scale = Long.MIN_VALUE;

	/**
	 *
	 */
	public Single() {
		super();
		this.setBegin(Long.MIN_VALUE);
	}

	public void setBegin(long i) {
		this.scale = i;
	}

	public long getBegin() {
		return this.scale;
	}

	/**
	 * 生成字节流
	 *
	 * @return
	 */
	public byte[] toBytes() {
		return null;
	}

	/**
	 * flush chunk identity
	 * @param num
	 * @return
	 */
	public long[] pull(int num) {
		long[] values = new long[num];
		super.lockSingle();
		try {
			for (int i = 0; i < values.length; i++) {
				values[i] = scale++;
				if (scale == 0L) {
					scale++;
				}
			}
		} catch (Throwable exp) {

		} finally {
			super.unlockSingle();
		}
		return values;
	}

	/**
	 * @return
	 */
	public byte[] buildXML() {
		String s = element("chunkid", scale);
		String text = element("application", s);
		return toUTF8(Effect.xmlHead + text);
	}

	public boolean parseXML(byte[] bytes) {
		XMLocal xml = new XMLocal();
		Document doc = xml.loadXMLSource(bytes);
		if(doc == null) {
			return false;
		}

		String s = xml.getXMLValue(doc.getElementsByTagName("chunkid"));
		scale = Long.parseLong(s);
		return true;
	}

}