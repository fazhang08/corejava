/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com. All rights reserved
 * 
 * this file is part of lexst
 * 
 * @author zhicheng.liang lexst@126.com
 * @version 1.0 11/17/2009
 * @see com.lexst.util
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.invoke;

import com.lexst.remote.Apply;
import com.lexst.remote.Reply;


public interface RPCInvoker {

	/**
	 * @param apply
	 * @return
	 */
	 Reply invoke(Apply apply);
}