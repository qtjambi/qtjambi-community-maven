package org.qtjambi.maven.plugins.utils.envvar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.qtjambi.maven.plugins.utils.shared.Utils;

public class OpPathRemoveMissing extends OperationBase implements EnvironmentOperation, EnvironmentPathOperation {
	public OpPathRemoveMissing() {
	}
	public OpPathRemoveMissing(String value) {
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
		String[] valueA = Utils.safeStringArraySplit(oldValue, File.pathSeparator);
		valueA = operationPathStatic(key, valueA, newValue);
		if(valueA != null)
			return Utils.stringConcat(valueA, File.pathSeparator);
		return null;
	}

	// EnvironmentPathOperation
	public String[] operationPath(String key, String[] oldValue, String newValue) {
		return operationPathStatic(key, oldValue, newValue);
	}

	public static String[] operationPathStatic(String key, String[] oldValue, String newValue) {
		if(oldValue != null) {
			List<String> newList = new ArrayList<String>();
			boolean modified = false;
			for(String path : oldValue) {
				File file = new File(path);
				if(file.exists() && file.isDirectory()) {
					newList.add(path);
				} else {
					modified = true;
					// FIXME: Want a way to log this action
				}
			}
			if(modified)
				return newList.toArray(new String[newList.size()]);
			return oldValue;	// no change
		}
		return oldValue;	// no change
	}
}
