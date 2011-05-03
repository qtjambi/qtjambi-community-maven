package org.qtjambi.maven.plugins.utils.envvar;

public interface EnvironmentPathOperation {
	String[] operationPath(String key, String[] oldValue);
	String[] operationPath(String key, String[] oldValue, String newValue);
}
