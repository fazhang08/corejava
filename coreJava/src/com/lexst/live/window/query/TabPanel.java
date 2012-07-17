/**
 * 
 */
package com.lexst.live.window.query;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;

import com.lexst.db.*;
import com.lexst.db.charset.*;
import com.lexst.db.column.*;
import com.lexst.db.field.*;
import com.lexst.db.row.*;
import com.lexst.db.schema.*;
import com.lexst.log.client.*;

public class TabPanel extends JPanel implements ChangeListener {

	private static final long serialVersionUID = 1L;
	
	private JTabbedPane tabPanel = new JTabbedPane();
	
	private JTextArea txtTip = new JTextArea();
	
	private StructModel model = new StructModel();
	private JTable table = new JTable();

	private LogPanel log = new LogPanel();
	
	/**
	 * 
	 */
	public TabPanel() {
		super();
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	@Override
	public void stateChanged(ChangeEvent arg) {
		
	}
	
	public LogPrinter getLogPrinter() {
		return log;
	}
	
	private JScrollPane initTip() {
		Font f1 = txtTip.getFont();
		Font font = new Font(f1.getName(), f1.getStyle(), f1.getSize() + 4);

		font = new Font(Font.DIALOG_INPUT, f1.getStyle(), f1.getSize() + 2);
		txtTip.setFont(font);
		txtTip.setEditable(false);
		txtTip.setToolTipText("SQL Execute Report");
		txtTip.setBorder(new EmptyBorder(2, 2, 2, 2));
		
		return new JScrollPane(txtTip);
	}

	private JScrollPane initSelect() {
		table.setModel(model);
		table.setRowHeight(23);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setRowSelectionAllowed(true);
		table.setShowGrid(true);
		table.getTableHeader().setReorderingAllowed(false);
		table.setSelectionMode(DefaultListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setIntercellSpacing(new Dimension(3, 3));
		table.setColumnSelectionAllowed(true);
		table.setSurrendersFocusOnKeystroke(true);

		TableColumnModel columnModel = table.getColumnModel();
		int count = columnModel.getColumnCount();
		for (int n = 0; n < count; n++) {
			columnModel.getColumn(n).setPreferredWidth(120);
		}

		String title = "SQL Query Result";
		table.setToolTipText(title);
		table.setBorder(new EmptyBorder(2, 2, 2, 2));
		
		return new JScrollPane(table);
	}
	
	public void init() {
		JScrollPane tip = this.initTip();
		JScrollPane tab = this.initSelect();
		log.init();

		tabPanel.addChangeListener(this);
		tabPanel.addTab("Report", tip);
		tabPanel.addTab("Query", tab);
		tabPanel.addTab("Logs", log);
		tabPanel.setBorder(new EmptyBorder(1, 1, 1, 1));
		
		this.setLayout(new BorderLayout(5, 5));
		this.add(tabPanel, BorderLayout.CENTER);
	}

	public void showFault(String s) {
		txtTip.setForeground(Color.red);
		txtTip.setText(s);
		
		tabPanel.setSelectedIndex(0);
	}

	public void showFault(String format, Object... args) {
		String s = String.format(format, args);
		showFault(s);
	}

	public void showMessage(String s) {
		txtTip.setForeground(new Color(0, 128, 64));
		txtTip.setText(s);
		tabPanel.setSelectedIndex(0);
	}

	public void showMessage(String format, Object... args) {
		String s = String.format(format, args);
		showMessage(s);
	}
	
	public void deleteTip() {
		if (!txtTip.getText().isEmpty()) {
			txtTip.setText("");
		}
	}

	public void clearItems() {
		model.clear();
	}

	public void clearTable() {
		this.clearItems();
		TableColumnModel m = table.getColumnModel();
		int count = m.getColumnCount();
		for (int i = 0; i < count; i++) {
			m.removeColumn(m.getColumn(0));
		}
	}

	public void updateTable(String[] cols) {
		// clear table
		clearTable();
		// reload column
		for (int index = 0; index < cols.length; index++) {
			TableColumn col = new TableColumn(index);
			col.setHeaderValue(cols[index]);
			table.addColumn(col);
		}
	}

	public void updateTable(Table head) {
		this.clearTable();
		int index = 0;
		for (short columnId : head.idSet()) {
			Field field = head.find(columnId);
			int width = 30;
			switch (field.getType()) {
			case Type.RAW:
			case Type.CHAR:
			case Type.NCHAR:
			case Type.WCHAR:
			case Type.TIMESTAMP:
				width = 130;
				break;
			case Type.SHORT:
			case Type.INTEGER:
			case Type.LONG:
				width = 85;
				break;
			case Type.REAL:
			case Type.DOUBLE:
			case Type.DATE:
			case Type.TIME:
				width = 100;
				break;
			}
			TableColumn col = new TableColumn(index);
			col.setHeaderValue(field.getName());
			table.addColumn(col);
			table.getColumnModel().getColumn(index).setPreferredWidth(width);
			index++;
		}
	}
	
	private String toHex(byte[] b, int limit) {
		StringBuilder buff = new StringBuilder();
		for (int i = 0; i < b.length; i++) {
			String s = String.format("%X", b[i] & 0xff);
			if (s.length() == 1) s = "0" + s;
			if (buff.length() == 0) buff.append("0x");
			buff.append(s);
			if (i + 1 >= limit) {
				if (i + 1 < b.length) buff.append("..");
				break;
			}
		}
		return buff.toString();
	}

	public void focusItem() {
		tabPanel.setSelectedIndex(1);
	}

	public void focusMessage() {
		tabPanel.setSelectedIndex(0);
	}
	
	/**
	 * @param table
	 * @param row
	 */
	public void addItem(SQLCharset set, Table table, Row row) {
		int select = tabPanel.getSelectedIndex();
		if (select != 1) tabPanel.setSelectedIndex(1);

		String[] s = new String[table.size()];
		int index = 0;
		for(short columnId : table.idSet()) {
			String value = null;
			Column col = row.get(columnId);
			
			if (col.isRaw()) {
				byte[] b = ((Raw) col).getValue();
				if (b != null && b.length > 0) {
					value = toHex(b, 16);
				}
			} else if (col.isChar()) {
				byte[] b = ((Char) col).getValue();
				if (set != null && b != null && b.length > 0) {
					value = set.getChar().decode(b);
				}
			} else if (col.isNChar()) {
				byte[] b = ((NChar) col).getValue();
				if (set != null && b != null && b.length > 0) {
					value = set.getNChar().decode(b);
				}
			} else if (col.isWChar()) {
				byte[] b = ((WChar) col).getValue();
				if (set != null && b != null && b.length > 0) {
					value = set.getWChar().decode(b);
				}
			} else if (col.isShort()) {
				value = String.format("%d", ((com.lexst.db.column.Short) col).getValue());
			} else if (col.isInteger()) {
				value = String.format("%d", ((com.lexst.db.column.Integer) col).getValue());
			} else if (col.isLong()) {
				value = String.format("%d", ((com.lexst.db.column.Long) col).getValue());
			} else if (col.isReal()) {
				value = String.format("%f", ((com.lexst.db.column.Real) col).getValue());
			} else if (col.isDouble()) {
				value = String.format("%f", ((com.lexst.db.column.Double) col).getValue());
			} else if (col.isDate()) {
				int num = ((com.lexst.db.column.Date) col).getValue();
				java.util.Date date = com.lexst.util.datetime.SimpleDate.format(num);
				java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
				value = sdf.format(date);
			} else if (col.isTime()) {
				int num = ((com.lexst.db.column.Time) col).getValue();
				java.util.Date date = com.lexst.util.datetime.SimpleTime.format(num);
				java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss SSS");
				value = sdf.format(date);
			} else if (col.isTimestamp()) {
				long num = ((com.lexst.db.column.TimeStamp)col).getValue();
				java.util.Date date = com.lexst.util.datetime.SimpleTimeStamp.format(num);
				java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
				value = sdf.format(date);
			}
			if (value == null) value = "";
			s[index++] = value;
		}
		model.addRow(s);
	}

	public void addItem(Row row) {
		String[] s = new String[row.size()];
		int index = 0;
		for (short columnId : row.keySet()) {
			String value = null;
			Column col = row.get(columnId);
			if (col.isRaw()) {

			} else if (col.isChar()) {
				value = new String(((Char) col).getValue());
			} else if (col.isNChar()) {

			} else if (col.isWChar()) {

			} else if (col.isShort()) {
				value = String.format("%d", ((com.lexst.db.column.Short) col).getValue());
			} else if (col.isInteger()) {
				value = String.format("%d", ((com.lexst.db.column.Integer) col).getValue());
			} else if (col.isLong()) {
				value = String.format("%d", ((com.lexst.db.column.Long) col).getValue());
			} else if (col.isReal()) {
				value = String.format("%f", ((com.lexst.db.column.Real) col).getValue());
			} else if (col.isDouble()) {
				value = String.format("%f", ((com.lexst.db.column.Double) col).getValue());
			} else if (col.isDate()) {
				int num = ((com.lexst.db.column.Date) col).getValue();
				java.util.Date date = com.lexst.util.datetime.SimpleDate.format(num);
				java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
				value = sdf.format(date);
			} else if (col.isTime()) {
				int num = ((com.lexst.db.column.Time) col).getValue();
				java.util.Date date = com.lexst.util.datetime.SimpleTime.format(num);
				java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss SSS");
				value = sdf.format(date);
			} else if (col.isTimestamp()) {
				long num = ((com.lexst.db.column.TimeStamp)col).getValue();
				java.util.Date date = com.lexst.util.datetime.SimpleTimeStamp.format(num);
				java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
				value = sdf.format(date);
			}
			if (value == null) value = "";
			s[index++] = value;
		}
		model.addRow(s);
	}

//	public void reload() {
//		int size = 5;
//		String[] heads = new String[size];
//		for(int i = 0; i<size; i++) {
//			heads[i] = String.format("Key %d", i+1);
//		}
//		this.updateTable(heads);
//
//		Row row = new Row();
//		for (short i = 0; i < size; i++) {
//			//row.add(new Char(i, "abc"));
//		}
//		for (int i = 0; i < 1000; i++) {
//			this.addItem(row);
//		}
//	}

}