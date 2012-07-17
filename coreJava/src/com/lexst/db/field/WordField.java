/**
 *
 */
package com.lexst.db.field;


public abstract class WordField extends VariableField {
	private static final long serialVersionUID = 1L;
	
	// case sensitive, default is true
	protected boolean sentient;
	// like mode, default is false
	protected boolean like;

	/**
	 *
	 * @param type
	 */
	public WordField(byte type) {
		super(type);
		this.sentient = true;
		this.like = false;
	}
	
	/**
	 * @param field
	 */
	public WordField(WordField field) {
		super(field);
		this.sentient = field.sentient;
		this.like = field.like;
	}

	/**
	 * @param type
	 * @param columnId
	 * @param name
	 */
	public WordField(byte type, short columnId, String name) {
		super(type, columnId, name);
	}

	public void setSentient(boolean b) {
		this.sentient = b;
	}
	public boolean isSentient() {
		return this.sentient;
	}

	public void setLike(boolean b) {
		this.like = b;
	}
	public boolean isLike() {
		return this.like;
	}
}