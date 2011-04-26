package org.qtjambi.maven.plugins.utils.resolvers;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.qtjambi.maven.plugins.utils.IEnvironmentResolver;
import org.qtjambi.maven.plugins.utils.Platform;
import org.qtjambi.maven.plugins.utils.shared.Utils;

public class DefaultEnvironmentResolver implements IEnvironmentResolver {
	public static final String K_make = "make";

	public static final String K_PATH		 		= "PATH";
	public static final String K_LD_LIBRARY_PATH	= "LD_LIBRARY_PATH";
	public static final String K_DYLD_LIBRARY_PATH	= "DYLD_LIBRARY_PATH";

	private String commandMake;

	private List<String> pathAppend;
	private List<String> ldLibraryPathAppend;
	private List<String> dyldLibraryPathAppend;
	private Map<String,String> envvarMap;

	protected Platform platform;

	public DefaultEnvironmentResolver(Platform platform) {
		this.platform = platform;
		commandMake = K_make;
	}

	public void applyEnvironmentVariables(Map<String, String> envvar) {
		//super.applyEnvironmentVariables(envvar);
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

	public String resolveCommand(File dir, String command) {
		if(command.indexOf(File.separator) >= 0 && dir != null && command.startsWith(File.separator) == false)
			command = dir.getAbsolutePath() + File.separator + command;

		return platform.makeExeFilename(command);
	}

	public String resolveCommandMake() {
		return commandMake;
	}

	public List<String> getPathAppend() {
		return pathAppend;
	}
	public void setPathAppend(List<String> pathAppend) {
		this.pathAppend = pathAppend;
	}

	public List<String> getLdLibraryPathAppend() {
		return ldLibraryPathAppend;
	}
	public void setLdLibraryPathAppend(List<String> ldLibraryPathAppend) {
		this.ldLibraryPathAppend = ldLibraryPathAppend;
	}

	public List<String> getDyldLibraryPathAppend() {
		return dyldLibraryPathAppend;
	}
	public void setDyldLibraryPathAppend(List<String> dyldLibraryPathAppend) {
		this.dyldLibraryPathAppend = dyldLibraryPathAppend;
	}

	public Map<String, String> getEnvvarMap() {
		return envvarMap;
	}
	public void setEnvvarMap(Map<String, String> envvarMap) {
		this.envvarMap = envvarMap;
	}
}
