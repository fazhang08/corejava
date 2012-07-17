/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com. All rights reserved
 * 
 * fixp message value
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

public final class Value {
	
	/* message type (4 bit) */
	public static final byte BINARY = 0;
	public static final byte CHAR = 1;
	public static final byte BOOLEAN = 2;
	public static final byte FLOAT32 = 3;
	public static final byte FLOAT64 = 4;
	public static final byte INT16 = 5;
	public static final byte INT32 = 6;
	public static final byte INT64 = 7;

	public final static int RCP_REQUEST = 1;
	public final static int TCP_REQUEST = 2;

	/* content type list */
	public static final byte CHUNK_DATA = 1;
	public static final byte XML_DATA = 2;
	public static final byte RAW_DATA = 3;

	/* site type list */
	public final static int HOME_SITE = 1;
	public final static int LOG_SITE = 2;
	public final static int DATA_SITE = 3;
	public final static int CALL_SITE = 4;
	public final static int BUILD_SITE = 5;
	public final static int LIVE_SITE = 6;
	
	/* sql insert mode */
	public final static int INSERT_SYNC = 1;
	public final static int INSERT_ASYNC = 2;

}