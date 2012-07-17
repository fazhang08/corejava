/**
 *
 */
package com.lexst.db.field;


public abstract class VariableField extends Field {
	private static final long serialVersionUID = 1L;
	
	// compress type, default is 0, not encode
	protected byte packing;
	// index limit size(only raw, char, nchar, wchar)
	protected int indexSize;

	/**
	 * @param type
	 */
	public VariableField(byte type) {
		super(type);
		this.indexSize = 0;
		this.packing = WordField.NOT_PACKING;
	}

	/**
	 * @param field
	 */
	public VariableField(VariableField field) {
		super(field);
		this.indexSize = field.indexSize;
		this.packing = field.packing;
	}
	
	/**
	 * @param type
	 * @param columnId
	 * @param name
	 */
	public VariableField(byte type, short columnId, String name) {
		super(type, columnId, name);
		this.indexSize = 0;
		this.packing = WordField.NOT_PACKING;
	}

	public void setPacking(byte b) {
		this.packing = b;
	}
	public byte getPacking() {
		return this.packing;
	}

	public void setIndexSize(int size) {
		this.indexSize = size;
	}
	public int getIndexSize() {
		return this.indexSize;
	}

}
