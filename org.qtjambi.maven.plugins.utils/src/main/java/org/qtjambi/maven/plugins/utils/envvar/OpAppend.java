package org.qtjambi.maven.plugins.utils.envvar;

public class OpAppend extends OperationBase implements EnvironmentOperation {
	public OpAppend() {
	}
	public OpAppend(String value) {
		super(value);
	}

	public String operation(String key, String oldValue) {
		String newValue = getValue();
		if(newValue != null)
			return operationStatic(key, oldValue, newValue);
		return null;
	}

	// EnvironmentOperation
	public String operation(String key, String oldValue, String newValue) {
		return operationStatic(key, oldValue, newValue);
	}

	public static String operationStatic(String key, String oldValue, String newValue) {
		if(oldValue != null)
			return oldValue + newValue;
		return newValue;
	}
}
