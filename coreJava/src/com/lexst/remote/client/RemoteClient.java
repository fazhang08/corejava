/**
 *
 */
package com.lexst.remote.client;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.security.interfaces.*;

import com.lexst.fixp.*;
import com.lexst.fixp.client.*;
import com.lexst.remote.*;
import com.lexst.security.SecureEncryptor;
import com.lexst.util.*;
import com.lexst.util.host.*;
import com.lexst.visit.*;

public class RemoteClient {

	/* fixp tcp client */
	private FixpStreamClient stream_client;

	/* fixp udp client */
	private FixpPacketClient packet_client;

	/* visit interface name */
	private String interface_name;

	// lock identity
	private boolean locked, into;

	// last invoke time
	private long endtime;

	/**
	 * construct method
	 */
	public RemoteClient(boolean stream) {
		super();
		locked = into = false;
		endtime = 0;
		if (stream) {
			stream_client = new FixpStreamClient();
		} else {
			packet_client = new FixpPacketClient();
		}
	}

	/**
	 * @param stream
	 * @param interfaceName
	 */
	public RemoteClient(boolean stream, String interfaceName) {
		this(stream);
		this.setInterfaceName(interfaceName);
	}
	
	public boolean isStream() {
		return stream_client != null;
	}

	public boolean isPacket() {
		return packet_client != null;
	}

	/**
	 * @param timeout (单位:毫秒)
	 */
	protected synchronized void delay(long timeout) {
		try {
			this.wait(timeout);
		} catch (InterruptedException exp) {

		}
	}

	/**
	 * wakeup thread
	 */
	protected synchronized void wakeup() {
		try {
			this.notify();
		} catch (IllegalMonitorStateException exp) {

		}
	}

	/**
	 * 判断是否处于锁定标记
	 * @return boolean
	 */
	public synchronized boolean isLocked() {
		return locked;
	}

	/**
	 * 设置锁定标记,成功返回TRUE,失败返回FALSE
	 * @return boolean
	 */
	public synchronized boolean lock() {
		if (locked) {
			into = true;
			return false;
		}
		return (locked = true);
	}

	/**
	 * 解除锁定标记,成功返回TRUE,失败返回FALSE
	 * @return
	 */
	public synchronized boolean unlock() {
		if(!locked) return false;
		locked = false;
		// when next object wait, wakeup it
		if (into) {
			into = false;
			try {
				this.notify();
			} catch (IllegalMonitorStateException exp) {
				exp.printStackTrace();
			}
		}
		return true;
	}

	/**
	 * check lock and wait
	 */
	public void locking() {
		while(!this.lock()) {
			this.delay(10L);
		}
	}

	/**
	 * lock object
	 * @param timeout
	 */
	public void locking(long timeout) {
		while(!this.lock()) {
			this.delay(timeout);
		}
	}
	
	/**
	 * retry lock
	 * @param timeout
	 * @param alltime
	 * @return
	 */
	public boolean locking(long timeout, long alltime) {
		long count = 0;
		while(!this.lock()) {
			this.delay(timeout);
			count += timeout;
			if (count >= alltime) return false;
		}
		return true;
	}

	protected void refreshTime() {
		endtime = System.currentTimeMillis();
	}

	public long getEndTime() {
		return endtime;
	}

	public boolean isRefreshTimeout(long timeout) {
		return System.currentTimeMillis() - endtime >= timeout;
	}

	/**
	 * set interface class's name
	 * @param s
	 */
	public void setInterfaceName(String s) {
		this.interface_name = s;
	}
	public String getInterfaceName() {
		return this.interface_name;
	}

	public void setRecvTimeout(int second) {
		if (stream_client != null) {
			stream_client.setReceiveTimeout(second);
		} else {
			packet_client.setReceiveTimeout(second);
		}
	}

	public int getRecvTimeout() {
		if (stream_client != null) {
			return stream_client.getReceiveTimeout();
		} else {
			return packet_client.getReceiveTimeout();
		}
	}

	public void setRemote(String ip, int port) {
		if (stream_client != null) {
			stream_client.setRemote(new SocketHost(SocketHost.TCP, ip, port));
		} else {
			packet_client.setRemote(new SocketHost(SocketHost.UDP, ip, port));
		}
	}

	public void setRemote(SocketHost host) {
		this.setRemote(host.getIP(), host.getPort());
	}

	public SocketHost getRemote() {
		if (stream_client != null) {
			return stream_client.getRemote();
		} else {
			return packet_client.getRemote();
		}
	}
	
	public IP getRemoteIP() throws UnknownHostException {
		if (stream_client != null) {
			return stream_client.getRemote().getSocketIP();
		} else {
			return packet_client.getRemote().getSocketIP();
		}
	}

	public void setBindIP(String s) {
		if (stream_client != null) {
			stream_client.setBindIP(s);
		} else {
			packet_client.setBindIP(s);
		}
	}

	public SocketHost getLocal() {
		if (stream_client != null) {
			return stream_client.getLocal();
		} else {
			return packet_client.getLocal();
		}
	}

	/**
	 * execute connect
	 * @throws IOException
	 */
	public void reconnect() throws IOException {
		if (stream_client != null) {
			stream_client.connect(stream_client.getRemote());
		} else {
			packet_client.connect(packet_client.getRemote());
		}
	}

	/**
	 * @param host
	 * @throws IOException
	 */
	public void connect(SocketHost host) throws IOException {
		if (stream_client != null) {
			stream_client.connect(host);
		} else {
			packet_client.connect(host);
		}
	}

	/**
	 * @return
	 */
	public boolean isConnected() {
		if (stream_client != null) {
			return stream_client.isConnected();
		} else {
			return packet_client.isConnected();
		}
	}

	/**
	 * @return
	 */
	public boolean isClosed() {
		if (stream_client != null) {
			return stream_client.isClosed();
		} else {
			return packet_client.isClosed();
		}
	}

	/**
	 * close socket
	 */
	public void close() {
		if (stream_client != null) {
			stream_client.close();
		} else {
			packet_client.close();
		}
	}

	/**
	 * data exchange by TCP mode
	 * @param request
	 * @param loadBody
	 * @return
	 * @throws IOException
	 */
	public Stream executeStream(Stream request, boolean loadBody) throws IOException {
		if (stream_client != null) {
			return stream_client.execute(request, loadBody);
		} else {
			throw new IOException("invalid call");
		}
	}

	/**
	 * data exchange by KEEP UDP
	 * @param request
	 * @return
	 * @throws IOException
	 */
	public Packet executePacket(Packet request) throws IOException {
		if(packet_client != null) {
			return packet_client.batch(request);
		} else {
			throw new IOException("invalid call");
		}
	}

	/**
	 * tcp nothing call
	 * @param sitetype
	 * @param ip
	 * @param tcport
	 * @param udport
	 * @return
	 * @throws IOException
	 */
	public boolean callNothing(int sitetype, String ip, int tcport, int udport) throws IOException {
		Command cmd = new Command(Request.NOTIFY, Request.HELO);

		if(stream_client != null) {
			Stream request = new Stream(cmd);
			request.addMessage(new Message(Key.SITE_TYPE, sitetype));
			request.addMessage(new Message(Key.IP, ip));
			request.addMessage(new Message(Key.TCPORT, tcport));
			request.addMessage(new Message(Key.UDPORT, udport));
			request.addMessage(new Message(Key.SPEAK, "hello"));
			this.addBind(request, stream_client.getLocalAddress());

			Stream resp = this.executeStream(request, true);
			cmd = resp.getCommand();
			return cmd.getResponse() == Response.OKAY;
		} else {
			Packet request = new Packet(cmd);
			request.addMessage(new Message(Key.SITE_TYPE, sitetype));
			request.addMessage(new Message(Key.IP, ip));
			request.addMessage(new Message(Key.TCPORT, tcport));
			request.addMessage(new Message(Key.UDPORT, udport));
			request.addMessage(new Message(Key.SPEAK, "hello"));
			this.addBind(request, packet_client.getLocalAddress());

			Packet resp = this.executePacket(request);
			cmd = resp.getCommand();
			return cmd.getResponse() == Response.OKAY;
		}
	}

	/**
	 * @return
	 * @throws IOException
	 */
	public boolean exit() throws IOException {
		Command cmd = new Command(Request.NOTIFY, Request.EXIT);
		if (stream_client != null) {
			Stream request = new Stream(getRemote(), cmd);
			this.addBind(request, stream_client.getLocalAddress());
			Stream resp = this.executeStream(request, true);
			cmd = resp.getCommand();
			return cmd.getResponse() == Response.OKAY;
		} else {
			// is udp
			Packet request = new Packet(getRemote(), cmd);
			this.addBind(request, packet_client.getLocalAddress());
			Packet resp = this.executePacket(request);
			cmd = resp.getCommand();
			return cmd.getResponse() == Response.OKAY;
		}
	}

	private void addBind(Entity entity, InetAddress local) {
		if (local != null) {
			Message msg = new Message(Key.BIND_IP, local.getHostAddress());
			entity.replaceMessage(msg);
		}
	}

	/**
	 *
	 * @param interfaceName
	 * @param method
	 * @param params
	 * @return
	 * @throws IOException
	 */
	private Object execute(String interfaceName, Method method, Object[] params)
			throws VisitException, IOException {
		//1. build serial data
		String methodName = method.getName();
		Class<?>[] paramTypes = method.getParameterTypes();
		Apply apply = new Apply(interfaceName, methodName, paramTypes, params);
		byte[] data = apply.build();

		//2. build request stream
		Command cmd = new Command(Request.RPC, Request.EXECUTE);
		if (stream_client != null) {
			Stream request = new Stream(stream_client.getRemote(), cmd);
			// add local ip
			addBind(request, stream_client.getLocalAddress());
			// save serial data
			request.setData(data);
			// 3. send and receive
			Stream resp = stream_client.execute(request, true);
			data = resp.getData();
		} else {
			Packet request = new Packet(packet_client.getRemote(), cmd);
			addBind(request, packet_client.getLocalAddress());
			request.setData(data);
			//3. send and receive (keep udp)
			Packet resp = packet_client.batch(request);
			data = resp.getData();
		}
		
		//4. resolve response data, and check error
		Reply reply = null;
		try {
			reply = Reply.resolve(data);
		} catch (ClassNotFoundException exp) {
			throw new VisitException(Reply.getMessage(exp));
		} catch (IOException exp) {
			throw new VisitException(Reply.getMessage(exp));
		} catch (Throwable exp) {
			throw new VisitException(Reply.getMessage(exp));
		}
		if (reply == null) {
			throw new VisitException("reply is null pointer!");
		}
		// throw error
		if (reply.getThrowable() != null) {
			throw new VisitException(reply.getThrowText());
		}
				
		//5. return result 
		return reply.getObject();
	}

	/**
	 * method invoke
	 * @param method
	 * @param params
	 * @return
	 * @throws IOException
	 */
	public Object invoke(Method method, Object[] params) throws VisitException {
		try {
			return execute(interface_name, method, params);
		} catch (IOException exp) {
			throw new VisitException(getThrowableText(exp));
		} catch (Throwable exp) {
			throw new VisitException(getThrowableText(exp));
		}
	}

	/**
	 * get throws trace
	 * @return
	 */
	private String getThrowableText(Throwable fatal) {
		if (fatal == null) return "";
		ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
		PrintStream s = new PrintStream(out, true);
		fatal.printStackTrace(s);
  		byte[] data = out.toByteArray();
  		return new String(data, 0, data.length);
	}

	public boolean initSecure(RSAPublicKey key, String algo, byte[] pwd) throws IOException {
		ByteArrayOutputStream buff = new ByteArrayOutputStream();
		byte[] algo_bytes = algo.getBytes();
		
		byte[] b = Numeric.toBytes(algo_bytes.length);
		buff.write(b, 0, b.length);
		buff.write(algo_bytes, 0, algo_bytes.length);
		
		b = Numeric.toBytes(pwd.length);
		buff.write(b, 0, b.length);
		buff.write(pwd, 0, pwd.length);
		
		algo_bytes = buff.toByteArray();
		byte[] raw = SecureEncryptor.rsaEncrypt(key, algo_bytes);
		
		Command cmd = new Command(Request.NOTIFY, Request.INIT_SECURE);
		if(stream_client != null) {
			Stream stream = new Stream(stream_client.getRemote(), cmd);
			// add local ip
			addBind(stream, stream_client.getLocalAddress());
			// save serial data
			stream.setData(raw);
			// 3. send and receive
			Stream resp = stream_client.execute(stream, true);
			cmd = resp.getCommand();
		} else {
			Packet packet = new Packet(packet_client.getRemote(), cmd);
			addBind(packet, packet_client.getLocalAddress());
			packet.setData(raw);
			// send and receive (keep udp)
			Packet resp = packet_client.single(packet);
			cmd = resp.getCommand();
		}

		boolean success = (cmd.getResponse() == Response.SECURE_ACCEPTED);

		if (success) {
			Cipher cipher = new Cipher(algo, pwd);
			if (stream_client != null) {
				stream_client.setCipher(cipher);
			} else {
				packet_client.setCipher(cipher);
			}
		}
		
		return success;
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		this.close();
	}
}