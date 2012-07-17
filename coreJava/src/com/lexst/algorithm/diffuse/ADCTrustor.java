/**
 * 
 */
package com.lexst.algorithm.diffuse;

public interface ADCTrustor {
	
	/**
	 * get next job identity
	 * @return
	 */
	long nextIdentity();

	/**
	 * write data to disk
	 * 
	 * @param jobid
	 * @param mod
	 * @param data
	 * @param off
	 * @param len
	 * 
	 * @return file offset pair(begin and end)
	 */
	long[] write(long jobid, int mod, byte[] data, int off, int len);

	/**
	 * read data from disk
	 * 
	 * @param jobid
	 * @param begin
	 * @param end
	 * @return
	 */
	byte[] read(long jobid, int mod, long begin, long end);
}