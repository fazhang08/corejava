/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com. All rights reserved
 * 
 * fixp exception class
 * 
 * @author yj.liang lexst@126.com
 * 
 * @version 1.0 3/13/2009
 * 
 * @see com.lexst.fixp
 * 
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.fixp;


public class FixpProtocolException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	public FixpProtocolException() {
		super();
	}

	/**
	 * @param msg
	 */
	public FixpProtocolException(String msg) {
		super(msg);
	}
	
	/**
	 * @param format
	 * @param args
	 */
	public FixpProtocolException(String format, Object ... args) {
		this(String.format(format, args));
	}

	/**
	 * @param cause
	 */
	public FixpProtocolException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public FixpProtocolException(String message, Throwable cause)  {
		super(message, cause);
	}
}