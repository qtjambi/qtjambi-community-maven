package org.qtjambi.maven.plugins.utils.resolvers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.qtjambi.maven.plugins.utils.IEnvironmentResolver;
import org.qtjambi.maven.plugins.utils.Platform;
import org.qtjambi.maven.plugins.utils.shared.Utils;

public class MsvcEnvironmentResolver extends DefaultEnvironmentResolver implements IEnvironmentResolver {
	public static final String K_nmake = "nmake";

	private String home;
	private Map<String,String> commandMap;
	private String commandMake;

	private List<String> pathAppend;
	private List<String> ldLibraryPathAppend;
	private List<String> dyldLibraryPathAppend;
	private Map<String,String> envvarMap;

	public MsvcEnvironmentResolver(Platform platform) {
		super(platform);
		commandMap = new HashMap<String,String>();
		commandMake = K_nmake;
	}

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

	public String resolveCommandMake() {
		return commandMake;
	}
}
