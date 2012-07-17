/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com. All rights reserved
 * 
 * fixp tcp client
 * 
 * @author yj.liang lexst@126.com
 * 
 * @version 1.0 3/16/2009
 * 
 * @see com.lexst.fixp.client
 * 
 * @license GNU Lesser General Public License (LGPL)
 */

package com.lexst.fixp.client;

import java.io.*;
import java.net.*;

import com.lexst.fixp.*;
import com.lexst.security.*;
import com.lexst.util.host.*;
import com.lexst.util.lock.*;

public class FixpStreamClient extends FixpClient {
	
	/* tcp socket */
	protected Socket socket;
	protected DataInputStream receiver;
	protected DataOutputStream sender;

	private SingleLock lock = new SingleLock();

	/**
	 * default constructor
	 */
	public FixpStreamClient() {
		super();
	}

	/**
	 * @param s
	 * @throws IOException
	 */
	public FixpStreamClient(Socket s) throws IOException {
		this();
		this.socket = s;
		super.setRemote(new SocketHost(SocketHost.TCP, s.getInetAddress().getHostAddress(), s.getPort()));
		receiver = new DataInputStream(s.getInputStream());
		sender = new DataOutputStream(s.getOutputStream());
	}

	/**
	 * return local address
	 * @return
	 */
	public SocketHost getLocal() {
		String ip = socket.getLocalAddress().getHostAddress();
		int port = socket.getLocalPort();
		return new SocketHost(SocketHost.TCP, ip, port);
	}

	/**
	 * check connection status
	 *
	 * @return boolean
	 */
	public boolean isConnected() {
		return socket != null && socket.isConnected();
	}
	
	/**
	 * check close status
	 * 
	 * @return
	 */
	public boolean isClosed() {
		return socket == null;
	}

	public InetAddress getRemoteAddress() {
		if (socket == null) return null;
		return socket.getInetAddress();
	}

	public int getRemotePort() {
		return socket == null ? -1 : socket.getPort();
	}

	public InetAddress getLocalAddress() {
		if (socket == null) return null;
		return socket.getLocalAddress();
	}

	public int getLocalPort() {
		return socket == null ? -1 : socket.getLocalPort();
	}

	/**
	 * @param host
	 * @throws IOException
	 */
	public void connect(SocketHost host) throws IOException {
		this.connect(host.getIP(), host.getPort());
	}

	/**
	 * connect server
	 *
	 * @param ip
	 * @param port
	 * @throws IOException
	 */
	public void connect(String ip, int port) throws IOException {
		if (ip == null || port < 1) {
			throw new IllegalArgumentException("host is null, or port<1");
		}
		InetSocketAddress address = new InetSocketAddress(ip, port);
		this.connect(address);
		this.setRemote(new SocketHost(SocketHost.TCP, ip, port));
	}

	/**
	 * connect to fixp server
	 * @param address
	 * @throws IOException
	 */
	private void connect(SocketAddress address) throws IOException {
		// when connected, close it
		if(isConnected()) {
			this.close();
		}

		socket = new Socket();
		if (receive_buffsize > 0) {
			socket.setReceiveBufferSize(receive_buffsize);
		}
		if (send_buffsize > 0) {
			socket.setSendBufferSize(send_buffsize);
		}
		if (receive_timeout > 0) {
			socket.setSoTimeout(receive_timeout * 1000);
		}
		// if local ip valid, bind local
		if(bindIP != null) {
			InetSocketAddress local = new InetSocketAddress(bindIP, 0);
			socket.bind(local);
		}
		// connect to server
		socket.connect(address, connect_timeout * 1000);
		// get handle
		receiver = new DataInputStream(socket.getInputStream());
		sender = new DataOutputStream(socket.getOutputStream());
	}

	/**
	 * check connect
	 * @param host
	 * @throws IOException
	 */
	private void check(SocketHost host) throws IOException {
		if (isClosed()) {
			if (host.isValid()) {
				this.connect(host);
			} else if (remote.isValid()) {
				this.reconnect();
			} else {
				throw new IllegalArgumentException("invalid host!");
			}
		} else if (host.isValid() && !host.equals(remote)) {
			this.connect(host);
		}
	}

	/**
	 * send data to server
	 * @param stream
	 * @throws IOException
	 */
	public void send(Stream stream) throws IOException {
		check(stream.getRemote());
		
		// encrypt data
		byte[] data = stream.getData();
		if (cipher != null && data != null && data.length > 0) {
			switch (cipher.getAlgorithm()) {
			case Cipher.DES:
				data = SecureEncryptor.desEncrypt(cipher.getPassword(), data);
				break;
			case Cipher.DES3:
				data = SecureEncryptor.des3Encrypt(cipher.getPassword(), data);
				break;
			case Cipher.MD5:
				data = SecureEncryptor.md5Encrypt(data);
				break;
			case Cipher.SHA1:
				data = SecureEncryptor.sha1Encrypt(data);
				break;
			}
			stream.setData(data);
		}
		
		// send data
		byte[] b = stream.build();
		sender.write(b, 0, b.length);
		sender.flush();
	}

	/**
	 * receive stream data
	 * @param loadContent
	 * @return
	 * @throws IOException
	 */
	public Stream receive(boolean loadContent) throws IOException {
		// receive and parse data
		Stream resp = new Stream();
		resp.read(receiver, sender, loadContent);
		
		// decrypt data
		byte[] data = resp.getData();
		if (cipher != null && data != null && data.length > 0) {
			switch (cipher.getAlgorithm()) {
			case Cipher.DES:
				data = SecureDecryptor.desDecrypt(cipher.getPassword(), data);
				break;
			case Cipher.DES3:
				data = SecureDecryptor.des3Decrypt(cipher.getPassword(), data);
				break;
			case Cipher.MD5:
				data = SecureDecryptor.md5Decrypt(data);
				break;
			case Cipher.SHA1:
				data = SecureDecryptor.sha1Decrypt(data);
				break;
			}
			resp.setData(data);
		}

		return resp;
	}

	/**
	 * close socket
	 */
	public void close() {
		if (isClosed()) return;
		try {
			if (socket != null) {
				socket.close();
			}
		} catch (Throwable exp) {
			if (this.isDebug()) {
				exp.printStackTrace();
				String s = String.format("%s %s", remote.toString(), exp.getMessage());
				System.out.println(s);
			}
		} finally {
			this.receiver = null;
			this.sender = null;
			this.socket = null;
		}
	}

	/**
	 * reconnect socket
	 * @throws IOException
	 */
	public void reconnect() throws IOException {
		this.close();
		this.connect(remote);
	}

	/**
	 * @param request
	 * @param readBody
	 * @return
	 * @throws IOException
	 */
	private Stream __execute(Stream request, boolean readBody) throws IOException {
		SocketHost remote = request.getRemote();
		// check connect
		this.check(remote);
		
		// encrypt data
		byte[] data = request.getData();
		if (cipher != null && data != null && data.length > 0) {
			data = cipher.encrypt(data);
			if (data == null) {
				throw new IOException("encrypt failed!");
			}
			request.setData(data);
		}

		// send data
		byte[] b = request.build();
		sender.write(b, 0, b.length);
		sender.flush();
		// receive and parse data
		Stream resp = new Stream(remote);
		resp.read(receiver, sender, readBody);
		
		// decrypt data
		if (readBody) {
			data = resp.getData();
			if (cipher != null && data != null && data.length > 0) {
				data = cipher.decrypt(data);
				if (data == null) {
					throw new IOException("decrypt failed!");
				}
				resp.setData(data);
			}
		}
		
		return resp;
	}

	/**
	 * send stream and receive stream
	 * @param request
	 * @param readBody (load stream body, yes or no)
	 * @return
	 * @throws IOException
	 */
	public Stream execute(Stream request, boolean readBody) throws IOException {
		lock.lock();
		try {
			return __execute(request, readBody);
		} catch (IOException exp) {
			this.close();
			throw exp;
		} catch (Throwable exp) {
			this.close();
			throw new IOException(exp);
		} finally {
			lock.unlock();
		}
	}

}