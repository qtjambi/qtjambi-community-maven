package org.qtjambi.buildtool.maven;

import java.util.Map;

public interface IEnvironmentResolver {
	void applyEnvironmentVariables(Map<String,String> envvar);

	String resolveCommand(String file);
}
