/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2010 lexst.com. All rights reserved
 * 
 * naming object (string value)
 * 
 * @author scott.jian lexst@126.com
 * 
 * @version 1.0 1/20/2010
 * 
 * @see com.lexst.util.naming
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.util.naming;

import java.io.*;

public class Naming implements Serializable, Comparable<Naming> {
	
	private static final long serialVersionUID = 264011252802994522L;
	
	/* object name */
	private String name;
	/* hash value */
	private int hash;

	/**
	 * 
	 */
	public Naming() {
		super();
		name = "";
		hash = 0;
	}

	/**
	 * @param name
	 */
	public Naming(String name) {
		this();
		this.set(name);
	}
	
	/**
	 * @param object
	 */
	public Naming(Naming object) {
		this();
		name = object.name;
		hash = object.hash;
	}

	public void set(String s) {
		if (s == null) {
			name = "";
			hash = 0;
		} else {
			name = s.trim();
			hash = name.toLowerCase().hashCode();
		}
	}

	public String get() {
		return name;
	}

	public String toString() {
		return this.name;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object arg) {
		if (arg == null || arg.getClass() != Naming.class) {
			return false;
		} else if (arg == this) {
			return true;
		}
		Naming obj = (Naming) arg;
		return name != null && name.equalsIgnoreCase(obj.name);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return hash;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Naming obj) {
		return (hash < obj.hash ? -1 : (hash > obj.hash ? 1 : 0));
	}
}