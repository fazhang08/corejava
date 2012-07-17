/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com. All rights reserved
 * 
 * fixp request code set
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

public class Request {

	/* major command */
	public final static byte LOGIN = 1;		// lexst login
	public final static byte LOGOUT = 2;	// lexst logut
	public final static byte NOTIFY = 3;	// lexst notify
	public final static byte APP = 4;		// lexst application
	public final static byte DATA = 5;		// lexst remote data request
	public final static byte SQL = 6;		// lexst SQL command
	public final static byte RPC = 7;		// lexst RPC call

	/* LOGIN and LOGOUT sub command (site type)*/
	public final static byte AUTOSITE = 1;
	public final static byte TOPSITE = 2;
	public final static byte HOMESITE = 3;
	public final static byte LIVESITE = 4;
	public final static byte LOGSITE = 5;
	public final static byte DATASITE = 6;
	public final static byte CALLSITE = 7;
	public final static byte WORKSITE = 8;

	/* SQL sub command */
	public final static byte SQL_GETREFRESHTIME = 4;
	public final static byte SQL_CREATE_SCHEMA = 5;
	public final static byte SQL_DELETE_SCHEMA = 6;
	public final static byte SQL_CREATEUSER = 7;
	public final static byte SQL_DELETEUSER = 8;
	public final static byte SQL_ALTERUSER = 9;
	public final static byte SQL_ADDPERMIT = 10;
	public final static byte SQL_DELETEPERMIT = 11;
	public final static byte SQL_GETOPTIONS = 12;
	public final static byte SQL_CREATETABLE = 13;
	public final static byte SQL_DELETETABLE = 14;
	public final static byte SQL_GET_SCHEMAS = 15;
	public final static byte SQL_GETTABLES = 16;
	public final static byte SQL_FINDTABLE = 17;
	public final static byte SQL_CHECKIDENTIFFIED = 18;	//check login identified
	public final static byte SQL_FINDCALLSITE = 19;
	public final static byte SQL_GETCHARSET = 20;
	public final static byte SQL_FINDCHARSET = 21;
	public final static byte SQL_FINDHOMESITE = 22;
	public final static byte SQL_SETCHUNKSIZE = 23;
	public final static byte SQL_SETOPTIMIZETIME = 24;
	
	public final static byte SQL_OPTIMIZE = 25;
	public final static byte SQL_LOADINDEX = 26;
	public final static byte SQL_STOPINDEX = 27;
	public final static byte SQL_LOADCHUNK = 28;
	public final static byte SQL_STOPCHUNK = 29;
	public final static byte SQL_BUILDTASK = 30;

	public final static byte SQL_INSERT = 31;
	public final static byte SQL_SELECT = 32;
	public final static byte SQL_DELETE = 33;
	public final static byte SQL_UPDATE = 34;
	public final static byte SQL_DC = 35;
	public final static byte SQL_ADC = 36;

	/* "NOTIFY" sub command */
	public final static byte HELO = 1;		//握手操作
	public final static byte EXIT = 2;		//退出
	public final static byte COMEBACK = 3;	//HOME节点通知其它节点,注册已经超时,需要立即发送激活包
	public final static byte SHUTDOWN = 4;	//要求本地站点关闭连接,退出运行
	public final static byte DISTRIBUTE_CHUNK = 6;
	public final static byte RETRY_SUBPACKET = 7;
	public final static byte CANCEL_PACKET = 8;
	public final static byte REFRESH_WORKSITE = 9;
	public final static byte REFRESH_DATASITE = 10;
	public final static byte INIT_SECURE = 11;
	
	public final static byte SCANHUB = 12;
	public final static byte HELOHUB = 13;
	public final static byte TRANSFER_HUB = 14;
	
	/* RPC sub command */
	public final static byte EXECUTE = 1;

	/* DATA sub command */
	public final static byte DOWNLOAD_CHUNK = 1;
	public final static byte SET_CACHE_ENTITY = 3;
	public final static byte DELETE_CACHE_ENTITY = 4;
	public final static byte SET_CHUNK_ENTITY = 5;

	/* application sub command */
	public final static byte SITE_TIMEOUT = 1;
	public final static byte APPLY_SINGLE = 2;
	public final static byte APPLY_TABLEKEY = 3;
	public final static byte FIND_CHUNKSIZE = 7;
	public final static byte CURRENT_TIME = 8;
	public final static byte ADD_LOG = 9;
	
	/**
	 * check site type
	 * @param type
	 * @return
	 */
	public static boolean isLegalSite(byte type) {
		switch (type) {
		case Request.AUTOSITE:
		case Request.TOPSITE:
		case Request.HOMESITE:
		case Request.LIVESITE:
		case Request.LOGSITE:
		case Request.DATASITE:
		case Request.CALLSITE:
		case Request.WORKSITE:
			break;
		default:
			return false;
		}
		return true;
	}

	/*
	 * check request command
	 */
	public static boolean isCommand(byte major, byte minor) {
		boolean match = true;
		switch (major) {
		case Request.LOGIN:
			match = Request.isLegalSite(minor);
			break;
		case Request.LOGOUT:
			match = Request.isLegalSite(minor);
			break;
		case Request.NOTIFY:
			break;
		case Request.APP:
			break;
		case Request.DATA:
			break;
		case Request.RPC:
			break;
		case Request.SQL:
			break;
		default:
			match = false;
			break;
		}
		return match;
	}

	public static String getCommandText(byte maincmd, byte subcmd) {
		String maintag = "";
		String subtag = "";
//		switch(maincmd) {
//		case Request.NOTIFY:
//			maintag = "Query";
//			switch(subcmd) {
////				case FixpCommand.LOGIN:
////					subtag = "Login"; break;
////				case FixpCommand.HELLO:
////					subtag = "Hello"; break;
////				case FixpCommand.HELP:
////					subtag = "Help"; break;
////				case FixpCommand.BYE:
////					subtag = "Bye"; break;
//			}
//			break;
//		case Request.SQL:
//			maintag = "Skip";
//			switch(subcmd) {
////			case FixpCommand.INTO:
////				subtag = "Into"; break;
////			case FixpCommand.HOLE:
////				subtag = "Hole"; break;
////			case FixpCommand.HOLEOK:
////				subtag = "HoleOk"; break;
////			case FixpCommand.INTOOK:
////				subtag = "IntoOk"; break;
////			case FixpCommand.INTOREFUSE:
////				subtag = "IntoRefuse"; break;
////			case FixpCommand.GOTO:
////				subtag = "Goto"; break;
////			case FixpCommand.HOLEREFUSE:
////				subtag = "HoleRefuse"; break;
////			case FixpCommand.REGISTRY:
////				subtag = "Registry"; break;
////			case FixpCommand.UNREGISTRY:
////				subtag = "Unregistry"; break;
////			case FixpCommand.REGBACK:
////				subtag = "RegBack"; break;
////			case FixpCommand.AGENT:
////				subtag = "Agent"; break;
////			case FixpCommand.ADDENTRYSITE:
////				subtag = "AddEntrySite"; break;
////			case FixpCommand.REMOVEENTRYSITE:
////				subtag = "RemoveEntrySite"; break;
////			case FixpCommand.REGNOTIFY:
////				subtag = "RegNotify"; break;
////			case FixpCommand.CHECKSITE:
////				subtag = "CheckSite"; break;
//			}
//			break;
////		case Command.notify:
////			maintag = "Search";
////			switch(subcmd) {
//////			case FixpCommand.LETTER:
//////				subtag = "Letter"; break;
//////			case FixpCommand.FILE:
//////				subtag = "File"; break;
//////			case FixpCommand.LIST:
//////				subtag = "List"; break;
////			}
////			break;
//		case Request.DATA:
//			maintag = "Exchange";
//			switch(subcmd) {
////			case FixpCommand.GET:
////				subtag = "Get"; break;
////			case FixpCommand.SEND:
////				subtag = "Send"; break;
////			case FixpCommand.SENDTEST:
////				subtag = "SendTest"; break;
////			case FixpCommand.OBJBOKHASH:
////				subtag = "ObjBokHash"; break;
////			case FixpCommand.PACKETTEST:
////				subtag = "PacketTest"; break;
////			case FixpCommand.MAP:
////				subtag = "Map"; break;
////			case FixpCommand.ACKNOW:
////				subtag = "Acknow"; break;
////			case FixpCommand.RESEND:
////				subtag = "Resend"; break;
//			}
////		case FixpCommand.CHAT:
////			maintag = "Chat";
////			break;
//		}
		return maintag + "-" + subtag;
	}
}
