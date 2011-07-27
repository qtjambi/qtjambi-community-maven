package org.qtjambi.maven.plugins.utils.envvar;

import java.io.File;

import org.qtjambi.maven.plugins.utils.shared.Utils;

/**
 * This class exists to correct the File.separator character used
 * i.e. convert from Windows <> Unix.
 * @author Darryl
 *
 */
public class OpPathCorrectSeparator extends OperationBase implements EnvironmentOperation, EnvironmentPathOperation {
	public OpPathCorrectSeparator() {
		setValue(File.separator);
	}
	public OpPathCorrectSeparator(String separator) {
		super(separator);
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
		if(oldValue != null) {
			String fromSeparator = oppositeSeparator(newValue);		// newValue is expected to contain separator
			oldValue = transform(oldValue, fromSeparator, newValue);
			return oldValue;
		}
		return newValue;
	}

	// EnvironmentPathOperation
	public String[] operationPath(String key, String[] oldValue, String newValue) {
		return operationPathStatic(key, oldValue, newValue);
	}

	public static String[] operationPathStatic(String key, String[] oldValue, String newValue) {
		if(oldValue != null) {
			String fromSeparator = oppositeSeparator(newValue);		// newValue is expected to contain separator
			final int len = oldValue.length;
			for(int i = 0; i < len; i++)
				oldValue[i] = transform(oldValue[i], fromSeparator, newValue);
		}
		return oldValue;
	}

	public static String oppositeSeparator(String separator) {
		String otherSeparator = null;
		if(separator.equals("/"))
			otherSeparator = "\\";
		else if(separator.equals("\\"))
			otherSeparator = "/";
		return otherSeparator;
	}

	public static String transform(String s, String fromSeparator, String toSeparator) {
		// We only transform when the separator string is well-known and there
		// target string does not contain any of the  
		if(fromSeparator == null)
			return s;
		if(toSeparator == null)
			return s;

		if(s.indexOf(toSeparator) >= 0)
			return s;		// value already contains at least one toSeparator

		return s.replace(fromSeparator, toSeparator);
	}
}
