package com.lexst.live.window.query;


import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.*;

public class RankTreeCellRenderer extends JLabel implements TreeCellRenderer {
	private static final long serialVersionUID = 1L;


	// database icon
	private Icon dbIcon;
	// table icon
	private Icon tableIcon;
	// field icon
	private Icon primaryIcon;
	private Icon indexIcon;
	private Icon fieldIcon;

    // Colors
    /** Color to use for the foreground for selected nodes. */
    protected Color textSelectionColor;

    /** Color to use for the foreground for non-selected nodes. */
    protected Color textNonSelectionColor;

    /** Color to use for the background when a node is selected. */
    protected Color backgroundSelectionColor;

    /** Color to use for the background when the node isn't selected. */
    protected Color backgroundNonSelectionColor;

    protected boolean selected;
    protected boolean hasFocus;

    /**
     * @param db
     * @param table
     * @param primary
     * @param index
     * @param field
     */
	public RankTreeCellRenderer(Icon db, Icon table, Icon primary, Icon index, Icon field) {
		super();
		this.dbIcon = db;
		this.tableIcon = table;
		this.primaryIcon = primary;
		this.indexIcon = index;
		this.fieldIcon = field;
		this.init();
		this.setIconTextGap(3);
		this.setBorder(new EmptyBorder(1, 2, 1, 2));
	}

	private void init() {
		setHorizontalAlignment(JLabel.LEFT);

		setTextSelectionColor(UIManager.getColor("Tree.selectionForeground"));
		setTextNonSelectionColor(UIManager.getColor("Tree.textForeground"));
		setBackgroundSelectionColor(UIManager.getColor("Tree.selectionBackground"));
		setBackgroundNonSelectionColor(UIManager.getColor("Tree.textBackground"));
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

		String stringValue = tree.convertValueToText(value, selected, expanded,
				leaf, row, hasFocus);

		this.selected = selected;
		this.hasFocus = hasFocus;
		setText(stringValue);

		if (hasFocus || selected) {
			setForeground(textSelectionColor);
			setBackground(backgroundSelectionColor);
		} else {
			setForeground(textNonSelectionColor);
			setBackground(backgroundNonSelectionColor);
		}

		if (value.getClass() == SelectMutableTreeNode.class) {
			SelectMutableTreeNode node = (SelectMutableTreeNode) value;
			switch (node.getType()) {
			case SelectMutableTreeNode.SCHEMA:
				this.setIcon(dbIcon);
				break;
			case SelectMutableTreeNode.TABLE:
				this.setIcon(tableIcon);
				break;
			case SelectMutableTreeNode.PRIMARY_FIELD:
				this.setIcon(this.primaryIcon);
				break;
			case SelectMutableTreeNode.Index_FIELD:
				this.setIcon(this.indexIcon);
				break;
			case SelectMutableTreeNode.FIELD:
				this.setIcon(fieldIcon);
				break;
			}
			String tip = node.getToolTip();
			if (tip != null && !tip.isEmpty()) {
				//this.setToolTipText(tip);
				//tree.setToolTipText(tip);
			}
		}

		setComponentOrientation(tree.getComponentOrientation());
		setEnabled(tree.isEnabled());
		setFont(tree.getFont());
		setOpaque(true);
		return this;
	}

    /**
	 * Sets the color the text is drawn with when the node is selected.
	 */
	public void setTextSelectionColor(Color newColor) {
		textSelectionColor = newColor;
	}

	/**
	 * Returns the color the text is drawn with when the node is selected.
	 */
	public Color getTextSelectionColor() {
		return textSelectionColor;
	}

	/**
	 * Sets the color the text is drawn with when the node isn't selected.
	 */
	public void setTextNonSelectionColor(Color newColor) {
		textNonSelectionColor = newColor;
	}

	/**
	 * Returns the color the text is drawn with when the node isn't selected.
	 */
	public Color getTextNonSelectionColor() {
		return textNonSelectionColor;
	}

	/**
	 * Sets the color to use for the background if node is selected.
	 */
	public void setBackgroundSelectionColor(Color newColor) {
		backgroundSelectionColor = newColor;
	}

	/**
	 * Returns the color to use for the background if node is selected.
	 */
	public Color getBackgroundSelectionColor() {
		return backgroundSelectionColor;
	}

	/**
	 * Sets the background color to be used for non selected nodes.
	 */
	public void setBackgroundNonSelectionColor(Color newColor) {
		backgroundNonSelectionColor = newColor;
	}

	/**
	 * Returns the background color to be used for non selected nodes.
	 */
	public Color getBackgroundNonSelectionColor() {
		return backgroundNonSelectionColor;
	}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void validate() {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    *
    * @since 1.5
    */
    public void invalidate() {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void revalidate() {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void repaint(long tm, int x, int y, int width, int height) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void repaint(Rectangle r) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    *
    * @since 1.5
    */
    public void repaint() {}

    public void updateUI() {
    	super.updateUI();
    	this.init();
    }
}