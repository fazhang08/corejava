/**
 *
 */
package com.lexst.live.window.query;

import java.util.ArrayList;

import javax.swing.table.DefaultTableModel;

public class StructModel extends DefaultTableModel {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private ArrayList<StructHead> head = new ArrayList<StructHead>();
	private ArrayList<StructData> data = new ArrayList<StructData>();

	/**
	 *
	 */
	public StructModel() {
		super();
//		loadColumns();
	}


//	private void loadColumns() {
//		for (int i = 0; i < 10; i++) {
//			String name = String.format("Key %d", i);
//			head.add(new StructHead(i, name, true));
//		}
//
//		for (StructHead obj : head) {
//			addColumn(obj.getColumnName());
//		}
//	}

	public void loadColumn(String[] name) {
		head.clear();
		for (int i = 0; name != null && i < name.length; i++) {
			head.add(new StructHead(i, name[i], true));
		}
		this.setColumnCount( name.length );
		for (StructHead obj : head) {
			addColumn(obj.getColumnName());
		}
//		System.out.printf("column size:%d , head size:%d\n", this.getColumnCount(), head.size() );
	}

	public void clear() {
		int rowCount = this.getRowCount();
		for (int index = 0; index < rowCount; index++) {
			removeRow(0);
		}
	}

	public void removeRow(int index) {
		if (index >= 0 && index < data.size()) {
			data.remove(index);
		}
		super.removeRow(index);
	}

	public String getColumnName(int column) {
		if (column < 0 || column >= head.size()) {
			return null;
		}
		StructHead node = head.get(column);
		return node.getColumnName();
	}

	public Class<?> getColumnClass(int index) {
		if(data.isEmpty()) return null;
		StructData sd = data.get(0);
		return sd.getColumnClass(index);
	}

	public void addRow(Object[] s) {
		StructData sd = new StructData(s);
		data.add(sd);
		super.addRow(s);
	}

	public Object getValueAt(int row, int col) {
		if (row < 0 || row >= data.size()) {
			return null;
		}
		StructData sd = data.get(row);
		return sd.getValutAt(col);
	}

	public void setValueAt(Object value, int row, int column) {
		if (row < 0 || row >= data.size()) {
			return;
		}

		StructData sd = data.get(row);
		sd.setValue(column, value);
		super.setValueAt(value, row, column );
	}
}
