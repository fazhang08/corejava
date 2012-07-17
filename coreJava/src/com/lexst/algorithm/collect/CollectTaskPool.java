/**
 * 
 */
package com.lexst.algorithm.collect;

import com.lexst.algorithm.*;
//import com.lexst.log.client.*;
//import com.lexst.pool.*;
import com.lexst.util.naming.Naming;
//import com.lexst.xml.*;
//
//import java.util.*;
//import java.util.jar.*;
//import java.io.*;
//
//import org.w3c.dom.*;

/**
 * Collect 规则
 * 1. 在jar必须有一个COLLECT-INF目录,这个目录下放一个collect.xml,做为配置文件
 * 2. "COLLECT-INF"必须全大写
 * 3. "collect.xml"必须全小写 
 * 4. collect.xml里面的配置规则见"task"标准
 * 5. 与task中的类相关的类文件可以放在不同包中,但是必须能被找到
 * 6. 如果在启动目录下定义"collect"目录,运行时自动加载里面的配置文件
 * 
 * Live接口参数: 1.SQLCharSet 2. Table, 3. 显示窗口
 * Console接口参数: 1. SQLCharset 2. Table 
 */
public class CollectTaskPool extends TaskPool {
	
	private static CollectTaskPool selfHandle = new CollectTaskPool();
		
//	private final String tag = "COLLECT-INF/collect.xml";
//	
//	private ProjectClassLoader loader = new ProjectClassLoader();
//	
//	/* object naming -> project object */
//	private Map<Naming, Project> mapProject = new HashMap<Naming, Project>();
//	
//	/* object naming  -> jar file*/
//	private Map<Naming, String> mapNaming = new HashMap<Naming, String>();
//
//	/* filename -> file object */
//	private Map<String, File> mapFile = new HashMap<String, File>();
//
//	/* directory array */
//	private List<File> paths = new ArrayList<File>();

	/**
	 * 
	 */
	private CollectTaskPool() {
		super();
	}

	/**
	 * @return
	 */
	public static CollectTaskPool getInstance() {
		return CollectTaskPool.selfHandle;
	}

	/**
	 * @param naming
	 * @return
	 */
	public CollectTask find(Naming naming) {
		return (CollectTask) super.findTask(naming);
	}
	
	/**
	 * @param naming
	 * @return
	 */
	public CollectTask find(String naming) {
		return (CollectTask) super.findTask(naming);
	}

//	/**
//	 * load ".jar" file
//	 * @param path
//	 * @return
//	 */
//	public List<String> load(String path) {		
//		File root = new File(path);
//		if (!(root.exists() && root.isDirectory())) {
//			this.remove_path(path);
//			return null;
//		}
//
//		ArrayList<String> array = new ArrayList<String>();
//
//		File[] subs = root.listFiles();
//		for(File file : subs) {
//			if(file.isHidden()) continue;
//			// load jar file
//			if (file.isDirectory()) {
//				List<String> list = loadPath(file);
//				if (list != null) array.addAll(list);
//			} else if (file.isFile()) {
//				String filename = file.getAbsolutePath();
//				if (filename.toLowerCase().endsWith(".jar")) {
//					array.add(filename);
//				}
//			}
//		}
//		
//		ArrayList<String> a2 = new ArrayList<String>();
//		for (String filename : array) {
//			File file1 = new File(filename);
//			File file2 = mapFile.get(filename);
//			if (file2 == null) {
//				a2.add(filename);
//			} else if (file1.lastModified() != file2.lastModified() || file1.length() != file2.length()) {
//				this.remove_file(filename);
//				a2.add(filename);
//			}
//		}
//		array.clear();
//		array.addAll(a2);
//
//		// load jar file
//		for(String filename : array) {
//			loader.add(filename);
//		}
//		// resolve jar file
//		ArrayList<String> names = new ArrayList<String>();
//		for(String filename : array) {
//			List<String> list = addFile(filename);
//			if(list != null) names.addAll(list);
//			this.add_filename(filename);
//		}
//		
//		add_path(root);
//		return names;
//	}
//
//	/**
//	 * resolve filename
//	 * @param root
//	 * @return
//	 */
//	private List<String> loadPath(File root) {
//		ArrayList<String> array = new ArrayList<String>();
//
//		File[] subs = root.listFiles();
//		for (File file : subs) {
//			if (file.isHidden()) continue;
//			// load jar file
//			if (file.isDirectory()) {
//				List<String> list = loadPath(file);
//				if(list != null) array.addAll(list);
//			} else if (file.isFile()) {
//				String filename = file.getAbsolutePath();
//				if (filename.toLowerCase().endsWith(".jar")) {
//					array.add(filename);
//				}
//			}
//		}
//
//		return array;
//	}
//
//	/**
//	 * @param filename
//	 * @return
//	 */
//	private List<String> addFile(String filename) {
//		File file = new File(filename);
//		ByteArrayOutputStream buff = new ByteArrayOutputStream();
//		try {
//			FileInputStream fi = new FileInputStream(file);
//			JarInputStream in = new JarInputStream(fi);
//			while (true) {
//				JarEntry entry = in.getNextJarEntry();
//				if (entry == null) break;
//
//				String name = entry.getName();
//				if (name.endsWith(tag)) {
//					byte[] b = new byte[10240];
//					while (true) {
//						int len = in.read(b, 0, b.length);
//						if (len == -1) break;
//						buff.write(b, 0, len);
//					}
//					break;
//				}
//			}
//			in.close();
//			fi.close();
//		} catch (IOException exp) {
//			Logger.error(exp);
//		}
//
//		if(buff.size() == 0) return null;
//				
//		// resolve xml
//		byte[] b = buff.toByteArray();
//		XMLocal xml = new XMLocal();
//		Document document = xml.loadXMLSource(b);
//		if (document == null) {
//			return null;
//		}
//		
//		List<String> array = new ArrayList<String>();
//		NodeList list =	document.getElementsByTagName("task");
//		int size = list.getLength();
//		for (int i = 0; i < size; i++) {
//			Element elem = (Element) list.item(i);
//			// task naming
//			String name = xml.getValue(elem, "naming");
//			// project class name
//			String project_class = xml.getValue(elem, "project-class");
//			// task class name
//			String task_class = xml.getValue(elem, "task-class");
//			// resource
//			String resource = xml.getValue(elem, "resource");
//
//			boolean success = addProject(filename, name, task_class, project_class, resource);
//			if(success) array.add(name);
//		}
//		return array;
//	}
//	
//	/**
//	 * @param filename
//	 * @param name
//	 * @param task_class
//	 * @param project_class
//	 * @param resource
//	 * @return
//	 */
//	private boolean addProject(String filename, String name, String task_class, String project_class, String resource) {
//		boolean success = false;
//		Naming naming = new Naming(name);
//		
//		super.lockSingle();
//		try {
//			Class<?> clss = Class.forName(project_class, true, loader);
//			Project project = (Project) clss.newInstance();
//
//			project.setNaming(naming);
//			project.setTaskClass(task_class);
//			project.setResource(resource);
//			
//			mapProject.put(naming, project);
//			mapNaming.put(naming, filename);
//			success = true;
//		} catch (ClassNotFoundException exp) {
//			Logger.error(exp);
//		} catch (IllegalAccessException exp) {
//			Logger.error(exp);
//		} catch (InstantiationException exp) {
//			Logger.error(exp);
//		} finally {
//			super.unlockSingle();
//		}
//		return success;
//	}
//	
//	/**
//	 * find collect task
//	 * 
//	 * @param naming
//	 * @return
//	 */
//	public CollectTask find(String name) {
//		Naming naming = new Naming(name);
//
//		super.lockMulti();
//		try {
//			Project project = mapProject.get(naming);
//			if (project != null) {
//				String task_class = project.getTaskClass();
//				CollectTask task = (CollectTask) Class.forName(task_class, true, loader).newInstance();
//				task.setProject(project);
//				return task;
//			}
//		} catch (InstantiationException exp) {
//			Logger.error(exp);
//		} catch (IllegalAccessException exp) {
//			Logger.error(exp);
//		} catch (ClassNotFoundException exp) {
//			Logger.error(exp);
//		} catch (Throwable exp) {
//			Logger.fatal(exp);
//		} finally {
//			super.unlockMulti();
//		}
//		return null;
//	}
//	
//	/**
//	 * @param filename
//	 * @return
//	 */
//	private int remove_file(String filename) {
//		List<Naming> array = new ArrayList<Naming>();
//		super.lockSingle();
//		try {
//			for(Naming naming: mapNaming.keySet()) {
//				String filename2 = mapNaming.get(naming);
//				if(filename.equalsIgnoreCase(filename2)) {
//					array.add(naming);
//				}
//			}
//			for(Naming naming: array) {
//				mapNaming.remove(naming);
//				mapProject.remove(naming);
//			}
//			mapFile.remove(filename);
//		} catch (Throwable exp) {
//			Logger.error(exp);
//		} finally {
//			super.unlockSingle();
//		}
//		return array.size();
//	}
//	
//	private int remove_path(String path) {
//		if (path.charAt(path.length() - 1) != File.separatorChar) path += File.separator;
//		
//		List<String> array = new ArrayList<String>();
//		super.lockSingle();
//		try {
//			for (String filename : mapFile.keySet()) {
//				int index = filename.indexOf(File.separatorChar);
//				String sub = filename.substring(index + 1);
//				if (path.equalsIgnoreCase(sub)) {
//					array.add(filename);
//				}
//			}
//		} catch (Throwable exp) {
//
//		} finally {
//			super.unlockSingle();
//		}
//		
//		int count = 0;
//		for(String filename : array) {
//			int size = this.remove_file(filename);
//			count += size;
//		}
//		return count;
//	}
//	
//	private void add_filename(String filename) {
//		super.lockSingle();
//		try {
//			mapFile.put(filename, new File(filename));
//		} catch (Throwable exp) {
//			Logger.error(exp);
//		} finally {
//			super.unlockSingle();
//		}
//	}
//	
//	/**
//	 * @param path
//	 */
//	private void add_path(File path) {
//		super.lockSingle();
//		try {
//			if (!paths.contains(path)) {
//				paths.add(path);
//			}
//		} catch (Throwable exp) {
//			Logger.error(exp);
//		} finally {
//			super.unlockSingle();
//		}
//	}
//
//	/**
//	 * check resource time (file time and file size)
//	 */
//	private void check() {
//		// remove or update jar file
//		Map<String, File> map1 = new HashMap<String, File>(mapFile);
//		for (String filename : map1.keySet()) {
//			File file1 = map1.get(filename);
//			// cannot find, delete it
//			if(!file1.exists()) {
//				this.remove_file(filename);
//				continue;
//			}
//			
//			// not match, update it
//			File file2 = new File(filename);
//			if (file1.lastModified() != file2.lastModified() || file1.length() != file2.length()) {
//				// delete old jar
//				this.remove_file(filename);
//				// add new jar
//				addFile(filename);
//				// save filename
//				add_filename(filename);
//			}
//		}
//		
//		// new jar file
//		List<String> a = new ArrayList<String>();
//		ArrayList<File> a2 = new ArrayList<File>(paths);
//		for(File path : a2) {
//			List<String> list = this.loadPath(path);
//			if(list == null) continue;
//			for(String filename : list) {
//				if(mapFile.containsKey(filename)) continue;
//				// save file
//				a.add(filename);
//			}
//		}
//		
//		// add new jar
//		for(String filename : a) {
//			loader.add(filename);
//		}
//		// add new jar
//		for(String filename : a) {
//			addFile(filename);
//			add_filename(filename);
//		}
//	}
//
//	/* (non-Javadoc)
//	 * @see com.lexst.thread.VirtualThread#init()
//	 */
//	@Override
//	public boolean init() {
//		String dir = System.getProperty("user.dir");
//		if (dir.charAt(dir.length() - 1) != File.separatorChar) {
//			dir += File.separatorChar;
//		}
//		this.load(dir + "collect");
//		
//		return true;
//	}
//
//	/* (non-Javadoc)
//	 * @see com.lexst.thread.VirtualThread#process()
//	 */
//	@Override
//	public void process() {
//		Logger.info("CollectPool.process, into...");
//		while (!isInterrupted()) {
//			check();
//			sleep();
//		}
//		Logger.info("CollectPool.process, exit");
//	}
//
//	/* (non-Javadoc)
//	 * @see com.lexst.thread.VirtualThread#finish()
//	 */
//	@Override
//	public void finish() {
//		// TODO Auto-generated method stub
//		
//	}
}