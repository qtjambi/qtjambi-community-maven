package org.qtjambi.maven.plugins.utils.envvar;

import java.io.File;

import org.qtjambi.maven.plugins.utils.shared.Utils;

public class OpPathPrependIfUnique extends OperationBase implements EnvironmentOperation, EnvironmentPathOperation {
	public OpPathPrependIfUnique() {
	}
	public OpPathPrependIfUnique(String value) {
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
		String[] valueA = Utils.safeStringArraySplit(oldValue, File.pathSeparator);
		valueA = operationPathStatic(key, valueA, newValue);
		if(valueA != null)
			return Utils.stringConcat(valueA, File.pathSeparator);
		return null;
	}

	public static String operationStatic(String key, String oldValue, String newValue) {
		if(oldValue != null)
			return oldValue + File.pathSeparator + newValue;
		return newValue;
	}

	// EnvironmentPathOperation
	public String[] operationPath(String key, String[] oldValue, String newValue) {
		return operationPathStatic(key, oldValue, newValue);
	}

	public static String[] operationPathStatic(String key, String[] oldValue, String newValue) {
		if(oldValue != null) {
			if(Utils.stringArrayContains(oldValue, newValue) == false)
				return Utils.stringArrayPrepend(oldValue, newValue);
			return oldValue;
		} else {
			return new String[] { newValue };
		}
	}
}
