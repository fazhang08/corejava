/**
 * 
 */
package com.lexst.algorithm;

import java.io.*;
import java.util.*;
import java.util.jar.*;

import org.w3c.dom.*;

import com.lexst.log.client.*;
import com.lexst.pool.*;
import com.lexst.util.naming.*;
import com.lexst.xml.*;

/**
 * TASK 规则
 * 1. 在jar必须有一个TASK-INF目录,这个目录下放一个tasks.xml,做为配置文件
 * 2. "TASK-INF"必须全大写
 * 3. "tasks.xml"必须全小写 
 * 4. tasks.xml里面的配置规则见"task"标准
 * 5. 与task中的类相关的类文件可以放在不同包中,但是必须能被找到
 * 6. 如果在启动目录下定义"task"目录,运行时自动加载里面的配置文件
 */
public class TaskPool extends Pool {
	
	/* jar task directory */
	private final static String TAG = "TASK-INF/tasks.xml";

	/* event handle */
	private TaskEventListener eventListener;
	
	/* task root directory */
	private String root;
	
	/* self loader */
	private TaskClassLoader loader = new TaskClassLoader();
	
	/* object naming -> project object */
	private Map<Naming, Project> mapProject = new HashMap<Naming, Project>();
	
	/* object naming  -> jar file*/
	private Map<Naming, String> mapNaming = new HashMap<Naming, String>();

	/* filename -> file object */
	private Map<String, File> mapFile = new HashMap<String, File>();

	/* directory array */
	private List<File> paths = new ArrayList<File>();
	
	/* update status */
	private boolean update_naming;

	/**
	 * 
	 */
	protected TaskPool() {
		super();
		this.update_naming = false;
	}

	/**
	 * @return
	 */
	public ClassLoader getClassLoader() {
		return this.loader;
	}
	
	/**
	 * naming listener
	 * @param arg
	 */
	public void setTaskEventListener(TaskEventListener arg) {
		this.eventListener = arg;
	}

	public TaskEventListener getTaskEventListener() {
		return this.eventListener;
	}
	
	/**
	 * task directory
	 * @param path
	 */
	public void setRoot(String path) {
		this.root = path;
	}

	public String getRoot() {
		return this.root;
	}

	/**
	 * naming list
	 * @return
	 */
	public Set<Naming> listNaming() {
		return mapProject.keySet();
	}
	
	public Project findProject(String naming) {
		return mapProject.get(new Naming(naming));
	}

	public Project findProject(Naming naming) {
		return mapProject.get(naming);
	}
	
	/**
	 * load ".jar" file
	 * @param path
	 * @return
	 */
	public List<String> load(String path) {		
		File root = new File(path);
		if (!(root.exists() && root.isDirectory())) {
			this.remove_path(path);
			return null;
		}

		ArrayList<String> array = new ArrayList<String>();

		File[] subs = root.listFiles();
		for(File file : subs) {
			if(file.isHidden()) continue;
			// load jar file
			if (file.isDirectory()) {
				List<String> list = loadPath(file);
				if (list != null) array.addAll(list);
			} else if (file.isFile()) {
				String filename = file.getAbsolutePath();
				if (filename.toLowerCase().endsWith(".jar")) {
					array.add(filename);
				}
			}
		}
		
		ArrayList<String> a2 = new ArrayList<String>();
		for (String filename : array) {
			File file1 = new File(filename);
			File file2 = mapFile.get(filename);
			if (file2 == null) {
				a2.add(filename);
			} else if (file1.lastModified() != file2.lastModified() || file1.length() != file2.length()) {
				this.remove_file(filename);
				a2.add(filename);
			}
		}
		array.clear();
		array.addAll(a2);

		// load jar file
		for(String filename : array) {
			loader.addJar(filename);
		}
		// resolve jar file
		ArrayList<String> names = new ArrayList<String>();
		for(String filename : array) {
			List<String> list = addFile(filename);
			if(list != null) names.addAll(list);
			this.add_filename(filename);
		}
		
		add_path(root);
		return names;
	}

	/**
	 * resolve filename
	 * @param root
	 * @return
	 */
	private List<String> loadPath(File root) {
		ArrayList<String> array = new ArrayList<String>();

		File[] subs = root.listFiles();
		for (File file : subs) {
			if (file.isHidden()) continue;
			// load jar file
			if (file.isDirectory()) {
				List<String> list = loadPath(file);
				if(list != null) array.addAll(list);
			} else if (file.isFile()) {
				String filename = file.getAbsolutePath();
				if (filename.toLowerCase().endsWith(".jar")) {
					array.add(filename);
				}
			}
		}

		return array;
	}

	/**
	 * @param filename
	 * @return
	 */
	private List<String> addFile(String filename) {
		File file = new File(filename);
		ByteArrayOutputStream buff = new ByteArrayOutputStream();
		try {
			FileInputStream fi = new FileInputStream(file);
			JarInputStream in = new JarInputStream(fi);
			while (true) {
				JarEntry entry = in.getNextJarEntry();
				if (entry == null) break;

				String name = entry.getName();
//				if (name.endsWith(TaskPool.TAG)) {
				if (name.equals(TaskPool.TAG)) {
					byte[] b = new byte[10240];
					while (true) {
						int len = in.read(b, 0, b.length);
						if (len == -1) break;
						buff.write(b, 0, len);
					}
					break;
				}
			}
			in.close();
			fi.close();
		} catch (IOException exp) {
			Logger.error(exp);
		}

		if(buff.size() == 0) return null;

		// resolve xml
		byte[] b = buff.toByteArray();
		XMLocal xml = new XMLocal();
		Document document = xml.loadXMLSource(b);
		if (document == null) {
			return null;
		}
		
		List<String> array = new ArrayList<String>();
		NodeList list =	document.getElementsByTagName("task");
		int size = list.getLength();
		for (int i = 0; i < size; i++) {
			Element elem = (Element) list.item(i);
			// task naming
			String name = xml.getValue(elem, "naming");
			// project class name
			String project_class = xml.getValue(elem, "project-class");
			// task class name
			String task_class = xml.getValue(elem, "task-class");
			// resource
			String resource = xml.getValue(elem, "resource");

			boolean success = addProject(filename, name, task_class, project_class, resource);
			if(success) array.add(name);
		}
		return array;
	}
	
	/**
	 * @param filename
	 * @param name
	 * @param task_class
	 * @param project_class
	 * @param resource
	 * @return
	 */
	private boolean addProject(String filename, String name, String task_class, String project_class, String resource) {
		boolean success = false;
		Naming naming = new Naming(name);
		
		super.lockSingle();
		try {
			Class<?> clss = Class.forName(project_class, true, loader);
			Project project = (Project) clss.newInstance();

			project.setNaming(naming);
			project.setTaskClass(task_class);
			project.setResource(resource);
			
			mapProject.put(naming, project);
			mapNaming.put(naming, filename);
			success = true;
		} catch (ClassNotFoundException exp) {
			Logger.error(exp);
		} catch (IllegalAccessException exp) {
			Logger.error(exp);
		} catch (InstantiationException exp) {
			Logger.error(exp);
		} finally {
			super.unlockSingle();
		}
		
		if(success) {
			Logger.info("TaskPool.addProject, naming:%s, task class:%s, project class:%s",
					name, task_class, project_class);
		}
		
		return success;
	}
	
	/**
	 * find naming task
	 * @param naming
	 * @return
	 */
	protected BasicTask findTask(String naming) {
		return findTask(new Naming(naming));
	}
	
	/**
	 * find naming task
	 * 
	 * @param naming
	 * @return
	 */
	protected BasicTask findTask(Naming naming) {
		Project project = null;
		super.lockMulti();
		try {
			project = mapProject.get(naming);
		} catch (Throwable exp) {
			Logger.fatal(exp);
		} finally {
			super.unlockMulti();
		}

		try {
			if (project != null) {
				String task_class = project.getTaskClass();
				BasicTask task = (BasicTask) Class.forName(task_class, true, loader).newInstance();
				task.setProject(project);
				return task;
			}
		} catch (InstantiationException exp) {
			Logger.error(exp);
		} catch (IllegalAccessException exp) {
			Logger.error(exp);
		} catch (ClassNotFoundException exp) {
			Logger.error(exp);
		}
		return null;
	}

	/**
	 * find a class
	 * 
	 * @return
	 */
	public Class<?> findClass(String class_name) {
		try {
			return Class.forName(class_name, true, loader);
		} catch (ClassNotFoundException exp) {
			Logger.error(exp);
		}
		return null;
	}

	/**
	 * @param filename
	 * @return
	 */
	private int remove_file(String filename) {
		List<Naming> array = new ArrayList<Naming>();
		super.lockSingle();
		try {
			for(Naming naming: mapNaming.keySet()) {
				String filename2 = mapNaming.get(naming);
				if(filename.equalsIgnoreCase(filename2)) {
					array.add(naming);
				}
			}
			for(Naming naming: array) {
				mapNaming.remove(naming);
				mapProject.remove(naming);
			}
			mapFile.remove(filename);
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			super.unlockSingle();
		}
		return array.size();
	}
	
	private int remove_path(String path) {
		if (path.charAt(path.length() - 1) != File.separatorChar) path += File.separator;
		
		List<String> array = new ArrayList<String>();
		super.lockSingle();
		try {
			for (String filename : mapFile.keySet()) {
				int index = filename.indexOf(File.separatorChar);
				String sub = filename.substring(index + 1);
				if (path.equalsIgnoreCase(sub)) {
					array.add(filename);
				}
			}
		} catch (Throwable exp) {

		} finally {
			super.unlockSingle();
		}
		
		int count = 0;
		for(String filename : array) {
			int size = this.remove_file(filename);
			count += size;
		}
		return count;
	}
	
	/**
	 * save jar-filename record
	 * @param filename
	 */
	private void add_filename(String filename) {
		super.lockSingle();
		try {
			mapFile.put(filename, new File(filename));
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			super.unlockSingle();
		}
	}
	
	/**
	 * @param path
	 */
	private void add_path(File path) {
		super.lockSingle();
		try {
			if (!paths.contains(path)) {
				paths.add(path);
			}
		} catch (Throwable exp) {
			Logger.error(exp);
		} finally {
			super.unlockSingle();
		}
	}

	/**
	 * check resource time (file time and file size)
	 */
	private void check() {
		// remove or update jar file
		Map<String, File> map1 = new HashMap<String, File>(mapFile);
		for (String filename : map1.keySet()) {
			File file1 = map1.get(filename);
			// cannot find, delete it
			if(!file1.exists()) {
				this.remove_file(filename);
				continue;
			}
			
			// not match, update it
			File file2 = new File(filename);
			if (file1.lastModified() != file2.lastModified() || file1.length() != file2.length()) {
				// delete old jar
				this.remove_file(filename);
				// add new jar
				addFile(filename);
				// save filename
				add_filename(filename);
			}
		}
		
		// new jar file
		List<String> a = new ArrayList<String>();
		ArrayList<File> a2 = new ArrayList<File>(paths);
		for(File path : a2) {
			List<String> list = this.loadPath(path);
			if(list == null) continue;
			for(String filename : list) {
				if(mapFile.containsKey(filename)) continue;
				// save file
				a.add(filename);
			}
		}
		
		// add new jar
		for(String filename : a) {
			loader.addJar(filename);
		}
		// save new jar
		for(String filename : a) {
			addFile(filename);
			add_filename(filename);
		}
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#init()
	 */
	@Override
	public boolean init() {
		if (root == null) {
			String bin = System.getProperty("user.dir");
			if (bin.charAt(bin.length() - 1) == File.separatorChar) {
				bin = bin.substring(0, bin.length() - 1);
			}
			int last = bin.lastIndexOf(File.separatorChar);
			if (last > -1) {
				bin = bin.substring(0, last + 1);
			}
			root = bin + "task";
		}
		Logger.info("TaskPool.init, task directory:%s", root);
		// load task
		this.load(root);
	
		return true;
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#process()
	 */
	@Override
	public void process() {
		Logger.info("TaskPool.process, into...");
		while (!isInterrupted()) {
			check();
			sleep();
			// update task naming
			if (eventListener != null && update_naming) {
				update_naming = false;
				eventListener.updateTask();
			}
		}
		Logger.info("TaskPool.process, exit");
	}

	/* (non-Javadoc)
	 * @see com.lexst.thread.VirtualThread#finish()
	 */
	@Override
	public void finish() {
		// TODO Auto-generated method stub		
	}

}