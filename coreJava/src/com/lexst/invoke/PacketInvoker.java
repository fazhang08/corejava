/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com. All rights reserved
 * 
 * this file is part of lexst
 * 
 * @author zhicheng.liang lexst@126.com
 * 
 * @version 1.0 12/1/2009
 * 
 * @see com.lexst.invoke
 * 
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.invoke;

import com.lexst.fixp.*;


public interface PacketInvoker {

	/**
	 * invoke object
	 * @param request
	 * @return
	 */
	Packet invoke(Packet request);
}