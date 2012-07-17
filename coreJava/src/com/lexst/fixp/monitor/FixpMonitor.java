/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com. All rights reserved
 * 
 * fixp basic monitor
 * 
 * @author yj.liang lexst@126.com
 * 
 * @version 1.0 3/15/2009
 * 
 * @see com.lexst.fixp.monitor
 * 
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.fixp.monitor;

import com.lexst.thread.*;

import com.lexst.fixp.Security;

abstract class FixpMonitor extends VirtualThread {

	protected Security security;

	/**
	 * 
	 */
	public FixpMonitor() {
		super();
	}

	public void setSecurity(Security instance) {
		this.security = instance;
	}

	public Security getSecurity() {
		return this.security;
	}

}
