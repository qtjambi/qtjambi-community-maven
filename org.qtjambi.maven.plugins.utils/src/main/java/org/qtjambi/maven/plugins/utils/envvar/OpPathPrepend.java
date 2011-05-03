package org.qtjambi.maven.plugins.utils.envvar;

import java.io.File;

import org.qtjambi.maven.plugins.utils.shared.Utils;

public class OpPathPrepend extends OperationBase implements EnvironmentOperation, EnvironmentPathOperation {
	public OpPathPrepend() {
	}
	public OpPathPrepend(String value) {
		super(value);
	}

	public String operation(String key, String oldValue) {
		String newValue = getValue();
		if(newValue != null)
			return operationStatic(key, oldValue, newValue);
		return null;
	}

	public String[] operationPath(String key, String[] oldValue) {
		String newValue = getValue();
		if(newValue != null)
			return operationPathStatic(key, oldValue, newValue);
		return null;
	}

	// EnvironmentOperation
	public String operation(String key, String oldValue, String newValue) {
		return operationStatic(key, oldValue, newValue);
	}

	public static String operationStatic(String key, String oldValue, String newValue) {
		if(oldValue != null)
			return newValue + File.pathSeparator + oldValue;
		return newValue;
	}

	// EnvironmentPathOperation
	public String[] operationPath(String key, String[] oldValue, String newValue) {
		return operationPathStatic(key, oldValue, newValue);
	}

	public static String[] operationPathStatic(String key, String[] oldValue, String newValue) {
		return Utils.safeStringArrayPrepend(oldValue, newValue);
	}
}
