package com.lexst.live.window.query;


public final class StructHead {

//	public static final int FIRST_COLUMN = 0;
//
//	public static final int KEY = 0;
//	public static final int USER = 1;
//	public static final int TYPE = 2;
//	public static final int SIZE = 3;
//	public static final int CASE = 4;	//忽略大小写
//	public static final int NULL = 5;	//是否允许空值
//	public static final int DEFVALUE = 6;	//如果是数字,必须有一个默认值
//	public static final int INDEXSIZE = 7;
//
//	public static final int INDEX_MODE = 8;
//	public static final int BIGINDEX_UNITS = 9;
//	public static final int INDEX2_UNITS = 10;

	private int columnIndex = -1;	//与上面固定值对应
	private String columnName = "";
	private boolean editable;

	public StructHead() {
		super();
	}

	public StructHead(int columnIndex, String name, boolean editable) {
		this();
		this.setColumnName(name);
		this.setColumnIndex(columnIndex);
		this.setEditable(editable);
	}

	public void setColumnName(String name) {
		if (name != null) {
			this.columnName = name.trim();
		}
	}

	public String getColumnName() {
		return this.columnName;
	}

	public void setColumnIndex(int index) {
		if (index >= 0) {
			this.columnIndex = index;
		}
	}

	public int getColumnIndex() {
		return this.columnIndex;
	}

	public void setEditable(boolean f) {
		this.editable = f;
	}
	public boolean isEditable() {
		return this.editable;
	}

	public boolean equals(Object obj) {
//		if(obj instanceof UserElement) {
//			return this.columnIndex == ((UserElement)obj).getColumnIndex();
//		}
		return false;
	}

	public int hashCode() {
		return this.columnIndex;
	}
}