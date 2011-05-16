package org.qtjambi.maven.plugins.utils.resolvers;

import java.util.HashMap;
import java.util.Map;

import org.qtjambi.maven.plugins.utils.IEnvironmentResolver;
import org.qtjambi.maven.plugins.utils.Platform;
import org.qtjambi.maven.plugins.utils.envvar.EnvironmentEditor;
import org.qtjambi.maven.plugins.utils.envvar.EnvironmentPathEditor;

public class JavaEnvironmentResolver extends DefaultEnvironmentResolver implements IEnvironmentResolver {

	private String home;
	private Map<String,String> commandMap;

	private EnvironmentPathEditor pathEditor;
	private EnvironmentEditor envvarEditor;

	public JavaEnvironmentResolver(Platform platform) {
		super(platform);
		commandMap = new HashMap<String,String>();
		pathEditor = new EnvironmentPathEditor();
		envvarEditor = new EnvironmentEditor();
	}

	// envvarMap
	//  unset JAVA_OPTS
	//  unset JAVAFX_HOME
	//  unset MAVEN
	//  unset MAVEN_HOME
	//  unset ANT
	/// unset ANT_HOME

	@Override
	public void applyEnvironmentVariables(Map<String, String> envvar) {
		super.applyEnvironmentVariables(envvar);
		applyEnvironmentVariablesNoParent(envvar);
	}

	@Override
	public void applyEnvironmentVariablesNoParent(Map<String, String> envvar) {
		envvarEditor.apply(envvar);
		pathEditor.apply(envvar, K_PATH);
	}
}
