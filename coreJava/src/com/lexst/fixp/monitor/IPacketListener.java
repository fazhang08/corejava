/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com. All rights reserved
 * 
 * fixp udp server interface
 * 
 * @author yj.liang lexst@126.com
 * 
 * @version 1.0 3/16/2009
 * 
 * @see com.lexst.fixp.monitor
 * 
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.fixp.monitor;

import com.lexst.fixp.Packet;
import com.lexst.util.host.*;

public interface IPacketListener {

	boolean send(SocketHost remote, Packet packet);

	boolean addCipher(SocketHost remote, String algo, byte[] pwd);

	boolean removeCipher(SocketHost remote);
	
	boolean existsCipher(SocketHost remote);
}