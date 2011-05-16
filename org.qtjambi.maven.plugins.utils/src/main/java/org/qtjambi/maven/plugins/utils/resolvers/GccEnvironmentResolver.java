package org.qtjambi.maven.plugins.utils.resolvers;

import java.util.HashMap;
import java.util.Map;

import org.qtjambi.maven.plugins.utils.IEnvironmentResolver;
import org.qtjambi.maven.plugins.utils.Platform;
import org.qtjambi.maven.plugins.utils.envvar.EnvironmentEditor;
import org.qtjambi.maven.plugins.utils.envvar.EnvironmentPathEditor;
import org.qtjambi.maven.plugins.utils.envvar.OpSet;

public class GccEnvironmentResolver extends DefaultEnvironmentResolver implements IEnvironmentResolver {
	public static final String K_make = "make";

	private String home;
	private String crossCompilePrefix;
	private Map<String,String> commandMap;
	private String commandMake;

	private EnvironmentPathEditor pathEditor;
	private EnvironmentEditor envvarEditor;

	public GccEnvironmentResolver(Platform platform) {
		super(platform);
		commandMap = new HashMap<String,String>();
		commandMake = K_make;
		pathEditor = new EnvironmentPathEditor();
		envvarEditor = new EnvironmentEditor();
	}

	public void autoConfigure() {
		
	}

	public void setCrossCompilePrefix(String crossCompilePrefix) {
		// Maybe we should do some auto-detection of this here
		this.crossCompilePrefix = crossCompilePrefix;
		envvarEditor.remove(K_CROSS_COMPILE);
		if(crossCompilePrefix != null)
			envvarEditor.add(K_CROSS_COMPILE, new OpSet(crossCompilePrefix));
	}

	@Override
	public void applyEnvironmentVariables(Map<String, String> envvar) {
		super.applyEnvironmentVariables(envvar);
		applyEnvironmentVariablesNoParent(envvar);
	}

	@Override
	public void applyEnvironmentVariablesNoParent(Map<String, String> envvar) {
		// Make it non-virtual
		applyEnvironmentVariablesNoParent(envvar, this);
	}

	private void applyEnvironmentVariablesNoParent(Map<String, String> envvar, GccEnvironmentResolver uniqueSignature) {
		envvarEditor.apply(envvar);
		pathEditor.apply(envvar, K_PATH);
	}

	@Override
	public String resolveCommandMake() {
		return commandMake;
	}
}
