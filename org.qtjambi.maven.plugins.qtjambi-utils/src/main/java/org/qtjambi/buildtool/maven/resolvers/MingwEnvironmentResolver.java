package org.qtjambi.buildtool.maven.resolvers;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.qtjambi.buildtool.maven.IEnvironmentResolver;
import org.qtjambi.buildtool.maven.Platform;
import org.qtjambi.buildtool.maven.utils.Utils;

public class MingwEnvironmentResolver extends DefaultEnvironmentResolver implements IEnvironmentResolver {
	public static final String K_mingw32_make = "mingw32-make";

	private String home;
	private String crossCompilePrefix;
	private Map<String,String> commandMap;
	private String commandMake;

	private List<String> pathAppend;
	private List<String> ldLibraryPathAppend;
	private List<String> dyldLibraryPathAppend;
	private Map<String,String> envvarMap;

	public MingwEnvironmentResolver(Platform platform) {
		super(platform);
		commandMap = new HashMap<String,String>();
		commandMake = K_mingw32_make;
	}

	public void setHome(String home, boolean autoConfigure) {
		this.home = home;

		// AUTO
		//  pathAppend += ${home}/bin
		if(autoConfigure) {
			File dirHome = new File(home);
			if(dirHome.exists() && dirHome.isDirectory()) {
				File dirHomeBin = new File(home, "bin");
				if(dirHomeBin.exists() && dirHomeBin.isDirectory()) {
					pathAppend = Utils.safeListStringAppend(pathAppend, "<" + dirHomeBin.getAbsolutePath());	// prepend
				}
			}
		}
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

	public String resolveCommand(File dir, String command) {
		if(commandMap.containsKey(command))
			return commandMap.get(command);
		if(command.indexOf(File.separator) >= 0) {	// Windows can't execute relative paths must convert to absolute
			if(dir != null && command.startsWith(File.separator) == false)
				command = dir.getAbsolutePath() + File.separator + platform.makeExeFilename(command);
			return command;
		}

		String commandPath = command;

		// Resolve ???
		String fileString = platform.makeExeFilename(command);
		if(home != null) {
			File dirHome = new File(home);
			if(dirHome.isDirectory()) {
				String relPath = Utils.resolveFileSeparator(new String[] { "bin", fileString });
				File file = new File(dirHome, relPath);
				if(file.exists() && file.isFile() && file.canExecute()) {
					commandPath = file.getAbsolutePath();
					commandMap.put(command, commandPath);
				}
			}
		}
		return commandPath;
	}

	public String resolveCommandMake() {
		return commandMake;
	}
}
