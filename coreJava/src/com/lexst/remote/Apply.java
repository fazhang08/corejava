/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com  All rights reserved
 * 
 * RPC basic class, network request
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

public class Apply implements Serializable {
	private static final long serialVersionUID = 6160007600000448018L;

	private String interfaceName;	//调用的接口名
	private String methodName;		//接口中的方法名
	private Class<?>[] paramTypes;	//参数类型
	private Object[] params;		//实际参数值

	/**
	 * default construct
	 */
	public Apply() {
		super();
	}

	public Apply(Object[] params) {
		this();
		this.params = params;
	}

	public Apply(Object param) {
		this(new Object[] { param });
	}

	/**
	 *
	 * @param interName
	 * @param methodName
	 * @param types
	 * @param params
	 */
	public Apply(String interName, String methodName, Class<?>[] types, Object[] params) {
		this();
		this.interfaceName = interName;
		this.methodName = methodName;
		this.paramTypes = types;
		this.params = params;
	}

	public String getInterfaceName() {
		return this.interfaceName;
	}

	public String getMethodName() {
		return this.methodName;
	}

	public Class<?>[] getParameterTypes() {
		return this.paramTypes;
	}

	public Object[] getParameters() {
		return this.params;
	}

	/**
	 * object to byte array
	 * @return
	 */
	public byte[] build() {
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream(1024 * 5);
			ObjectOutputStream oout = new ObjectOutputStream(bout);
			oout.writeObject(this);
			oout.close();
			return bout.toByteArray();
		} catch (IOException exp) {
			exp.printStackTrace();
			//Logger.error("Request.build", exp);
		} catch (Throwable exp) {
			exp.printStackTrace();
			//Logger.error("Request.build", exp);
		}
		return null;
	}

	/**
	 * change to bytes
	 * @param obj
	 * @return
	 */
	public static byte[] build(Object obj) {
		Apply apply = new Apply(obj);
		return apply.build();
	}

	/**
	 * change to bytes
	 * @param objs
	 * @return
	 */
	public static byte[] build(Object[] objs) {
		Apply apply = new Apply(objs);
		return apply.build();
	}

	/**
	 * byte array to object
	 *
	 * @param b
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Apply resolve(byte[] b) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bin = new ByteArrayInputStream(b);
		ObjectInputStream oin = new ObjectInputStream(bin);
		Apply apply = (Apply) oin.readObject();
		oin.close();
		bin.close();
		return apply;
	}

}