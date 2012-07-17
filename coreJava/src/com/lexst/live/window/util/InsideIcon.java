package com.lexst.live.window.util;

import java.awt.Image;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public final class InsideIcon {

	public InsideIcon() {
		super();
	}

	/**
	 * JAR image URL: jar:file:/E:/lexst/lib/spider.jar!/com/lexst/spider/app.gif
	 * @param cls
	 * @param name
	 * @return
	 */
	public static ImageIcon getIcon(Class<?> cls, String name) {
		URL url = cls.getResource(name);
		if (url == null) {
			return null;
		}
		try {
			Image img = ImageIO.read(url);
			return new ImageIcon(img);
		} catch (IOException exp) {

		} catch (Throwable exp) {
			exp.printStackTrace();
		}
		return null;
	}

	public static ImageIcon getIcon(Class<?> cls, String name, int width,
			int height) {
		ImageIcon icon = InsideIcon.getIcon(cls, name);
		if (icon == null) {
			return null;
		}
		Image img = icon.getImage().getScaledInstance(width, height,
				Image.SCALE_SMOOTH);
		return new ImageIcon(img);
	}
}