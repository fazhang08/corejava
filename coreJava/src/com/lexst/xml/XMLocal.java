/**
 *
 */
package com.lexst.xml;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.*;

public class XMLocal {

	/**
	 *
	 */
	public XMLocal() {
		super();
	}

	/**
	 *
	 * @param parent
	 * @param tag
	 * @return
	 */
	public String getValue(Element parent, String tag) {
		NodeList list = parent.getElementsByTagName(tag);
		if(list == null) return "";
		Element elem = (Element) list.item(0);
		if(elem == null) return "";
		return getString(elem.getTextContent());
	}

	public String getString(String s) {
		if (s == null) return "";
		return s.trim();
	}

	/**
	 * 返回XML配置表某节点中第一项数据
	 *
	 * @param NodeList
	 * @return String
	 */
	public String getXMLValue(NodeList nodes) {
		if (nodes == null || nodes.getLength() < 1) return "";
		Element elem = (Element) nodes.item(0);
		if (elem == null) return "";
		return getString(elem.getTextContent());
	}

	public String[] getXMLValues(NodeList nodes) {
		int size = nodes.getLength();
		ArrayList<String> a = new ArrayList<String>(size);
		for (int i = 0; i < size; i++) {
			Element elem = (Element) nodes.item(i);
			if (elem == null) continue;
			NodeList c = elem.getChildNodes();
			if (c.getLength() < 1) continue;
			Node node = c.item(0);
			String s = getString(node.getNodeValue());
			a.add(s);
		}

		if(a.isEmpty()) return null;
		String[] s = new String[a.size()];
		return a.toArray(s);
	}

	/**
	 * 根据文件名,装载XML配置文件
	 * @param text
	 * @return
	 */
	public Document loadXMLSource(String filename) {
		File file = new File(filename);
		return loadXMLSource(file);
	}

	/**
	 * load xml source
	 * @param file
	 * @return
	 */
	public Document loadXMLSource(File file) {
		if(!file.exists() || file.isDirectory()) {
			return null;
		}
		byte[] b = new byte[(int)file.length()];
		try {
			FileInputStream in = new FileInputStream(file);
			in.read(b);
			in.close();
			return loadXMLSource(b);
		}catch(IOException exp) {
			exp.printStackTrace();
		}
		return null;
	}

	/**
	 * load xml byte
	 * @param b
	 * @return
	 */
	public Document loadXMLSource(byte[] b) {
		try {
			ByteArrayInputStream bin = new ByteArrayInputStream(b);
			// 生成对象
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			return builder.parse(bin);
		}catch(IOException exp) {
			exp.printStackTrace();
		}catch(ParserConfigurationException exp) {
			exp.printStackTrace();
		}catch(SAXException exp) {
			exp.printStackTrace();
		}
		return null;
	}

}