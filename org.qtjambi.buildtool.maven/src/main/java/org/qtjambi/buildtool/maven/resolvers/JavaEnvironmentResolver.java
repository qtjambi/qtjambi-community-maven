package org.qtjambi.buildtool.maven.resolvers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.qtjambi.buildtool.maven.IEnvironmentResolver;
import org.qtjambi.buildtool.maven.Platform;
import org.qtjambi.buildtool.maven.utils.Utils;

public class JavaEnvironmentResolver extends DefaultEnvironmentResolver implements IEnvironmentResolver {

	private String home;
	private Map<String,String> commandMap;

	private List<String> pathAppend;
	private List<String> ldLibraryPathAppend;
	private List<String> dyldLibraryPathAppend;
	private Map<String,String> envvarMap;

	public JavaEnvironmentResolver(Platform platform) {
		super(platform);
		commandMap = new HashMap<String,String>();
	}

	// envvarMap
	//  unset JAVA_OPTS
	//  unset JAVAFX_HOME
	//  unset MAVEN
	//  unset MAVEN_HOME
	//  unset ANT
	/// unset ANT_HOME

	public void applyEnvironmentVariables(Map<String, String> envvar) {
		super.applyEnvironmentVariables(envvar);
		applyEnvironmentVariablesNoParent(envvar);
	}

	public void applyEnvironmentVariablesNoParent(Map<String, String> envvar) {
		if(envvarMap != null)
			Utils.applyEnvVarMap(envvar, envvarMap);

		if(pathAppend != null)
			Utils.applyEnvVarPath(envvar, K_PATH, pathAppend);

		if(ldLibraryPathAppend != null)
			Utils.applyEnvVarPath(envvar, K_LD_LIBRARY_PATH, ldLibraryPathAppend);

		if(dyldLibraryPathAppend != null)
			Utils.applyEnvVarPath(envvar, K_DYLD_LIBRARY_PATH, dyldLibraryPathAppend);
	}
}
