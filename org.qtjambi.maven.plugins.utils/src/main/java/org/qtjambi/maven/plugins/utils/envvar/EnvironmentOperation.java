package org.qtjambi.maven.plugins.utils.envvar;

public interface EnvironmentOperation {
	String operation(String key, String oldValue);
	String operation(String key, String oldValue, String newValue);
}
