/**
 *
 */
package com.lexst.live.window.query;

import javax.swing.tree.DefaultMutableTreeNode;

public class SelectMutableTreeNode extends DefaultMutableTreeNode {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public final static int SCHEMA = 1;
	public final static int TABLE = 2;

	public final static int PRIMARY_FIELD = 3;
	public final static int Index_FIELD = 4;
	public final static int FIELD = 5;

	private int type;

	private String tooltip;

	/**
	 *
	 */
	public SelectMutableTreeNode() {
		super();
		this.type = 0;
	}

	/**
	 * @param userObject
	 */
	public SelectMutableTreeNode(Object userObject) {
		super(userObject);
		this.type = 0;
	}

	/**
	 * @param userObject
	 */
	public SelectMutableTreeNode(Object userObject, int type) {
		super(userObject);
		setType(type);
	}

	/**
	 * @param userObject
	 * @param allowsChildren
	 */
	public SelectMutableTreeNode(Object userObject, boolean allowsChildren) {
		super(userObject, allowsChildren);
		this.type = 0;
	}

	/**
	 * @param userObject
	 * @param allowsChildren
	 * @param type
	 */
	public SelectMutableTreeNode(Object userObject, boolean allowsChildren, int type) {
		super(userObject, allowsChildren);
		setType(type);
	}

	public void setType(int i) {
		this.type = i;
	}

	public int getType() {
		return this.type;
	}

	public void setToolTip(String s) {
		this.tooltip = s;
	}
	public String getToolTip() {
		return this.tooltip;
	}
}
