package org.qtjambi.maven.plugins.utils.envvar;

public class OperationBase {
	private String value;

	public OperationBase() {
	}
	public OperationBase(String value) {
		setValue(value);
	}

	public String getValue() {
		return value;
	}
	protected void setValue(String value) {
		this.value = value;
	}
}
