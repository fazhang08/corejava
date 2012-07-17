/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com. All rights reserved
 * 
 * this file is part of lexst
 * 
 * @author zhicheng.liang lexst@126.com
 * 
 * @version 1.0 12/1/2009
 * 
 * @see com.lexst.invoke.impl
 * 
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.invoke.impl;

import java.lang.reflect.*;
import java.util.*;

import com.lexst.invoke.*;
import com.lexst.log.client.*;
import com.lexst.remote.*;
import com.lexst.visit.*;

public class RPCInvokerImpl implements RPCInvoker {
	
	/* interface name -> object instance */
	private Map<String, Object> mapInstance;

	/**
	 *
	 */
	public RPCInvokerImpl() {
		super();
		mapInstance = new HashMap<String, Object>();
	}

	/**
	 * add a visit class
	 * @param cls
	 * @return
	 */
	public boolean addInstance(Class<?> cls) {
		int count = 0;
		try {
			Object instance = cls.newInstance();
			Class<?>[] subs = cls.getInterfaces();
			for (int i = 0; subs != null && i < subs.length; i++) {
				if (!isVisit(subs[i])) {
					continue;
				}
				String name = subs[i].getName(); //接口名称
				mapInstance.put(name, instance);
				count++;
			}
		} catch (InstantiationException exp) {
			Logger.error(exp);
		} catch (IllegalAccessException exp) {
			Logger.error(exp);
		}
		return count > 0;
	}

	/**
	 * delete a class instance
	 * @param cls
	 * @return
	 */
	public boolean deleteInstance(Class<?> cls) {
		int count = 0;
		Class<?>[] cs = cls.getInterfaces();
		for (int i = 0; cs != null && i < cs.length; i++) {
			if (!isVisit(cs[i])) continue;
			String name = cs[i].getName();
			if (mapInstance.remove(name) != null) {
				count++;
			}
		}
		return count > 0;
	}

	/**
	 * check class
	 * @param cls
	 * @return
	 */
	private boolean isVisit(Class<?> cls) {
		if (cls == Visit.class) return true;
		Class<?>[] cs = cls.getInterfaces();
		for (int i = 0; cs != null && i < cs.length; i++) {
			if (this.isVisit(cs[i]))
				return true;
		}
		return false;
	}

	/**
	 * rpc invoke
	 * @param apply
	 * @return
	 */
	@Override
	public Reply invoke(Apply apply) {
		if (apply == null) {
			return new Reply(null, new NullPointerException("null point request!"));
		}
		String interfaceName = apply.getInterfaceName();
		String methodName = apply.getMethodName();
		Class<?>[] paramTypes = apply.getParameterTypes();
		Object[] params = apply.getParameters();

		Reply reply = new Reply();

		Object instance = mapInstance.get(interfaceName);
		if (instance == null) {
			Logger.error("RPCInvokerImpl.invoke, cannot find [%s]", interfaceName);
			reply.setThrowable(new ClassNotFoundException("cannot find class!"));
			return reply;
		}

		try {
			Method method = instance.getClass().getMethod(methodName, paramTypes);
			Object reobj = method.invoke(instance, params);
			reply.setObject(reobj);
		} catch (IllegalAccessException exp) {
			exp.printStackTrace();
			reply.setThrowable(exp);
		} catch (IllegalArgumentException exp) {
			exp.printStackTrace();
			reply.setThrowable(exp);
		} catch (InvocationTargetException exp) {
			exp.printStackTrace();
			reply.setThrowable(exp);
		} catch (SecurityException exp) {
			exp.printStackTrace();
			reply.setThrowable(exp);
		} catch (NoSuchMethodException exp) {
			exp.printStackTrace();
			reply.setThrowable(exp);
		} catch (Throwable exp) {
			exp.printStackTrace();
			reply.setThrowable(exp);
		}
		return reply;
	}

}