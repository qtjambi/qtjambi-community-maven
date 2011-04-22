package org.qtjambi.buildtool.maven;

import java.io.File;
import java.util.Map;

public interface IEnvironmentResolver {
	void applyEnvironmentVariables(Map<String, String> envvar);
	void applyEnvironmentVariablesNoParent(Map<String,String> envvar);

	String resolveCommand(File dir, String file);
}
