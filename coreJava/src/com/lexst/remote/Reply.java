/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com  All rights reserved
 * 
 * RPC basic class, network response
 * 
 * @author yj.liang lexst@126.com
 * 
 * @version 1.0 2/1/2009
 * 
 * @see com.lexst.remote
 * 
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.remote;

import java.io.*;

public class Reply implements Serializable {
	private static final long serialVersionUID = 9173693351345878761L;

	private Object object;
	private Throwable fatal;

	public Reply() {
		super();
	}

	public Reply(Object obj, Throwable err) {
		this();
		this.setObject(obj);
		this.setThrowable(err);
	}

	public Reply(Object obj) {
		this();
		this.setObject(obj);
	}

	public Object getObject() {
		return this.object;
	}
	public void setObject(Object obj) {
		this.object =  obj ;
	}

	public void setThrowable(Throwable e) {
		this.fatal = e;
	}

	public Throwable getThrowable() {
		return this.fatal;
	}

	/**
	 * 返回错误堆栈信息
	 * @return
	 */
	public String getThrowText() {
		return Reply.getMessage(fatal);
	}

	/**
	 * 返回错误信息堆栈
	 * @param e
	 * @return
	 */
	public static String getMessage(Throwable e) {
		if (e == null) return "";
		ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
		PrintStream s = new PrintStream(out, true);
		e.printStackTrace(s);
  		byte[] data = out.toByteArray();
  		return new String(data, 0, data.length);
	}

	/**
	 * 生成字节流
	 * @return
	 */
	public byte[] build() {
		try {
			ByteArrayOutputStream a = new ByteArrayOutputStream(1024);
			ObjectOutputStream o = new ObjectOutputStream(a);
			o.writeObject(this);
			o.close();
			return a.toByteArray();
		} catch (IOException exp) {
			//Logger.error("Reply.build", exp);
		} catch (Throwable exp) {
			//Logger.error("Reply.build", exp);
		}
		return null;
	}

	/**
	 * 生成字节流
	 * @param object
	 * @return
	 */
	public static byte[] build(Object object) {
		Reply reply = new Reply(object);
		return reply.build();
	}

	/**
	 * 解析成类
	 * @param b
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Reply resolve(byte[] b) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bi = new ByteArrayInputStream(b);
		ObjectInputStream oi = new ObjectInputStream(bi);
		Reply reply = (Reply) oi.readObject();
		oi.close();
		bi.close();
		return reply;
	}

}
