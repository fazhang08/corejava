/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com. All rights reserved
 * 
 * fixp reply code set
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

public class Response {

	/** suggest command  **/
	public static final short ISEE = 1000;

	public final static short SQL_ADMIN = 1010;
	public final static short SQL_CLIENT = 1011;
	
	public final static short HOME_ISEE = 1020;
	public final static short LIVE_ISEE = 1021;

	public final static short IP_EXISTED = 1030;
	public final static short ACCOUNT_EXISTED = 1031;
	public final static short TABLE_EXISTED = 1032;

	/** correct command **/
	public final static short OKAY = 2000;
	public final static short ACCEPTED = 2001;
	public final static short SECURE_ACCEPTED = 2002;
	
	/** error command **/
	public final static short REFUSE = 3000;
	public final static short UNSUPPORT = 3001;
	public final static short NOTLOGIN = 3002;
	public final static short CLIENT_ERROR = 3003;
	public final static short SERVER_ERROR = 3004;
	public final static short ENCRYPT_FAILED = 3005;
	public final static short DECRYPT_FAILED = 3006;
	
	public final static short NOTFOUND = 3010;
	public final static short NOTFOUND_ACCOUNT = 3011;
	public final static short NOTFOUND_SCHEMA = 3012;
	public final static short NOTFOUND_TABLE = 3013;
	
	public final static short NOTACCEPTED = 3020;

	public final static short DATA_INSERT_FAILED = 3034;
	public final static short DATA_CHECKSUM_ERROR = 3035;

	public final static short CHUNK_SIZEOUT = 3050;
	public final static short AUTHENTICATE_FAILED = 3060;
	public final static short ADDRESS_NOTMATCH = 3061;
	public final static short ADDRESS_ILLEGAL = 3062;
	
	/** sql command **/
	public final static short SELECT_FOUND = 3200;
	public final static short SELECT_NOTFOUND = 3201;
	public final static short DELETE_FOUND = 3202;
	public final static short DELETE_NOTFOUND = 3203;
	public final static short DC_FOUND = 3204;
	public final static short DC_NOTFOUND = 3205;
	public final static short DC_SIZENOTMATCH = 3206;
	public final static short DC_CLIENTERR = 3207;
	public final static short DC_SERVERERR = 3208;
	

	/** unkonwn command **/
	public final static short UNKNOWN_COMMAND = 4000;
	public final static short UNIDENTIFIED = 4001;
	public final static short UNSUPPORT_COMMAND = 4002;

	/** fixp command range **/
	public static final short MIN_REPLYCODE = 1000;
	public static final short MAX_REPLYCODE = 4999;

	/**
	 * check reply code
	 * @param code
	 * @return
	 */
	public static boolean isCode(short code){
		return Response.MIN_REPLYCODE <= code && code <= Response.MAX_REPLYCODE;
	}

}