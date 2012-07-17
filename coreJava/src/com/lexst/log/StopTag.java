/**
 * 
 */
package com.lexst.log;


public final class StopTag {

	public static final byte[] cmd = { (byte) 0xFA, (byte) 0x12, (byte) 0x83,
			(byte) 0x35, (byte) 0x87, (byte) 0xFF, (byte) 0x90, (byte) 0x2,
			(byte) 0x63, (byte) 0x76, (byte) 0xB6, (byte) 0x52, (byte) 0x32,
			(byte) 0x15, (byte) 0x86, (byte) 0xAF };


	public static boolean isLength(int size) {
		return StopTag.cmd.length == size;
	}

	/**
	 * check close command
	 * @param data
	 * @return
	 */
	public static boolean isTag(byte[] data, int off, int len) {
		if (data == null || len != StopTag.cmd.length) {
			return false;
		}
		for (int i = 0; i < StopTag.cmd.length; i++) {
			if (StopTag.cmd[i] != data[off++]) return false;
		}
		return true;
	}
	
}
