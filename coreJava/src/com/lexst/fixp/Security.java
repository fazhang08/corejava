/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2009 lexst.com. All rights reserved
 * 
 * fixp security class
 * 
 * @author yj.liang lexst@126.com
 * 
 * @version 1.0 3/15/2009
 * 
 * @see com.lexst.fixp
 * 
 * @license GNU Lesser General Public License (LGPL)
 */
package com.lexst.fixp;

import java.io.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.regex.*;

import org.w3c.dom.*;

import com.lexst.security.*;
import com.lexst.util.range.LongRange;
import com.lexst.xml.*;

public class Security {

	public final static int NONE = 0;
	
	public final static int ADDRESS_MATCH = 1;

	/* client ip address range */
	public final static int ADDRESS_CHECK = 2;

	/* password check */
	public final static int CIPHERTEXT_CHECK = 3;

	/* address and encrypt */
	public final static int DOUBLE_CHECK = 4;

	private int type;

	/* legal ip address range (IPv4) */
	private ArrayList<LongRange> ranges = new ArrayList<LongRange>();

	/* RSA private key (server key) */
	private RSAPrivateKey rsa_prikey;
	
	/* RSA public key (client key) */
	private RSAPublicKey rsa_pubkey;
	
	/**
	 * 
	 */
	public Security() {
		super();
		this.type = Security.NONE;
	}

	/**
	 * set seucrity type
	 * 
	 * @param value
	 */
	public void setType(int value) {
		if (!(Security.NONE <= value && value <= Security.DOUBLE_CHECK)) {
			throw new IllegalArgumentException("invalid type");
		}
		this.type = value;
	}

	/**
	 * get security type
	 * 
	 * @return
	 */
	public int getType() {
		return this.type;
	}
	
	public boolean isNone() {
		return this.type == Security.NONE;
	}
	
	public boolean isAddressMatch () {
		return this.type == Security.ADDRESS_MATCH;
	}
	
	public boolean isAddressCheck() {
		return this.type == Security.ADDRESS_CHECK;
	}
	
	public boolean isCipherCheck() {
		return this.type == Security.CIPHERTEXT_CHECK;
	}
	
	public boolean isDoubleCheck() {
		return this.type == Security.DOUBLE_CHECK;
	}

	/**
	 * RSA private key (server key)
	 * @param key
	 */
	public void setPrivateKey(RSAPrivateKey key) {
		this.rsa_prikey = key;
	}

	public RSAPrivateKey getPrivateKey() {
		return this.rsa_prikey;
	}
	
	/**
	 * RSA public key (client key)
	 * @param key
	 */
	public void setPublicKey(RSAPublicKey key) {
		this.rsa_pubkey = key;
	}
	
	public RSAPublicKey getPublicKey() {
		return this.rsa_pubkey;
	}

	/**
	 * @param begin
	 * @param end
	 */
	public void addLegalIP(long begin, long end) {
		LongRange range = new LongRange(begin, end);
		ranges.add(range);
		java.util.Collections.sort(ranges);
	}

	/**
	 * check ip address range, when ADDRESS_CHECK
	 * 
	 * @param ip
	 * @return
	 */
	public boolean isLegalAddress(long ip) {
		for (LongRange range : ranges) {
			if (range.inside(ip)) return true;
		}
		return false;
	}

	private long parseIP(String s) {
		final String IP = "^\\s*([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\.([0-9]{1,3})\\s*$";
		Pattern pattern = Pattern.compile(IP);
		Matcher matcher = pattern.matcher(s);

		if (matcher.matches()) {
			long value = (Long.parseLong(matcher.group(1)) & 0xFF) << 24;
			value |= (Long.parseLong(matcher.group(2)) & 0xFF) << 16;
			value |= (Long.parseLong(matcher.group(3)) & 0xFF) << 8;
			value |= Long.parseLong(matcher.group(4)) & 0xFF;
			return value;
		}
		return -1;
	}
	
	private boolean parseServer(XMLocal xml, Element server) {
		String type = xml.getXMLValue(server.getElementsByTagName("type"));
		if ("address-match".equalsIgnoreCase(type)) {
			this.setType(Security.ADDRESS_MATCH);
		} else if ("address-check".equalsIgnoreCase(type)) {
			this.setType(Security.ADDRESS_CHECK);
		} else if ("ciphertext-check".equalsIgnoreCase(type)) {
			this.setType(Security.CIPHERTEXT_CHECK);
		} else if ("double-check".equalsIgnoreCase(type)) {
			this.setType(Security.DOUBLE_CHECK);
		} else if ("none".equalsIgnoreCase(type)) {
			this.setType(Security.NONE);
		} else {
			throw new IllegalArgumentException("invalid type " + type);
		}

		// legal address range
		if (this.isAddressCheck() || this.isDoubleCheck()) {
			NodeList list = server.getElementsByTagName("address-range");
			if (list != null && list.getLength() > 0) {
				for (int i = 0; i < list.getLength(); i++) {
					Element element = (Element) list.item(i);
					NodeList subs = element.getElementsByTagName("range");
					int len = subs.getLength();
					for (int j = 0; j < len; j++) {
						Element elem = (Element) subs.item(j);
						String begin = xml.getXMLValue(elem.getElementsByTagName("begin"));
						String end = xml.getXMLValue(elem.getElementsByTagName("end"));

						long b = parseIP(begin);
						long e = parseIP(end);
						this.addLegalIP(b, e);
					}
				}
			}
		}

		if (this.isCipherCheck() || this.isDoubleCheck()) {
			// rsa key
			NodeList list = server.getElementsByTagName("rsa-private-key");
			Element element = (Element) list.item(0);

			list = element.getElementsByTagName("code");
			if (list != null && list.getLength() > 0) {
				element = (Element) list.item(0);
				String modulus = xml.getValue(element, "modulus");
				String exponent = xml.getValue(element, "exponent");
				rsa_prikey = SecureGenerator.buildRSAPrivateKey(modulus, exponent);
			} else {
				list = element.getElementsByTagName("file");
				if (list != null && list.getLength() > 0) {
					String rsafile = xml.getXMLValue(list);
					File file = new File(rsafile);
					if (!(file.exists() && file.isFile())) return false;

					Document doc = xml.loadXMLSource(rsafile);
					list = doc.getElementsByTagName("code");
					element = (Element) list.item(0);
					String modulus = xml.getValue(element, "modulus");
					String exponent = xml.getValue(element, "exponent");
					rsa_prikey = SecureGenerator.buildRSAPrivateKey(modulus, exponent);
				}
			}
		}

		return true;
	}
	
	private boolean parseClient(XMLocal xml, Element client) {
		NodeList list = client.getElementsByTagName("rsa-public-key");
		if (list == null || list.getLength() != 1) return true;
		Element element = (Element) list.item(0);

		list = element.getElementsByTagName("code");
		if (list != null && list.getLength() > 0) {
			element = (Element) list.item(0);
			String modulus = xml.getValue(element, "modulus");
			String exponent = xml.getValue(element, "exponent");
			rsa_pubkey = SecureGenerator.buildRSAPublicKey(modulus, exponent);
		} else {
			list = element.getElementsByTagName("file");
			if (list != null && list.getLength() > 0) {
				String rsafile = xml.getXMLValue(list);
				File file = new File(rsafile);
				if (!(file.exists() && file.isFile())) return false;

				Document doc = xml.loadXMLSource(rsafile);
				list = doc.getElementsByTagName("code");
				element = (Element) list.item(0);
				String modulus = xml.getValue(element, "modulus");
				String exponent = xml.getValue(element, "exponent");
				rsa_pubkey = SecureGenerator.buildRSAPublicKey(modulus, exponent);
			}
		}

		return true;
	}

	public boolean parse(String filename) {
		File file = new File(filename);
		if (!(file.exists() && file.isFile())) return false;

		XMLocal xml = new XMLocal();
		Document doc = xml.loadXMLSource(file);
		if(doc == null) return false;
		
		NodeList list = doc.getElementsByTagName("server");
		if (list == null || list.getLength() < 1) return false;
		if (!parseServer(xml, (Element) list.item(0))) return false;

		list = doc.getElementsByTagName("client");
		if (list != null && list.getLength() == 1) {
			if (!parseClient(xml, (Element) list.item(0))) return false;
		}

		return true;
	}

//	public static void main(String[] args) {
//		String filename = "D:/lexst/src/com/lexst/fixp/security.xml";
//		Security s = new Security();
//		s.parse(filename);
//		System.out.printf("RSA Key is %s\n", s.getPrivateKey()!=null ? "handle" : "NULL");
//		
//		Security ss = s;
//		System.out.printf("RSA Key is %s\n", ss.getPrivateKey()!=null ? "handle" : "NULL");
//	}

}