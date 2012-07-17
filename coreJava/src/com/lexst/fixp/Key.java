/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com. All rights reserved
 * 
 * fixp message key
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

public final class Key {

	/* common key list */
	public final static short SPEAK = 1000;
	public static final short TIMEOUT = 1001;
	public final static short SERIAL_TYPE = 1020;
	public final static short SERIAL_OBJECTS = 1021;
	public final static short CONTENT_LENGTH = 1030;
	public final static short CONTENT_TYPE = 1031;
	public final static short CONTENT_ITEMS = 1032;	
	public final static short CHECKSUM_CRC32 = 1040;
	
	/* chunk key list */
	public static final short CHUNK_ID = 1100;
	public static final short CHUNK_COUNT = 1101;
	public static final short CHUNK_LENGTH = 1102;
	public static final short CHUNK_LASTMODIFIED = 1103;
	public static final short CHUNK_BREAKPOINT = 1104;
	
	/* authorize list */
	public static final short CIPHERTEXT_ALGORITHM = 1200;

	/* site key list */
	public final static short SITE_TYPE = 2000;
	public final static short HOME_IP = 2001;
	public final static short HOME_TCPORT = 2002;
	public final static short HOME_UDPORT = 2003;
	public final static short DATA_IP = 2004;
	public final static short DATA_TCPORT = 2005;
	public final static short DATA_UDPORT = 2006;
	public final static short CALL_IP = 2007;
	public final static short CALL_TCPORT = 2008;
	public final static short CALL_UDPORT = 2009;
	public final static short LIVE_IP = 2010;
	public final static short LIVE_TCPORT = 2011;
	public final static short LIVE_UDPORT = 2012;
	public final static short WORK_IP = 2013;
	public final static short WORK_TCPORT = 2014;
	public final static short WORK_UDPORT = 2015;
	public final static short DATA_TCPHOST = 2016;
	public final static short WORK_TCPHOST = 2017;
	public final static short MASTER_DATA_IP = 2018;
	public final static short MASTER_DATA_TCPORT = 2019;
	public final static short MASTER_DATA_UDPORT = 2020;

	/* ip address */
	public final static short IP = 2100;
	public final static short TCPORT = 2101;
	public final static short UDPORT = 2102;
	public final static short BIND_IP = 2103;
	public final static short SERVER_IP = 2104;
	public final static short SERVER_TCPORT = 2105;
	public final static short SERVER_UDPORT = 2106;
	public final static short IP_COUNT = 2107;
	public final static short HOST_COUNT = 2108;
	
	public final static short SERVER_ADDRESS = 2110;
	public final static short LOCAL_ADDRESS = 2111;
	
	/* sub packet key list */
	public final static short PACKET_IDENTIFY = 3000;
	public final static short SUBPACKET_COUNT = 3001;
	public final static short SUBPACKET_SERIAL = 3002;
	public final static short SUBPACKET_TIMEOUT = 3003;
	
	/* sql key list */
	public final static short SCHEMA = 3100;
	public final static short TABLE = 3101;
	
	/* write mode of  INSERT AND INJECT (sync or async) */ 
	public final static short INSERT_MODE = 3110; 

	/* DC and ADC instance */
	public final static short DC_OBJECT = 3120;
	public final static short ADC_OBJECT = 3121;

}