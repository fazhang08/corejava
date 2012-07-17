/**
 *
 */
package com.lexst.site.log;

import java.io.*;

import com.lexst.site.*;


public class LogNode implements Serializable {

	private static final long serialVersionUID = 2324941612313212318L;
	
	private int type;
	private int port;

	/**
	 * @param type
	 * @param port
	 */
	public LogNode(int type, int port) {
		super();
		this.setType(type);
		this.setPort(port);
	}

	public void setType(int type) {
		this.type = type;
	}
	public int getType() {
		return this.type;
	}
	
	public void setPort(int s) {
		this.port = s;
	}
	public int getPort() {
		return this.port;
	}

	public String getTag() {
		String tag = null;
		switch (type) {
		case Site.TOP_SITE:
			tag = "top";
			break;
		case Site.HOME_SITE:
			tag = "home";
			break;
		case Site.DATA_SITE:
			tag = "data";
			break;
		case Site.CALL_SITE:
			tag = "call";
			break;
		case Site.WORK_SITE:
			tag = "work";
			break;
		case Site.BUILD_SITE:
			tag = "build";
			break;
		}
		return tag;
	}


	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || obj.getClass() != LogNode.class) {
			return false;
		}
		LogNode node = (LogNode) obj;
		return type == node.type && port == node.port;
	}

	public int hashCode() {
		return type ^ port;
	}
	
}