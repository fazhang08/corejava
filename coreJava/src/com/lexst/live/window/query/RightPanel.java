/**
 *
 */
package com.lexst.live.window.query;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.*;

import com.lexst.algorithm.collect.*;
import com.lexst.db.account.*;
import com.lexst.db.charset.*;
import com.lexst.db.field.*;
import com.lexst.db.row.*;
import com.lexst.db.schema.*;
import com.lexst.db.statement.*;
import com.lexst.live.*;
import com.lexst.live.pool.*;
import com.lexst.live.window.*;
import com.lexst.live.window.util.*;
import com.lexst.log.client.*;
import com.lexst.site.live.*;
import com.lexst.sql.*;
import com.lexst.util.*;
import com.lexst.util.host.*;
import com.lexst.visit.*;

public class RightPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;

	private JButton cmdCheck = new JButton();
	private JButton cmdGo = new JButton();
	private JButton cmdFont = new JButton();
	private JButton cmdCut = new JButton();
	private JButton cmdClear = new JButton();

	private JToolBar toolbar = new JToolBar();

	private SQLTextPane txtSQL = new SQLTextPane();
	private TabPanel controls = new TabPanel();

	private SQLParser parser = new SQLParser();
	private SQLChecker checker = new SQLChecker();

	/**
	 *
	 */
	public RightPanel() {
		super();
	}

	public class SQLKeyAdapter extends KeyAdapter {
		public void keyPressed(KeyEvent e) {
			if (e.getSource() == txtSQL) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_F5:
					check();
					break;
				case KeyEvent.VK_F6:
					execute();
					break;
				}
			}
		}
	}
	
	public LogPrinter getLogPrinter() {
		return controls.getLogPrinter();
	}

	private void selectFont() {
		Font defont = txtSQL.getFont();
		Font font = FontSelectDialog.showDialog(Launcher.getInstance().getFrame(), true, defont);
		if (font != null) {
			txtSQL.setFont(font);
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == cmdCheck) {
			this.check();
		} else if (e.getSource() == cmdGo) {
			this.execute();
		} else if (e.getSource() == cmdFont) {
			this.selectFont();
		} else if (e.getSource() == cmdCut) {
			controls.deleteTip();
		} else if (e.getSource() == cmdClear) {
			controls.clearTable();
		}
	}

	/**
	 * split mulit sql text
	 * @param sql
	 * @return
	 */
	private String[] split(String sql) {
		BufferedReader b = new BufferedReader(new StringReader(sql));
		ArrayList<String> a = new ArrayList<String>();
		while (true) {
			try {
				String line = b.readLine();
				if (line == null) break;
				line = line.trim();
				if (!line.isEmpty()) a.add(line);
			} catch (IOException exp) {
				break;
			}
		}
		if(a.isEmpty()) return null;
		String[] all = new String[a.size()];
		return a.toArray(all);
	}

	private String[] getSQL() {
		String text = txtSQL.getText().trim();
		if (!text.isEmpty()) {
			// split sql
			return split(text);
		}
		return null;
	}

	/**
	 * check sql syntax
	 * @param sql
	 */
	private void checkSingle(String sql) {
		if("HELP".equalsIgnoreCase(sql)) {
			controls.showMessage("correct syntax");
			return;
		}

		boolean success = false;
		try {
			success = checker.isShowCharset(sql);
			if (!success) success = checker.isCreateSchema(TopPool.getInstance().getSQLCharMap(), sql);
			if (!success) success = checker.isDeleteSchema(sql);
			if (!success) success = checker.isCreateUser(sql);
			if (!success) success = checker.isDropUser(sql);
			if (!success) success = checker.isDropSHA1User(sql);
			if (!success) success = checker.isAlterUser(sql);
			if (!success) success = checker.isGrant(sql);
			if (!success) success = checker.isRevoke(sql);
			if (!success) success = checker.isDeleteTable(sql);
			if (!success) success = checker.isSetChunkSize(sql);
			if (!success) success = checker.isSetOptimizeTime(sql);

			if (!success) success = checker.isLoadIndex(sql);
			if (!success) success = checker.isStopIndex(sql);
			if (!success) success = checker.isLoadChunk(sql);
			if (!success) success = checker.isStopChunk(sql);
			if (!success) success = checker.isLoadOptimize(sql);
			if (!success) success = checker.isBuildTask(sql);
			if (!success) success = checker.isShowSite(sql);
			if (!success) success = checker.isSetCollectPath(sql);
			if (!success) success = checker.isTestCollectPath(sql);
			
			// check "select"
			if (!success) {
				success = checker.isSelectPattern(sql);
				if(success) {
					Space space = checker.getSelectSpace(sql);
					// find a table
					SQLCharset set = TopPool.getInstance().findCharset(space.getSchema());
					if(set == null) {
						throw new SQLSyntaxException("cannot find database charset!");
					}
					Table table = TopPool.getInstance().findTable(space);
					if(table == null) {
						throw new SQLSyntaxException("cannot find table space configure");
					}
					success = checker.isSelect(set, table, sql);
					if(!success) {
						throw new SQLSyntaxException("invalid select syntax");
					}
				}
			}
			// check "delete from"
			if (!success) {
				success = checker.isDeletePattern(sql);
				if(success) {
					Space space = checker.getDeleteSpace(sql);
					SQLCharset set = TopPool.getInstance().findCharset(space.getSchema());
					Table table = TopPool.getInstance().findTable(space);
					success = checker.isDelete(set, table, sql);
					if(!success) {
						throw new SQLSyntaxException("invalid delete syntax");
					}
				}
			}
			// check "insert into"
			if(!success) {
				success = checker.isInsertPattern(sql);
				if(success) {
					Space space = checker.getInsertSpace(sql);
					SQLCharset set = TopPool.getInstance().findCharset(space.getSchema());
					if (set == null) {
						throw new SQLSyntaxException("cannot find '%s' database", space.getSchema());
					}
					Table table = TopPool.getInstance().findTable(space);
					if (table == null) {
						throw new SQLSyntaxException("cannot find '%s' table", space);
					}
					success = checker.isInsert(set, table, sql);
					if(!success) {
						throw new SQLSyntaxException("invalid insert syntax");
					}
				}
			}
			// check "inject into"
			if(!success) {
				success = checker.isInjectPattern(sql);
				if(success) {
					Space space = checker.getInjectSpace(sql);
					SQLCharset set = TopPool.getInstance().findCharset(space.getSchema());
					if (set == null) {
						throw new SQLSyntaxException("cannot find '%s' database", space.getSchema());
					}
					Table table = TopPool.getInstance().findTable(space);
					if (table == null) {
						throw new SQLSyntaxException("cannot find '%s' table", space);
					}
					success = checker.isInject(set, table, sql);
					if(!success) {
						throw new SQLSyntaxException("invalid inject syntax");
					}
				}
			}
			// check update
			if (!success) {
				success = checker.isUpdate(sql);
				if(success) {
					Space space = checker.getUpdateSpace(sql);
					SQLCharset set = TopPool.getInstance().findCharset(space.getSchema());
					Table table = TopPool.getInstance().findTable(space);
					success = checker.isUpdate(set, table, sql);
					if (!success) {
						throw new SQLSyntaxException("invalid update syntax");
					}
				}
			}
			// check dc
			if(!success) {
				success = checker.isDC(sql);
				if(success) {
					SQLCharset set = null; Table table = null;
					Space space = checker.getDiffuseSpace(sql);
					if (space != null) {
						set = TopPool.getInstance().findCharset(space.getSchema());
						if(set == null) {
							throw new SQLSyntaxException("cannot find charset: '%s'", space.getSchema());
						}
						table = TopPool.getInstance().findTable(space);
						if(table == null) {
							throw new SQLSyntaxException("cannot find table: '%s'", space);
						}
					}
					success = checker.isDC(set, table, sql);
					if(!success) {
						throw new SQLSyntaxException("invalid dc syntax");
					}
				}
			}
			// check adc
			if(!success) {
				success = checker.isADC(sql);
				if (success) {
					SQLCharset set = null; Table table = null;
					Space space = checker.getDiffuseSpace(sql);
					if (space != null) {
						set = TopPool.getInstance().findCharset(space.getSchema());
						if(set == null) {
							throw new SQLSyntaxException("cannot find charset: '%s'", space.getSchema());
						}
						table = TopPool.getInstance().findTable(space);
						if(table == null) {
							throw new SQLSyntaxException("cannot find table: '%s'", space);
						}
					}
					success = checker.isADC(set, table, sql);
					if (!success) {
						throw new SQLSyntaxException("invalid dc syntax");
					}
				}
			}
		} catch (SQLSyntaxException exp) {
			String msg = exp.getMessage();
			controls.showFault(msg);
			return;
		}
		if (success) {
			controls.showMessage("correct syntax");
		} else {
			controls.showFault("incorrect syntax");
		}
	}

	private void showCharset(String sql) {
		String db = parser.splitCharSet(sql);
		SQLCharmap map = null;
		if(db == null) {
			map = TopPool.getInstance().getSQLCharMap();
		} else {
			SQLCharset set = TopPool.getInstance().findCharset(db);
			if(set == null) {
				controls.showFault(String.format("cannot find charset by \'%s\' ", db));
				return;
			}

			map = new SQLCharmap();
			String[] allcls = { set.getChar().getClass().getName(),
					set.getNChar().getClass().getName(),
					set.getWChar().getClass().getName() };

			// get char class
			SQLCharmap all = TopPool.getInstance().getSQLCharMap();
			for (String clsname : allcls) {
				SQLCharType mode = all.findClass(clsname);
				if (mode == null) {
					controls.showFault("cannot find \'%s\'", clsname);
					return;
				}
				map.add(mode);
			}
		}

		String[] heads  = {"Char Name", "Type", "Class Name"};
		controls.updateTable(heads);
		for(String name : map.keys()) {
			SQLCharType mode = map.find(name);
			com.lexst.db.column.Char charname = new com.lexst.db.column.Char((short)1, mode.getName().getBytes() );
			com.lexst.db.column.Char type = new com.lexst.db.column.Char((short)2, mode.toTypeString().getBytes() );
			com.lexst.db.column.Char clsname = new com.lexst.db.column.Char((short)3, mode.getClassName().getBytes());

			Row row = new Row();
			row.add(charname);
			row.add(type);
			row.add(clsname);
			controls.addItem(row);
		}
	}

	private void createSchema(String sql) {
		LiveSite local = Launcher.getInstance().getLocal();
		SQLCharmap map = TopPool.getInstance().getSQLCharMap();
		Schema db = parser.splitCreateSchema(map, sql);
		boolean success = TopPool.getInstance().createSchema(null, local, db);
		if (success) {
			controls.showMessage("create '%s' success", db.getName());
			Launcher.getInstance().getFrame().refresh();
		} else {
			controls.showFault("cannot create database");
		}
	}

	private void deleteSchema(String sql) {
		LiveSite local = Launcher.getInstance().getLocal();
		Schema db = parser.splitDropSchema(sql);
		boolean success = TopPool.getInstance().deleteSchema(null, local, db.getName());
		if(success) {
			controls.showMessage("drop '%s' success", db.getName());
			Launcher.getInstance().getFrame().refresh();
		} else {
			controls.showFault("cannot drop '%s' database");
		}
	}

	/**
	 * create a user configure
	 * @param sql
	 */
	private void createUser(String sql) {
		LiveSite local = Launcher.getInstance().getLocal();
		User user = parser.splitCreateUser(sql);
		boolean success = TopPool.getInstance().createUser(null, local, user);
		if (success) {
			controls.showMessage("create user success");
		} else {
			controls.showFault("cannot create user");
		}
	}
	
	/**
	 * delete a user configure
	 * @param sql
	 */
	private void deleteUser(String sql) {
		LiveSite local = Launcher.getInstance().getLocal();
		User user = parser.splitDropUser(sql);
		boolean success = TopPool.getInstance().deleteUser(null, local, user.getHexUsername());
		if (success) {
			controls.showMessage("drop user success");
		} else {
			controls.showFault("cannot drop user");
		}
	}
	
	/**
	 * delete a user configure
	 * @param sql
	 */
	private void deleteSHA1User(String sql) {
		LiveSite local = Launcher.getInstance().getLocal();
		User user = parser.splitDropSHA1User(sql);
		boolean success = TopPool.getInstance().deleteUser(null, local, user.getHexUsername());
		if (success) {
			controls.showMessage("drop user success");
		} else {
			controls.showFault("cannot drop user");
		}
	}
	

	private void alterUser(String sql) {
		LiveSite local = Launcher.getInstance().getLocal();
		User user = parser.splitAlterUser(sql);
		boolean success = TopPool.getInstance().alterUser(null, local, user);
		if (success) {
			controls.showMessage("alter '%s' success", user.getHexUsername());
		} else {
			controls.showFault("cannot alter '%s' user", user.getHexUsername());
		}
	}

	private void grant(String sql) {
		LiveSite local = Launcher.getInstance().getLocal();
		Permit permit = parser.splitGrant(sql);
		boolean success = TopPool.getInstance().addPermit(null, local, permit);
		if(success) {
			controls.showMessage("grant success");
		} else {
			controls.showFault("grant fault");
		}
	}

	private void revoke(String sql) {
		LiveSite local = Launcher.getInstance().getLocal();
		Permit permit = parser.splitRevoke(sql);
		boolean success = TopPool.getInstance().deletePermit(null, local, permit);
		if(success) {
			controls.showMessage("revoke success");
		} else {
			controls.showFault("revoke fault");
		}
	}
	
	private void setChunkSize(String sql) {
		Object[] objects = parser.splitSetChunkSize(sql);
		Space space = (Space) objects[0];
		Integer size = (Integer) objects[1];
		
		if (!TopPool.getInstance().existsTable(space)) {
			controls.showFault("Cannot find " + space);
			return;
		}
		int limit = 1024 * 1024 * 1024;
		if (size > limit) {
			controls.showFault("chunk size is too large");
			return;
		}
		
		LiveSite local = Launcher.getInstance().getLocal();
		boolean success = TopPool.getInstance().setChunkSize(null, local, space, size.intValue());
		if(success) {
			controls.showMessage("set success");
		} else {
			controls.showFault("set fault");
		}
	}
	
	private void setOptimizeTime(String sql) {
		Object[] objects = parser.splitSetOptimizeTime(sql);
		Space space = (Space)objects[0];
		int type = ((Integer)objects[1]).intValue();
		long time = ((Long)objects[2]).longValue();
		
		if(!TopPool.getInstance().existsTable(space)) {
			controls.showFault("cannot find '%s'", space);
			return;
		}
		
		LiveSite local = Launcher.getInstance().getLocal();
		boolean success = TopPool.getInstance().setOptimizeTime(null, local, space, type, time);
		if (success) {
			controls.showMessage("set success");
		} else {
			controls.showFault("set fault");
		}
	}

	/**
	 * create sql table
	 * @param sqlTable
	 * @param sqlIndex
	 */
	private void createTable(String sqlTable, String sqlIndex, String sqlLayout) {
		LiveSite local = Launcher.getInstance().getLocal();
		Table table = parser.splitCreateTable(sqlTable, sqlIndex, sqlLayout);
		boolean success = TopPool.getInstance().createTable(null, local, table);
		if (success) {
			controls.showMessage("create '%s' success", table.getSpace());
			Launcher.getInstance().getFrame().refresh();
		} else {
			controls.showFault("cannot create '%s'", table.getSpace());
		}
	}

	/**
	 * delete sql table
	 * @param sql
	 * @return
	 */
	private void deleteTable(String sql) {
		LiveSite local = Launcher.getInstance().getLocal();
		Space space = parser.splitDropTable(sql);
		if (space == null) {
			return;
		}
		boolean success = TopPool.getInstance().deleteTable(null, local, space);
		if (success) {
			controls.showMessage("delete '%s' success", space);
			Launcher.getInstance().getFrame().refresh();
		} else {
			controls.showFault("cannot delete '%s'", space);
		}
	}

	/**
	 * query data
	 * @param sql
	 */
	private void select(String sql) {
		Space space = checker.getSelectSpace(sql);
		// find charset
		SQLCharset set = TopPool.getInstance().findCharset(space.getSchema());
		// find a table
		Table table = TopPool.getInstance().findTable(space);
		// split to select object
		Select select = parser.splitSelect(set, table, sql);

		SiteHost top = TopPool.getInstance().getRemote();
		LiveSite local = Launcher.getInstance().getLocal();

		// select
		short[] colIds = select.getSelectId();
		Table head = new Table();
		if (colIds == null) {
			for(Field field : table.values()) {
				head.add(field);
			}
		} else {
			short id = 1;
			for (short columnId : colIds) {
				Field field = table.find(columnId);
				if(field == null) {
					throw new NullPointerException("cannot find field:" + columnId);
				}
				Field field2 = field.fresh();
				field2.setColumnId(id++);
				head.add(field2);
			}
		}
		// update table
		controls.updateTable(head);
		// show query result		
		SQLCaller caller = new SQLCaller();
		byte[] bytes = caller.select(top, local, select);
		
		this.splitShow(bytes, set, head);
	}
	
	private void dc(String sql) {
		SQLCharset charset = null;
		Table table = null;
		Space space = checker.getDiffuseSpace(sql);
		if (space != null) {
			charset = TopPool.getInstance().findCharset(space.getSchema());
			if(charset == null) {
				throw new SQLSyntaxException("cannot find charset: '%s'", space.getSchema());
			}
			table = TopPool.getInstance().findTable(space);
			if(table == null) {
				throw new SQLSyntaxException("cannot find table: '%s'", space);
			}
		}
		DC dc = parser.splitDC(sql, charset, table);
		
		SiteHost top = TopPool.getInstance().getRemote();
		LiveSite local = Launcher.getInstance().getLocal();

		Table head = null;
		Space show = dc.getCollectSpace();
		if (show != null) {
			head = TopPool.getInstance().findTable(show);
		}
		
		// find collect-task object
		CollectTask collTask = null;
		String name = dc.getCollectNaming();
		if (name != null) {
			collTask = CollectTaskPool.getInstance().find(name);
			if (collTask != null) {
				collTask.setSpace(dc.getCollectSpace());
				collTask.setWriteto(dc.getCollectWriteto());
			}
		}

		// show query result
		SQLCaller caller = new SQLCaller();
		byte[] data = caller.dc(top, local, dc);

		// show result
		if (collTask == null) {
			if (head != null) {
				controls.updateTable(head);
			}
			this.splitShow(data, charset, head);
		} else {
			Object[] params = new Object[] { charset, head, controls };
			int ret = collTask.execute(params, data);
			if(ret == 0) {
				controls.focusItem();
			} else {
				controls.focusMessage();
			}
		}
	}

	/**
	 * @param sql
	 */
	private void adc(String sql) {
		SQLCharset charset = null;
		Table table = null;
		Space space = checker.getDiffuseSpace(sql);
		if (space != null) {
			charset = TopPool.getInstance().findCharset(space.getSchema());
			if(charset == null) {
				throw new SQLSyntaxException("cannot find charset: '%s'", space.getSchema());
			}
			table = TopPool.getInstance().findTable(space);
			if(table == null) {
				throw new SQLSyntaxException("cannot find table: '%s'", space);
			}
		}
		ADC adc = parser.splitADC(sql, charset, table);
		
		SiteHost top = TopPool.getInstance().getRemote();
		LiveSite local = Launcher.getInstance().getLocal();

		Table head = null;
		Space show = adc.getCollectSpace();
		if (show != null) {
			head = TopPool.getInstance().findTable(show);
		}

		// find collect-task object
		CollectTask collTask = null;
		String name = adc.getCollectNaming();
		if (name != null) {
			collTask = CollectTaskPool.getInstance().find(name);
			if (collTask != null) {
				collTask.setSpace(adc.getCollectSpace());
				collTask.setWriteto(adc.getCollectWriteto());
			}
		}

		// show query result
		SQLCaller caller = new SQLCaller();
		byte[] data = caller.adc(top, local, adc);

		// show result
		if (collTask == null) {
			if (head != null) {
				controls.updateTable(head);
			}
			this.splitShow(data, charset, head);
		} else {
			Object[] params = new Object[] { charset, head, controls };
			collTask.execute(params, data);
		}
	}

	private void splitShow(byte[] data, SQLCharset set, Table table) {
		if(data == null) return;
		
		int off = 0, size = data.length;
		if(size < 8) return;
		int items = Numeric.toInteger(data, off, 8);
		off += 8;

		if(table == null) {
			table = new Table();
			IntegerField field = new IntegerField((short)1, "rows", 0);
			table.add(field);
			controls.updateTable(table);
			
			Row row = new Row();
			com.lexst.db.column.Integer i = new com.lexst.db.column.Integer((short)1, items);
			row.add(i);
			controls.addItem(row);
			return;
		}
		
		// split and show
		while(off < size) {
			Row row = new Row();
			int len = row.resolve(table, data, off);
			if(len < 1) break;
			off += len;
			controls.addItem(set, table, row);
		}
	}

	/**
	 * delete data
	 * @param sql
	 */
	private void delete(String sql) {
		Space space = checker.getDeleteSpace(sql);
		if(space == null) {
			controls.showFault("illegal delete syntax");
		}
		SQLCharset set = TopPool.getInstance().findCharset(space.getSchema());
		// find a table
		Table table = TopPool.getInstance().findTable(space);
		Delete delete = parser.splitDelete(set, table, sql);
		
		SiteHost top = TopPool.getInstance().getRemote();
		LiveSite local = Launcher.getInstance().getLocal();

		// show table head
		String[] head = { "count" };
		controls.updateTable(head);
		// show delete count
		SQLCaller caller = new SQLCaller();
		long count = caller.delete(top, local, delete);
		com.lexst.db.column.Long value = new com.lexst.db.column.Long((short) 1, count);
		Row row = new Row();
		row.add(value);
		controls.addItem(row);
	}

	private void insert(String sql) {
		Space space = checker.getInsertSpace(sql);
		if(space == null) {
			controls.showFault("illegal insert syntax");
			return;
		}
		SQLCharset set = TopPool.getInstance().findCharset(space.getSchema());
		Table table = TopPool.getInstance().findTable(space);
		Insert insert = parser.splitInsert(set, table, sql);
		
		SiteHost top = TopPool.getInstance().getRemote();
		LiveSite local = Launcher.getInstance().getLocal();

		// clear items
		controls.clearItems();

		SQLCaller caller = new SQLCaller();
		int count = caller.insert(top, local, insert);

		if(count > 0) {
			controls.showMessage("insert %d item", count);
		} else {
			controls.showFault("insert failed");
		}

	}

	private void inject(String sql) {
		Space space = checker.getInjectSpace(sql);
		if(space == null) {
			controls.showFault("illegal inject syntax!");
			return;
		}
		SQLCharset set = TopPool.getInstance().findCharset(space.getSchema());
		Table table = TopPool.getInstance().findTable(space);
		Inject inject = parser.splitInject(set, table, sql);
		
		SiteHost top = TopPool.getInstance().getRemote();
		LiveSite local = Launcher.getInstance().getLocal();

		// clear items
		controls.clearItems();

		SQLCaller caller = new SQLCaller();
		int count = caller.inject(top, local, inject);

		if(count > 0) {
			controls.showMessage("inject %d item", count);
		} else {
			controls.showFault("inject failed");
		}
	}
	
	private void update(String sql) {
		Space space = checker.getUpdateSpace(sql);
		if(space == null) {
			controls.showFault("illegal update syntax");
			return;
		}
		SQLCharset set = TopPool.getInstance().findCharset(space.getSchema());
		Table table = TopPool.getInstance().findTable(space);
		Update update = parser.splitUpdate(set, table, sql);
		
		SiteHost top = TopPool.getInstance().getRemote();
		LiveSite local = Launcher.getInstance().getLocal();
		
		String[] head = { "count" };
		controls.updateTable(head);
		// show update count
		SQLCaller caller = new SQLCaller();
		long count = caller.update(top, local, update);
		com.lexst.db.column.Long value = new com.lexst.db.column.Long((short) 1, count);
		Row row = new Row();
		row.add(value);
		controls.addItem(row);		
	}
	
	private void showMessage(Space space, IP[] hosts) {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("accpeted '%s'", space));
		for (int i = 0; hosts != null && i < hosts.length; i++) {
			sb.append("\n");
			sb.append(String.format("site: %s", hosts[i].toString()));
		}
		controls.showMessage(sb.toString());
	}
	
	private void showMessage(String naming, IP[] hosts) {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("accpeted '%s'", naming));
		for (int i = 0; hosts != null && i < hosts.length; i++) {
			sb.append("\n");
			sb.append(String.format("site: %s", hosts[i].toString()));
		}
		controls.showMessage(sb.toString());
	}

	public void loadOptimize(String sql) {
		SpaceHost sh = parser.splitLoadOptimize(sql);
		LiveSite local = Launcher.getInstance().getLocal();
		try {
			IP[] s = TopPool.getInstance().optimize(null, local, sh.getSpace(), sh.getAllHost());
			this.showMessage(sh.getSpace(), s);
		} catch (VisitException exp) {
			controls.showFault("cannot accepted optimize '%s'", sh.getSpace());
			Logger.error(exp);
		}
	}
	
	private void loadIndex(String sql) {
		SpaceHost sh = parser.splitLoadIndex(sql);
		LiveSite local = Launcher.getInstance().getLocal();
		try {
			IP[] s = TopPool.getInstance().loadIndex(null, local, sh.getSpace(), sh.getAllHost());
			this.showMessage(sh.getSpace(), s);
		} catch (VisitException exp) {
			controls.showFault("cannot accpeted index '%s'", sh.getSpace());
			Logger.error(exp);
		}
	}

	private void stopIndex(String sql) {
		SpaceHost sh = parser.splitStopIndex(sql);
		LiveSite local = Launcher.getInstance().getLocal();
		try {
			IP[] s = TopPool.getInstance().stopIndex(null, local, sh.getSpace(), sh.getAllHost());
			this.showMessage(sh.getSpace(), s);
		} catch (VisitException exp) {
			controls.showFault("cannot accpeted index '%s'", sh.getSpace());
			Logger.error(exp);
		}
	}
	

	
	private void loadChunk(String sql) {
		SpaceHost sh = parser.splitLoadChunk(sql);
		LiveSite local = Launcher.getInstance().getLocal();
		try {
			IP[] s = TopPool.getInstance().loadChunk(null, local, sh.getSpace(), sh.getAllHost());
			this.showMessage(sh.getSpace(), s);
		} catch (VisitException exp) {
			controls.showFault("cannot accpeted index '%s'", sh.getSpace());
			Logger.error(exp);
		}
	}
	
	private void stopChunk(String sql) {
		SpaceHost sh = parser.splitStopChunk(sql);
		LiveSite local = Launcher.getInstance().getLocal();
		try {
			IP[] s = TopPool.getInstance().stopChunk(null, local, sh.getSpace(), sh.getAllHost());
			this.showMessage(sh.getSpace(), s);
		} catch (VisitException exp) {
			controls.showFault("cannot accpeted chunk '%s'", sh.getSpace());
			Logger.error(exp);
		}
	}
	
	private void buildTask(String sql) {
		NamingHost sh = parser.splitBuildTask(sql);
		LiveSite local = Launcher.getInstance().getLocal();
		try {
			IP[] s = TopPool.getInstance().buildTask(null, local, sh.getNaming(), sh.getAllHost());
			this.showMessage(sh.getNaming(), s);
		} catch (VisitException exp) {
			controls.showFault("cannot accpeted naming task '%s'", sh.getNaming());
			Logger.error(exp);
		}
	}
	
	private void showSite(String sql) {
		LiveSite local = Launcher.getInstance().getLocal();

		Object[] params = parser.splitShowSite(sql);
		int site_type = ((Integer) params[0]).intValue();
		String from = null;
		if (params.length == 2) {
			from = (String) params[1];
		}

		SiteHost[] hosts = null;
		try {
			hosts = TopPool.getInstance().showSite(null, local, site_type, from);
		} catch (VisitException exp) {
			controls.showFault("visit error");
			Logger.error(exp);
			return;
		}

		if (hosts != null && hosts.length > 0) {
			Table head = new Table();
			head.add(new com.lexst.db.field.CharField((short) 1, "IP"));
			head.add(new com.lexst.db.field.IntegerField((short) 2, "TCP PORT", 0));
			head.add(new com.lexst.db.field.IntegerField((short) 3, "UDP PORT", 0));
			controls.updateTable(head);

			SQLCharset set = new SQLCharset();
			set.setChar(new ISO_8859_1());

			for (SiteHost host : hosts) {
				Row row = new Row();
				row.add(new com.lexst.db.column.Char((short) 1, host.getIP().getBytes()));
				row.add(new com.lexst.db.column.Integer((short) 2, host.getTCPort()));
				row.add(new com.lexst.db.column.Integer((short) 3, host.getUDPort()));
				controls.addItem(set, head, row);
			}
		} else {
			controls.showFault("cannot find address!");
		}
	}
	
	private void setCollectPath(String sql) {
		String path = parser.splitCollectPath(sql);
		// load collect configure
		List<String> list = CollectTaskPool.getInstance().load(path);
		int items = (list == null ? 0 : list.size());
		StringBuilder buff = new StringBuilder();
		for (int i = 0; list != null && i < list.size(); i++) {
			buff.append(String.format("naming: %s\r\n", list.get(i)));
		}
		controls.showMessage("load collect resource from %s, item count:%d\r\n%s", path, items, buff.toString());
	}
	
	private void testCollectTask(String sql) {
		String naming = parser.splitCollectTask(sql);
		// find task
		CollectTask task = CollectTaskPool.getInstance().find(naming);
		// show result
		if(task == null) {
			controls.showFault("cannot find '%s'!", naming);
		} else {
			controls.showMessage("'%s' existed!", naming);
//			task.execute(null, null);
		}
		
//		// test code, begin
////		String class_name = "org.lexst.collect.Block";
//		String class_name = "org.lexst.collect.Block";
//		Class<?> cls = CollectPool.getInstance().findClass(class_name);
//		if(cls == null) {
//			Logger.error("cannot find '%s' class", class_name);
//		} else {
//			Logger.info("find '%s' class", class_name);
//		}
//		
//		try {
////			cls = ClassLoader.getSystemClassLoader().loadClass(class_name);
////			cls = Class.forName(class_name, true, ClassLoader.getSystemClassLoader());
//			cls = Class.forName(class_name, true, CollectPool.getInstance().getClassLoader());
//			if (cls == null) {
//				Logger.error("cannot load find '%s' class", class_name);
//			} else {
//				Logger.info("loader '%s' class", class_name);
//			}
//		} catch (ClassNotFoundException exp) {
//			Logger.error(exp);
//		}
//		// test code, end
	}

	/**
	 * execute sql statement
	 * @param sql
	 */
	private void doSingle(String sql) {
		
		if("HELP".equalsIgnoreCase(sql)) {
			// 数据打印在窗口上
			LiveStreamInvoker invoker = new LiveStreamInvoker();
			String s = invoker.help();
			this.controls.showMessage(s);
			return;
		}
		
		boolean success = false;
		try {
			success = checker.isShowCharset(sql);
			if(success) {
				showCharset( sql );
			}
			if (!success) {
				success = checker.isCreateSchema(TopPool.getInstance().getSQLCharMap(), sql);
				if (success) createSchema(sql);
			}
			if (!success) {
				success = checker.isDeleteSchema(sql);
				if (success) deleteSchema(sql);
			}
			if(!success) {
				success = checker.isCreateUser(sql);
				if(success) createUser(sql);
			}
			if(!success) {
				success = checker.isDropUser(sql);
				if(success) deleteUser(sql);
			}
			if(!success) {
				success = checker.isDropSHA1User(sql);
				if(success) deleteSHA1User(sql);
			}
			if(!success) {
				success = checker.isAlterUser(sql);
				if(success) alterUser(sql);
			}
			if(!success) {
				success = checker.isGrant(sql);
				if(success) grant(sql);
			}
			if(!success) {
				success = checker.isRevoke(sql);
				if(success) revoke(sql);
			}
			if(!success) {
				success = checker.isSetChunkSize(sql);
				if (success) setChunkSize(sql);
			}
			if(!success) {
				success = checker.isSetOptimizeTime(sql);
				if(success) setOptimizeTime(sql);
			}
			if (!success) {
				success = checker.isDeleteTable(sql);
				if (success) deleteTable(sql);
			}
			if(!success) {
				success = checker.isSelectPattern(sql);
				if(success) select(sql);
			}
			if(!success) {
				success = checker.isDeletePattern(sql);
				if(success) delete(sql);
			}
			if (!success) {
				success = checker.isInsertPattern(sql);
				if (success) insert(sql);
			}
			if (!success) {
				success = checker.isInjectPattern(sql);
				if (success) inject(sql);
			}
			if (!success) {
				success = checker.isUpdate(sql);
				if (success) update(sql);
			}
			if(!success) {
				success = checker.isDC(sql);
				if(success) dc(sql);
			}
			if (!success) {
				success = checker.isADC(sql);
				if (success) adc(sql);
			}
			if (!success) {
				success = checker.isLoadIndex(sql);
				if (success) loadIndex(sql);
			}
			if (!success) {
				success = checker.isStopIndex(sql);
				if (success) stopIndex(sql);
			}
			if (!success) {
				success = checker.isLoadChunk(sql);
				if (success) loadChunk(sql);
			}
			if (!success) {
				success = checker.isStopChunk(sql);
				if (success) stopChunk(sql);
			}
			if(!success) {
				success = checker.isLoadOptimize(sql);
				if(success) loadOptimize(sql);
			}
			if(!success) {
				success = checker.isBuildTask(sql);
				if(success) buildTask(sql);
			}
			if (!success) {
				success = checker.isShowSite(sql);
				if (success) showSite(sql);
			}
			if (!success) {
				success = checker.isSetCollectPath(sql);
				if (success) setCollectPath(sql);
			}
			if (!success) {
				success = checker.isTestCollectPath(sql);
				if (success) testCollectTask(sql);
			}
		} catch (SQLSyntaxException exp) {
			String msg = exp.getMessage();
			controls.showFault(msg);
		}

		if(!success) {
			controls.showFault("invalid sql syntax");
		}
	}

	private String[] splitTIL(String[] items) {
		ArrayList<String> array = new ArrayList<String>();
		SQLChecker check = new SQLChecker();
		for (int index = 0; index < 3; index++) {
			String item = null;
			if (index == 0) {
				for (int j = 0; j < items.length; j++) {
					if (check.isCreateTableSyntax(items[j])) {
						item = items[j]; break;
					}
				}
			} else if (index == 1) {
				for (int j = 0; j < items.length; j++) {
					if (check.isCreateIndexSyntax(items[j])) {
						item = items[j]; break;
					}
				}
			} else if (index == 2) {
				for (int j = 0; j < items.length; j++) {
					if (check.isCreateLayoutSyntax(items[j])) {
						item = items[j]; break;
					}
				}
			}
			if (item == null && (index == 0 || index == 1)) {
				return null;
			}
			if (item != null) array.add(item);
		}
		if(array.isEmpty()) return null;
		String[] all = new String[array.size()];
		return array.toArray(all);
	}

	/**
	 * check sql syntax
	 */
	public void check() {
		String[] sqls = getSQL();
		if (sqls == null) return;

		if (sqls.length == 1) {
			checkSingle(sqls[0]);
		} else if (sqls.length > 1) {
			sqls = this.splitTIL(sqls);
			if(sqls == null) {
				controls.showFault("invalid create table command");
				return;
			}
			String table = (sqls.length >= 1 ? sqls[0] : null);
			String index = (sqls.length >= 2 ? sqls[1] : null);
			String layout = (sqls.length >= 3 ? sqls[2] : null);
			boolean success = false;
			try {
				success = checker.isCreateTable(table, index, layout);
			} catch (SQLSyntaxException exp) {
				String msg = exp.getMessage();
				controls.showFault(msg);
				return;
			}
			if (success) {
				controls.showMessage("correct syntax");
			} else {
				controls.showFault("incorrect syntax"); //can not be identified");
			}
		}
	}

	/**
	 * execute sql statement
	 */
	public void execute() {
		String[] sqls = getSQL();
		if (sqls == null) return;

		if (sqls.length == 1) {
			doSingle(sqls[0]);
		} else if (sqls.length > 1) {
			sqls = this.splitTIL(sqls);
			if(sqls == null) {
				controls.showFault("invalid sql syntax");
				return;
			}
			String table = (sqls.length >= 1 ? sqls[0] : null);
			String index = (sqls.length >= 2 ? sqls[1] : null);
			String layout = (sqls.length >= 3 ? sqls[2] : null);
			boolean success = checker.isCreateTable(table, index, layout);
			if (success) {
				createTable(table, index, layout);
			}
		}
	}

	private void initToolBar() {
		JButton[] cmds = { cmdCheck, cmdGo, cmdFont, cmdCut, cmdClear };
		String[] images = { "sqlcheck_16.png", "go_16.png", "font_16.png", "cut_16.png", "clear_16.png" };
		String[] tips = {"Check SQL Syntax (F5)", "Run (F6)", "Set Text Font", "Clear Message", "Clear Table Information"};
		for (int i = 0; i < cmds.length; i++) {
			Icon icon = InsideIcon.getIcon(getClass(), images[i]);
			cmds[i].setIcon(icon);
			cmds[i].addActionListener(this);
			cmds[i].setToolTipText(tips[i]);
			toolbar.add(cmds[i]);
		}
		toolbar.setFloatable(false);
		toolbar.setOrientation(JToolBar.VERTICAL);
	}

	public void init() {
		this.initToolBar();

		JPanel top = initSQLPane();
		controls.init();
		JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, controls);
		
        pane.setContinuousLayout(true);
        pane.setOneTouchExpandable(true);
        pane.setResizeWeight(0.05);
        pane.setBorder(new EmptyBorder(0, 0, 0, 0));

        setLayout(new BorderLayout());
		add(pane, BorderLayout.CENTER);
	}

	
	private JPanel initSQLPane() {
		Font f1 = txtSQL.getFont();
		Font font = new Font(f1.getName(), f1.getStyle(), f1.getSize() + 4);

		txtSQL.setFont(font);
		txtSQL.setPreferredSize(new Dimension(10, 80));
		txtSQL.addKeyListener(new SQLKeyAdapter());
		txtSQL.setToolTipText("SQL Syntax Input    (ALT+S)");
		txtSQL.setFocusAccelerator('S');
		txtSQL.setBorder(new EmptyBorder(2, 2, 2, 2));

		JScrollPane top = new JScrollPane(txtSQL);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(top, BorderLayout.CENTER);
		panel.add(toolbar, BorderLayout.EAST);
		return panel;
	}

	public void reload() {
		int size = 5;
		String[] heads = new String[size];
		for(int i = 0; i<size; i++) {
			heads[i] = String.format("Key %d", i+1);
		}
		controls.updateTable(heads);

		Row row = new Row();
		for (short i = 0; i < size; i++) {
			//row.add(new Char(i, "abc"));
		}
		for (int i = 0; i < 1000; i++) {
			controls.addItem(row);
		}
	}

}
