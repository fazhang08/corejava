/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com. All rights reserved
 * 
 * fixp stream analyser and processer
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

import java.io.*;
import java.net.*;

import com.lexst.fixp.*;
import com.lexst.invoke.*;
import com.lexst.remote.*;
import com.lexst.thread.*;
import com.lexst.util.*;
import com.lexst.util.host.*;
import com.lexst.security.*;

final class StreamTask extends VirtualThread {

	private Socket socket;
	private DataInputStream dataIn;
	private DataOutputStream dataOut;
	private SocketHost remote;

	private IStreamListener listener;
	private RPCInvoker rpcall;
	private StreamInvoker streamCall;

	private boolean exited;
	
	private Security security;
	
	private Cipher cipher;

	/**
	 *
	 */
	public StreamTask(Socket socket, IStreamListener listener, RPCInvoker call1, StreamInvoker call2) throws IOException {
		super();
		this.exited = false;
		this.socket = socket;
		remote = new SocketHost(SocketHost.TCP,
			socket.getInetAddress().getHostAddress(), socket.getPort());

		dataIn = new DataInputStream(socket.getInputStream());
		dataOut = new DataOutputStream( socket.getOutputStream() );
		this.listener = listener;
		this.rpcall = call1;
		this.streamCall = call2;
	}
	
	/**
	 * set security instance
	 * @param instance
	 */
	public void setSecurity(Security instance) {
		this.security = instance;
	}

	public Security getSecurity() {
		return this.security;
	}

	private void closeSocket() {
		if(socket == null) return;
		try {
			socket.close();
		} catch (IOException exp) {

		} finally {
			socket = null;
		}
	}
	
	private boolean matchAddress(Stream stream) {
		Message msg = stream.findMessage(Key.BIND_IP);
		if(msg == null) return false;
		String address = msg.stringValue();
		SocketHost remote = stream.getRemote();
		return remote.getIP().equalsIgnoreCase(address);
	}

	private boolean authenticate(Stream request) {
		if (security == null || security.isNone()) return true;

		SocketHost remote = request.getRemote();
		
		if (security.isAddressMatch()) {
			if (!matchAddress(request)) {
				return false;
			}
		} else if (security.isAddressCheck()) {
			// check address range
			long ip = remote.getIPv4() & Long.MAX_VALUE;
			if (!security.isLegalAddress(ip)) {
				return false;
			}
		} else if (security.isCipherCheck()) {

		} else if (security.isDoubleCheck()) {
			long ip = remote.getIPv4() & Long.MAX_VALUE;
			if (!security.isLegalAddress(ip)) {
				if (cipher != null) {
					// check
				}
			}
		}

		return true;
	}
	
	/**
	 * execute remote call
	 * @param request
	 * @return
	 */
	private void callRPC(Stream request) throws IOException {
		if(!authenticate(request)) {
			Command cmd = new Command(Response.AUTHENTICATE_FAILED);
			Stream resp = new Stream(cmd);
			this.send(resp);
			return;
		}
		
		byte[] data = request.getData();
		//1. resolve rpc data
		Apply apply = null;
		Reply reply = null;
		try {
			if(data == null) {
				throw new NullPointerException("RPC data is null");
			}
			apply = Apply.resolve(data);
		} catch (IOException exp) {
			reply = new Reply(null, exp);
		} catch (ClassNotFoundException exp) {
			reply = new Reply(null, exp);
		} catch (Throwable exp) {
			reply = new Reply(null, exp);
		}
		//2. call object instance
		if (reply == null) {
			reply = rpcall.invoke(apply);
		}
		//3. build response stream
		data = reply.build();
		
		Command cmd = new Command(Response.OKAY);
		Stream resp = new Stream(cmd);
		resp.addMessage(new Message(Key.CONTENT_TYPE, Value.RAW_DATA));
		resp.setData(data);
		this.send(resp);
	}
	
	/**
	 * @param request
	 * @throws IOException
	 */
	private void callMethod(Stream request) throws IOException {		
		if(!authenticate(request)) {
			Command cmd = new Command(Response.AUTHENTICATE_FAILED);
			Stream resp = new Stream(cmd);
			this.send(resp);
			return;
		}
		
		if (cipher != null) {
			request.setCipher(cipher);
		}
		
		Command cmd = request.getCommand();
		// call object instance
		streamCall.invoke(request, dataOut);
		// if logout, exit process
		if (cmd.isLogout()) {
			exited = true;
		}
	}
	
	/**
	 * @param stream
	 * @throws IOException
	 */
	private void send(Stream stream) throws IOException {
		// encrypt data
		byte[] data = stream.getData();
		if (cipher != null && data != null && data.length > 0) {
			data = cipher.encrypt(data);
			if(data == null) {
				throw new IOException("encrypt failed!");
			}
			stream.setData(data);
		}

		// send binary data
		byte[] b = stream.build();
		dataOut.write(b, 0, b.length);
		dataOut.flush();
	}

	/**
	 * goodbye notify
	 */
	private void goodbye() throws IOException {
		Command cmd = new Command(Response.OKAY);
		Stream resp = new Stream(cmd);
		resp.addMessage(Key.SPEAK, "see you next time");
		send(resp);
	}
	
	/**
	 * init security service
	 * @param request
	 * @return
	 * @throws IOException
	 */
	private boolean initSecure(Stream request) throws IOException {
		short code = Response.NOTACCEPTED;
		byte[] data = request.getData();
		if (data != null) {
			code = init_secure(data);
		}
		
		Command cmd = new Command(code);
		Stream resp = new Stream(cmd);
		resp.addMessage(Key.SPEAK, "hello!");
		this.send(resp);
		
		return code == Response.SECURE_ACCEPTED;
	}
	
	private short init_secure(byte[] raws) throws IOException {
		if (security == null || security.getPrivateKey() == null) {
			return Response.NOTACCEPTED;
		} else if (security.getType() != Security.CIPHERTEXT_CHECK && security.getType() != Security.DOUBLE_CHECK) {
			return Response.NOTACCEPTED;
		}

		byte[] data = SecureDecryptor.rsaDecrypt(security.getPrivateKey(), raws);
		if (data == null || data.length == 0) {
			return Response.NOTACCEPTED;
		}
		
		int off = 0;
		if (off + 4 > data.length) return Response.NOTACCEPTED;
		int len = Numeric.toInteger(data, off, 4);
		off += 4;
		
		if(off + len > data.length) return Response.NOTACCEPTED;
		String algo = new String(data, off, len);
		off += len;
		
		if(off + 4 > data.length) return Response.NOTACCEPTED;
		len = Numeric.toInteger(data, off, 4);
		off += 4;
		
		if(off + len > data.length) return Response.NOTACCEPTED;
		byte[] pwd = new byte[len];
		System.arraycopy(data, off, pwd, 0, pwd.length);
		off += pwd.length;
		
		try {
			cipher = new Cipher(algo, pwd);
		} catch (IllegalArgumentException exp) {
			return Response.NOTACCEPTED;
		}
		
		return Response.SECURE_ACCEPTED;
	}

	/**
	 * send invalid packet
	 * @param host
	 */
	private void invalid(short code) throws IOException {
		Command cmd = new Command(code);
		Stream resp = new Stream(cmd);
		resp.addMessage(Key.SPEAK, "sorry!");
		this.send(resp);
	}

	/**
	 * @throws IOException
	 */
	private void subprocess() throws IOException {
		Stream request = new Stream(remote);
		
//		// read head data (cannot read content)
//		request.read(dataIn, dataOut, false);
		
		if (cipher == null) {
			// read head data (cannot read content)
			request.read(dataIn, dataOut, false);
		} else {
			request.read(dataIn, dataOut, true);
			// decrypt data
			byte[] data = request.getData();
			
			if (data != null && data.length > 0) {
				byte[] bs = cipher.decrypt(data);
				if(bs == null) {
					invalid(Response.DECRYPT_FAILED);
					return;
				}
				request.setData(bs);
			}
		}
		
		// check command
		Command cmd = request.getCommand();

		// send response
		if (cmd.isRPCall()) { // rpc call
			if (cipher == null) request.readBody();
			this.callRPC(request);
		} else if(cmd.isInitSecure()) {
			if (cipher == null) request.readBody();
			boolean success = initSecure(request);
			if(!success) exited = true;
		} else if (cmd.isExit()) {
			if(cipher == null) request.readBody();
			exited = true;
			this.goodbye();
		} else if (streamCall != null) {
			this.callMethod(request);
		} else {
			this.invalid(Response.UNSUPPORT_COMMAND);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.lexst.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// close socket
		this.closeSocket();
		// remove self
		listener.remove(this);
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		while (!isInterrupted()) {
			if (exited) break;
			try {
				this.subprocess();
			} catch (FixpProtocolException exp) {
				exp.printStackTrace();
				break;
			} catch (IOException exp) {
				exp.printStackTrace();
				break;
			} catch (Throwable exp) {
				exp.printStackTrace();
				break;
			}
		}
		// self stop
		setInterrupted(true);
	}

}