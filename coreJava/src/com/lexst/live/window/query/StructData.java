package com.lexst.live.window.query;

import java.io.Serializable;
import java.util.ArrayList;

public class StructData implements Serializable {
	private static final long serialVersionUID = 1L;

	private ArrayList<Object> a = new ArrayList<Object>();

	public StructData() {
		super();
	}

	public StructData(Object[] all) {
		this();
		this.addAll(all);
	}

	public void addAll(Object[] s) {
		for (Object obj : s) {
			a.add(obj);
		}
	}

	public Object[] getObjects() {
		Object[] fs = new Object[a.size()];
		return a.toArray(fs);
	}

	public Object getValutAt(int i) {
		if (i < 0 || i >= a.size())
			return null;
		return a.get(i);
	}

	public Class<?> getColumnClass(int i) {
		if (i < 0 || i >= a.size()) return null;
		return a.get(i).getClass();
	}

	public boolean setValue(int i, Object s) {
		if (i < 0 || i >= a.size()) {
			return false;
		}
		a.set(i, s);
		return true;
	}
}