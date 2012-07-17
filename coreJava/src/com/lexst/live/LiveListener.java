/**
 * 
 */
package com.lexst.live;

import com.lexst.util.host.SocketHost;

public interface LiveListener {

	public void flicker();
	
	public void shutdown();
	
	public void disconnect();
	
	public void active(int num, SocketHost topsite);
}