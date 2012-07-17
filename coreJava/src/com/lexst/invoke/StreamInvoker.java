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

import java.io.IOException;
import java.io.OutputStream;
import com.lexst.fixp.Stream;

public interface StreamInvoker {

	/**
	 * @param request
	 * @param resp
	 * @throws IOException
	 */
	void invoke(Stream request, OutputStream resp) throws IOException;
}