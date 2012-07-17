/**
 * 
 */
package com.lexst.live.console;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

import com.lexst.algorithm.collect.*;
import com.lexst.db.*;
import com.lexst.db.account.*;
import com.lexst.db.charset.*;
import com.lexst.db.field.*;
import com.lexst.db.row.*;
import com.lexst.db.schema.*;
import com.lexst.db.statement.*;
import com.lexst.fixp.*;
import com.lexst.live.*;
import com.lexst.live.pool.*;
import com.lexst.live.window.Launcher;
import com.lexst.log.client.*;
import com.lexst.site.live.*;
import com.lexst.sql.*;
import com.lexst.util.*;
import com.lexst.util.host.*;
import com.lexst.visit.*;

final class Terminal {

	/* java console */
	private Console console;

	/* into logined status */
	private boolean logined;

	private SQLParser parser = new SQLParser();
	private SQLChecker checker = new SQLChecker();

	/**
	 * 
	 */
	public Terminal() {
		super();
		logined = false;
	}

	/**
	 * @return
	 */
	public boolean isLogined() {
		return this.logined;
	}

	/**
	 * @return
	 */
	public Console getConsole() {
		return this.console;
	}

	/**
	 * load system console
	 * @return
	 */
	public boolean initialize() {
		if (console == null) {
			console = System.console();
		}
		return (console != null);
	}
	
	private String input() {
		String cmd = console.readLine("%s", "SQL> ");
		return cmd.trim();
	}

	private void showFault(String s) {
		System.out.println(s);
	}

	private void showFault(String format, Object... args) {
		String s = String.format(format, args);
		showFault(s);
	}

	private void showMessage(String s) {
		System.out.println(s);
	}

	private void showMessage(String format, Object... args) {
		String s = String.format(format, args);
		showMessage(s);
	}

	private void showMessage(String naming, IP[] hosts) {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("accpeted '%s'", naming));
		for (int i = 0; hosts != null && i < hosts.length; i++) {
			sb.append("\n");
			sb.append(String.format("site: %s", hosts[i].toString()));
		}
		this.showMessage(sb.toString());
	}

	private void showMessage(Space space, IP[] hosts) {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("accpeted '%s'", space));
		for (int i = 0; hosts != null && i < hosts.length; i++) {
			sb.append("\n");
			sb.append(String.format("site: %s", hosts[i].toString()));
		}
		this.showMessage(sb.toString());
	}
	
	private boolean isHelp(String cmd) {
		return "HELP".equalsIgnoreCase(cmd);
	}
	
	private boolean isExit(String cmd) {
		return "EXIT".equalsIgnoreCase(cmd) || "QUIT".equalsIgnoreCase(cmd);
	}

	/**
	 * when exit, true return; otherwise false 
	 * @return
	 */
	public boolean check() {
		String cmd = input();
		if(isHelp(cmd)) {
			this.help();
			return false;
		} else if(isExit(cmd)) {
			return true;
		}
		// other command
		this.execute(cmd);
		return false;
	}
	
	/**
	 * execute sql statement
	 * @param sql
	 */
	private void execute(String sql) {
		boolean success = false;
		try {
			success = checker.isShowCharset(sql);
			if (success) {
				showCharset(sql);
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
				success = isCreateTable(sql);
				if(success) createTable(sql);
			}
			if (!success) {
				success = checker.isDeleteTable(sql);
				if (success) deleteTable(sql);
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
			if (!success) {
				success = checker.isDC(sql);
				if (success) dc(sql);
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
			if (!success) {
				showFault("invalid sql command");
			}
		} catch (SQLSyntaxException exp) {
			String msg = exp.getMessage();
			this.showFault(msg);
		}
	}
	
	private boolean isCreateTable(String sql) {
		String regex = "^\\s*(?i)create\\s+(?i)table\\s+(.+)\\s*";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(sql);
		return matcher.matches();
	}
	
	private void createTable(String sql) {
		String[] all = sql.split("&");
		String[] sqls = this.splitTIL(all);
		if(sqls == null) {
			this.showFault("invalid create table command");
			return;
		}
		String sqlTable = (sqls.length >= 1 ? sqls[0] : null);
		String sqlIndex = (sqls.length >= 2 ? sqls[1] : null);
		String sqlLayout = (sqls.length >= 3 ? sqls[2] : null);
		
		boolean success = false;
		try {
			success = checker.isCreateTable(sqlTable, sqlIndex, sqlLayout);
		} catch (SQLSyntaxException exp) {
			this.showFault(exp.getMessage());
			return;
		}
		if (!success) {
			this.showFault("incorrect syntax");
			return;
		}
		
		// send to top site
		LiveSite local = Launcher.getInstance().getLocal();
		Table table = parser.splitCreateTable(sqlTable, sqlIndex, sqlLayout);
		success = TopPool.getInstance().createTable(null, local, table);
		if (success) {
			Space space = table.getSpace();
			this.showMessage("create '%s' success", space);
		} else {
			this.showFault("cannot create '%s'", table.getSpace());
		}
	}
	
	private String[] splitTIL(String[] items) {
		ArrayList<String> array = new ArrayList<String>();
		for (int index = 0; index < 3; index++) {
			String item = null;
			if (index == 0) {
				for (int j = 0; j < items.length; j++) {
					if (checker.isCreateTableSyntax(items[j])) {
						item = items[j]; break;
					}
				}
			} else if (index == 1) {
				for (int j = 0; j < items.length; j++) {
					if (checker.isCreateIndexSyntax(items[j])) {
						item = items[j]; break;
					}
				}
			} else if (index == 2) {
				for (int j = 0; j < items.length; j++) {
					if (checker.isCreateLayoutSyntax(items[j])) {
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
					System.out.println("cannot find field:" + columnId);
					return;
				}
				Field field2 = field.fresh();
				field2.setColumnId(id++);
				head.add(field2);
			}
		}
		// update table
		this.showHeads(head);
		
		SQLCaller caller = new SQLCaller();
		byte[] bytes = caller.select(top, local, select);

		this.splitShow(bytes, head);
	}

	/**
	 * query data
	 * @param sql
	 */
	private void dc(String sql) {
		SQLCharset charset = null;
		Table table = null;
		Space space = checker.getDiffuseSpace(sql);
		if (space != null) {
			charset = TopPool.getInstance().findCharset(space.getSchema());
			if(charset == null) {
				System.out.printf("cannot find charset: '%s'\r\n", space.getSchema());
				return;
			}
			table = TopPool.getInstance().findTable(space);
			if(table == null) {
				System.out.printf("cannot find table: '%s'\r\n", space);
				return;
			}
		}
		// split to select object
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
			if(collTask != null) {
				collTask.setSpace(dc.getCollectSpace());
				collTask.setWriteto(dc.getCollectWriteto());
			}
		}

		// call "dc" command
		SQLCaller caller = new SQLCaller();
		byte[] bytes = caller.dc(top, local, dc);

		// show result
		if(collTask == null) {
			this.splitShow(bytes, head);
		} else {
			Object[] params = new Object[] { charset, head };
			collTask.execute(params, bytes);
		}
	}
	
	private void adc(String sql) {
		SQLCharset charset = null; 
		Table table = null;
		Space space = checker.getDiffuseSpace(sql);
		if (space != null) {
			charset = TopPool.getInstance().findCharset(space.getSchema());
			if(charset == null) {
				System.out.printf("cannot find charset: '%s'\r\n", space.getSchema());
				return;
			}
			table = TopPool.getInstance().findTable(space);
			if(table == null) {
				System.out.printf("cannot find table: '%s'\r\n", space);
				return;
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
			if(collTask != null) {
				collTask.setSpace(adc.getCollectSpace());
				collTask.setWriteto(adc.getCollectWriteto());
			}
		}

		// show query result
		SQLCaller caller = new SQLCaller();
		byte[] bytes = caller.adc(top, local, adc);
		
		// show result
		if (collTask == null) {
			this.splitShow(bytes, head);
		} else {
			Object[] params = new Object[] { charset, head };
			collTask.execute(params, bytes);
		}
	}

	/**
	 * delete data
	 * @param sql
	 */
	private void delete(String sql) {
		Space space = checker.getDeleteSpace(sql);
		if(space == null) {
			this.showFault("illegal delete syntax");
		}
		SQLCharset set = TopPool.getInstance().findCharset(space.getSchema());
		// find a table
		Table table = TopPool.getInstance().findTable(space);
		Delete delete = parser.splitDelete(set, table, sql);
		
		SiteHost top = TopPool.getInstance().getRemote();
		LiveSite local = Launcher.getInstance().getLocal();

		// show table head
		String[] head = { "count" };
		this.showHeads(head);
		// show delete count		
		SQLCaller caller = new SQLCaller();
		long count = caller.delete(top, local, delete);
		
		com.lexst.db.column.Long value = new com.lexst.db.column.Long((short) 1, count);
		Row row = new Row();
		row.add(value);
		addItem(row);
	}

	private void insert(String sql) {
		Space space = checker.getInsertSpace(sql);
		if(space == null) {
			this.showFault("illegal insert syntax");
			return;
		}
		SQLCharset set = TopPool.getInstance().findCharset(space.getSchema());
		Table table = TopPool.getInstance().findTable(space);
		Insert insert = parser.splitInsert(set, table, sql);
		
		SiteHost top = TopPool.getInstance().getRemote();
		LiveSite local = Launcher.getInstance().getLocal();

		SQLCaller caller = new SQLCaller();
		int count = caller.insert(top, local, insert);

		if(count > 0) {
			this.showMessage("insert %d item", count);
		} else {
			this.showFault("insert failed");
		}
	}

	private void inject(String sql) {
		Space space = checker.getInsertSpace(sql);
		if(space == null) {
			this.showFault("illegal inject syntax");
			return;
		}
		SQLCharset set = TopPool.getInstance().findCharset(space.getSchema());
		Table table = TopPool.getInstance().findTable(space);
		Inject inject = parser.splitInject(set, table, sql);
		
		SiteHost top = TopPool.getInstance().getRemote();
		LiveSite local = Launcher.getInstance().getLocal();

		SQLCaller caller = new SQLCaller();
		int count = caller.inject(top, local, inject);

		if(count > 0) {
			this.showMessage("inject %d item", count);
		} else {
			this.showFault("inject failed");
		}
	}

	private void update(String sql) {
		Space space = checker.getUpdateSpace(sql);
		if(space == null) {
			this.showFault("illegal update syntax");
			return;
		}
		SQLCharset set = TopPool.getInstance().findCharset(space.getSchema());
		Table table = TopPool.getInstance().findTable(space);
		Update update = parser.splitUpdate(set, table, sql);
		
		SiteHost top = TopPool.getInstance().getRemote();
		LiveSite local = Launcher.getInstance().getLocal();

		// show table head
		String[] head = { "count" };
		this.showHeads(head);
		// show delete count		
		SQLCaller caller = new SQLCaller();
		long count = caller.update(top, local, update);
		
		com.lexst.db.column.Long value = new com.lexst.db.column.Long((short) 1, count);
		Row row = new Row();
		row.add(value);
		addItem(row);
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
			this.showMessage("delete '%s' success", space);
		} else {
			this.showFault("cannot delete '%s'", space);
		}
	}

	private void setOptimizeTime(String sql) {
		Object[] objects = parser.splitSetOptimizeTime(sql);
		Space space = (Space)objects[0];
		int type = ((Integer)objects[1]).intValue();
		long time = ((Long)objects[2]).longValue();
		
		if(!TopPool.getInstance().existsTable(space)) {
			this.showFault("cannot find '%s'", space);
			return;
		}
		
		LiveSite local = Launcher.getInstance().getLocal();
		boolean success = TopPool.getInstance().setOptimizeTime(null, local, space, type, time);
		if (success) {
			this.showMessage("set success");
		} else {
			this.showFault("set fault");
		}
	}
	
	private void createUser(String sql) {
		LiveSite local = Launcher.getInstance().getLocal();
		User user = parser.splitCreateUser(sql);
		boolean success = TopPool.getInstance().createUser(null, local, user);
		if(success) {
			this.showMessage("create '%s' success", user.getHexUsername());
		} else {
			this.showFault("cannot create '%s' user", user.getHexUsername());
		}
	}
	
	private void deleteUser(String sql) {
		LiveSite local = Launcher.getInstance().getLocal();
		User user = parser.splitDropUser(sql);
		boolean success = TopPool.getInstance().deleteUser(null, local, user.getHexUsername());
		if(success) {
			this.showMessage("drop user success");
		} else {
			this.showFault("cannot drop user");
		}
	}
	
	private void deleteSHA1User(String sql) {
		LiveSite local = Launcher.getInstance().getLocal();
		User user = parser.splitDropSHA1User(sql);
		boolean success = TopPool.getInstance().deleteUser(null, local, user.getHexUsername());
		if(success) {
			this.showMessage("drop sha1 user success");
		} else {
			this.showFault("cannot drop sha1 user");
		}
	}

	private void alterUser(String sql) {
		LiveSite local = Launcher.getInstance().getLocal();
		User user = parser.splitAlterUser(sql);
		boolean success = TopPool.getInstance().alterUser(null, local, user);
		if (success) {
			this.showMessage("alter '%s' success", user.getHexUsername());
		} else {
			this.showFault("cannot alter '%s' user", user.getHexUsername());
		}
	}

	private void grant(String sql) {
		LiveSite local = Launcher.getInstance().getLocal();
		Permit permit = parser.splitGrant(sql);
		boolean success = TopPool.getInstance().addPermit(null, local, permit);
		if(success) {
			this.showMessage("grant success");
		} else {
			this.showFault("grant fault");
		}
	}

	private void revoke(String sql) {
		LiveSite local = Launcher.getInstance().getLocal();
		Permit permit = parser.splitRevoke(sql);
		boolean success = TopPool.getInstance().deletePermit(null, local, permit);
		if(success) {
			this.showMessage("revoke success");
		} else {
			this.showFault("revoke fault");
		}
	}

	private void deleteSchema(String sql) {
		LiveSite site = Launcher.getInstance().getLocal();
		Schema db = parser.splitDropSchema(sql);
		boolean success = TopPool.getInstance().deleteSchema(null, site, db.getName());
		if(success) {
			this.showMessage("drop '%s' success", db.getName());
		} else {
			this.showFault("cannot drop '%s' database");
		}
	}
	
	private void createSchema(String sql) {
		LiveSite site = Launcher.getInstance().getLocal();
		SQLCharmap map = TopPool.getInstance().getSQLCharMap();
		Schema db = parser.splitCreateSchema(map, sql);
		boolean success = TopPool.getInstance().createSchema(null, site, db);
		if (success) {
			this.showMessage("create '%s' success", db.getName());
		} else {
			this.showFault("cannot create database");
		}
	}
	
	private void setChunkSize(String sql) {
		Object[] objects = parser.splitSetChunkSize(sql);
		Space space = (Space) objects[0];
		Integer size = (Integer) objects[1];
		
		if (!TopPool.getInstance().existsTable(space)) {
			this.showFault("Cannot find " + space);
			return;
		}
		int limit = 1024 * 1024 * 1024;
		if (size > limit) {
			this.showFault("chunk size is too large");
			return;
		}
		
		LiveSite local = Launcher.getInstance().getLocal();
		boolean success = TopPool.getInstance().setChunkSize(null, local, space, size.intValue());
		if(success) {
			this.showMessage("set success");
		} else {
			this.showFault("set fault");
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
				showFault(String.format("cannot find charset by \'%s\' ", db));
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
					this.showFault("cannot find \'%s\'", clsname);
					return;
				}
				map.add(mode);
			}
		}

		String[] heads  = {"Char name", "Type", "Class name"};
		this.showHeads(heads);
		for(String name : map.keys()) {
			SQLCharType mode = map.find(name);
			com.lexst.db.column.Char charname = new com.lexst.db.column.Char((short)1, mode.getName().getBytes() );
			com.lexst.db.column.Char type = new com.lexst.db.column.Char((short)2, mode.toTypeString().getBytes() );
			com.lexst.db.column.Char clsname = new com.lexst.db.column.Char((short)3, mode.getClassName().getBytes());

			Row row = new Row();
			row.add(charname);
			row.add(type);
			row.add(clsname);
			this.addItem(row);
		}
	}
	
	public void loadOptimize(String sql) {
		SpaceHost sh = parser.splitLoadOptimize(sql);
		LiveSite local = Launcher.getInstance().getLocal();
		try {
			IP[] s = TopPool.getInstance().optimize(null, local, sh.getSpace(), sh.getAllHost());
			this.showMessage(sh.getSpace(), s);
		} catch (VisitException exp) {
			this.showFault("cannot accepted optimize '%s'", sh.getSpace());
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
			this.showFault("cannot accpeted index '%s'", sh.getSpace());
		}
	}

	private void stopIndex(String sql) {
		SpaceHost sh = parser.splitStopIndex(sql);
		LiveSite local = Launcher.getInstance().getLocal();
		try {
			IP[] s = TopPool.getInstance().stopIndex(null, local, sh.getSpace(), sh.getAllHost());
			this.showMessage(sh.getSpace(), s);
		} catch (VisitException exp) {
			this.showFault("cannot accpeted index '%s'", sh.getSpace());
		}
	}
	
	private void loadChunk(String sql) {
		SpaceHost sh = parser.splitLoadChunk(sql);
		LiveSite local = Launcher.getInstance().getLocal();
		try {
			IP[] s = TopPool.getInstance().loadChunk(null, local, sh.getSpace(), sh.getAllHost());
			this.showMessage(sh.getSpace(), s);
		} catch (VisitException exp) {
			this.showFault("cannot accpeted index '%s'", sh.getSpace());
		}
	}
	
	private void stopChunk(String sql) {
		SpaceHost sh = parser.splitStopChunk(sql);
		LiveSite local = Launcher.getInstance().getLocal();
		try {
			IP[] s = TopPool.getInstance().stopChunk(null, local, sh.getSpace(), sh.getAllHost());
			this.showMessage(sh.getSpace(), s);
		} catch (VisitException exp) {
			this.showFault("cannot accpeted chunk '%s'", sh.getSpace());
		}
	}
	
	private void buildTask(String sql) {
		NamingHost sh = parser.splitBuildTask(sql);
		LiveSite local = Launcher.getInstance().getLocal();
		try {
			IP[] s = TopPool.getInstance().buildTask(null, local, sh.getNaming(), sh.getAllHost());
			this.showMessage(sh.getNaming(), s);
		} catch (VisitException exp) {
			this.showFault("cannot accpeted naming task '%s'", sh.getNaming());
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
			Logger.error(exp);
		}
		
		if (hosts != null) {
			Table head = new Table();
			head.add(new com.lexst.db.field.CharField((short) 1, "IP"));
			head.add(new com.lexst.db.field.IntegerField((short) 2, "TCP PORT", 0));
			head.add(new com.lexst.db.field.IntegerField((short) 3, "UDP PORT", 0));

			List<Row> a = new ArrayList<Row>();
			for (SiteHost host : hosts) {
				Row row = new Row();
				row.add(new com.lexst.db.column.Char((short) 1, host.getIP().getBytes()));
				row.add(new com.lexst.db.column.Integer((short) 2, host.getTCPort()));
				row.add(new com.lexst.db.column.Integer((short) 3, host.getUDPort()));
				a.add(row);
			}
			
			// show list
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
		showMessage("load collect resource from %s, item count:%d\r\n%s", path, items, buff.toString());
	}
	
	private void testCollectTask(String sql) {
		String naming = parser.splitCollectTask(sql);
		// find task
		CollectTask task = CollectTaskPool.getInstance().find(naming);
		// show result
		if(task == null) {
			this.showFault("cannot find '%s'!", naming);
		} else {
			this.showMessage("'%s' existed!", naming);
		}
	}
	
	private String format(String s, int limit) {
		char c = 0x20;
		StringBuilder buff = new StringBuilder(s);
		for (int i = buff.length(); i < limit; i++) {
			buff.append(c);
		}
		return buff.toString();
	}

	private void showHeads(String[] cols) {
		StringBuilder buff = new StringBuilder();
		for (int index = 0; index < cols.length; index++) {
			String s = format(cols[index], 12);
			buff.append(s);
		}
		System.out.println(buff.toString());
	}
	
	private void showHeads(Table head) {
		StringBuilder buff = new StringBuilder();
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
			String name = field.getName();
			String s = format(name, width - name.length());
			buff.append(s);
			index++;
		}
		System.out.println(buff.toString());
	}
	
	private void addItem(Row row) {

	}
	
	private void splitShow(byte[] data, Table table) {
		if(data == null) return;
		
		int off = 0, size = data.length;
		if(size < 8) return;
		int items = Numeric.toInteger(data, off, 8);
		off += 8;

		if (table == null) {
			System.out.printf("rows %d\n", items);
			return;
		}

		// split and show
		while(off < size) {
			Row row = new Row();
			int len = row.resolve(table, data, off);
			if(len < 1) break;
			off += len;
//			this.addItem(table, row);
		}
	}
	
	private String translate(String ip) {
		if(IP4Style.isLoopbackIP(ip)) {
			ip = IP4Style.getFirstPrivateAddress();
		} else if(!IP4Style.isIPv4(ip)) {
			try {
				InetAddress address = InetAddress.getByName(ip);
				if(address == null) return null;
				String v4 = address.getHostAddress();
				if(v4.equalsIgnoreCase(ip)) {
					return null;
				}
				ip = v4;
			} catch (UnknownHostException exp) {
				return null;
			}
		}
		return ip;
	}
	
	private boolean connect(SiteHost remote) {
		String username = console.readLine("%s", "User: ");
		char[] pwd = console.readPassword("%s", "Pwd: ");
		String password = new String(pwd);
		//choose a ciphertext
		int digit = 0;
		while (true) {
			String num = console.readLine("%s","0 None | 1 AES | 2 DES | 3 3DES | 4 BLOWFISH | 5 MD5 | 6 SHA1 | Choose:");
			try {
				digit = Integer.parseInt(num);
				if (0 <= digit && digit <= 6) break;
			} catch (NumberFormatException exp) {

			}
		}
 
		LiveSite site = Launcher.getInstance().getLocal();
		site.setUser(username, password);
		// set secure algorithm
		switch(digit) {
		case 1:
			site.setAlgorithm(Cipher.translate(Cipher.AES)); break;
		case 2: 
			site.setAlgorithm(Cipher.translate(Cipher.DES)); break;
		case 3: 
			site.setAlgorithm(Cipher.translate(Cipher.DES3)); break;
		case 4:
			site.setAlgorithm(Cipher.translate(Cipher.BLOWFISH)); break;
		case 5:
			site.setAlgorithm(Cipher.translate(Cipher.MD5)); break;
		case 6:
			site.setAlgorithm(Cipher.translate(Cipher.SHA1)); break;
		}

		// login
		logined = TopPool.getInstance().login(remote, site);
		System.out.printf("%s\n", (logined ? "login success" : "login failed"));
		return logined;
	}

	public boolean login() {
		boolean success = false;
		while (true) {
			String cmd = input();
			if (isHelp(cmd)) {
				this.help();
			} else if (isExit(cmd)) {
				break;
			} else {
				String regex = "^\\s*(?i)(?:OPEN|LOGIN|CONNECT)\\s+([a-zA-Z0-9.-]+)(?:\\s+|\\:)(\\d{1,5})\\s*$";
				Pattern pattern = Pattern.compile(regex);
				Matcher matcher = pattern.matcher(cmd);
				if (matcher.matches()) {
					String s1 = matcher.group(1);
					String s2 = matcher.group(2);
					String ip = translate(s1);
					if (ip == null) {
						System.out.println("address invalid!");
						continue;
					}
					SiteHost host = new SiteHost(ip, Integer.parseInt(s2), Integer.parseInt(s2));
					success = connect(host);
					if(success) break;
				} else {
					System.out.println("not login");
				}
			}
		}
		return success;
	}
	
	private void help() {
		System.out.println("----------- command -----------");
		LiveStreamInvoker invoker = new LiveStreamInvoker();
		String s = invoker.help();
		showMessage(s);
	}
	
}
