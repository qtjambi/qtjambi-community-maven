package org.qtjambi.maven.plugins.utils.envvar;

import org.qtjambi.maven.plugins.utils.shared.Utils;

public class OpPathRemove extends OperationBase implements EnvironmentOperation, EnvironmentPathOperation {
	public OpPathRemove() {
	}
	public OpPathRemove(String value) {
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
		if(oldValue != null) {
			if(oldValue.equals(newValue))
				return null;
		}
		return oldValue;
	}

	// EnvironmentPathOperation
	public String[] operationPath(String key, String[] oldValue, String newValue) {
		return operationPathStatic(key, oldValue, newValue);
	}

	public static String[] operationPathStatic(String key, String[] oldValue, String newValue) {
		if(oldValue != null) {
			if(Utils.stringArrayContains(oldValue, newValue))
				return Utils.stringArrayRemove(oldValue, newValue);
		}
		return oldValue;	// no change
	}
}
