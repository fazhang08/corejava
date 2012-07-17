/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com, All rights reserved
 * 
 * sql virtual object 
 * 
 * @author yj.liang lexst@126.com
 * 
 * @version 1.0 5/5/2009
 * 
 * @see com.lexst.db.statement
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.db.statement;

import java.io.*;

public class BasicObject implements Serializable {

	private static final long serialVersionUID = 4737883774150762022L;

	/* sql operator method */
	public final static byte SELECT_METHOD = 1;
	public final static byte DELETE_METHOD = 2;
	public final static byte UPDATE_METHOD = 3;
	public final static byte INSERT_METHOD = 4;
	public final static byte SORT_METHOD = 5;
	public final static byte DC_METHOD = 6;
	public final static byte ADC_METHOD = 7;

	/* sql function type */
	public final static byte SPACE = 1;
	public final static byte CONDITION = 2;
	public final static byte COLUMNS = 3;
	public final static byte CHUNKS = 4;
	public final static byte ORDERBY = 5;
	public final static byte RANGE = 6;
	public final static byte SNATCH = 7;
	public final static byte SDOTSET = 8;

	// sql method (select, delete, update, insert, dc, adc)
	protected byte method;

	/**
	 * 
	 */
	public BasicObject() {
		super();
	}

	/**
	 *
	 */
	public BasicObject(byte method) {
		this();
		this.setMethod(method);
	}

	public void setMethod(byte b) {
		this.method = b;
	}
	public byte getMethod() {
		return this.method;
	}
}
