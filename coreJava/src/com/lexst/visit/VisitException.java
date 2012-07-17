/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com, All rights reserved
 * 
 * this file is part of lexst
 * 
 * visit error class
 * 
 * @author yj.liang lexst@126.com
 * 
 * @version 1.0 2/2/2009
 * @see com.lexst.visit
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.visit;

import java.io.*;

public class VisitException extends IOException {
	private static final long serialVersionUID = 8851025042175217907L;

	/**
	 *
	 */
	public VisitException() {
		super();
	}

	/**
	 * @param message
	 */
	public VisitException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public VisitException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public VisitException(String message, Throwable cause) {
		super(message, cause);
	}

}