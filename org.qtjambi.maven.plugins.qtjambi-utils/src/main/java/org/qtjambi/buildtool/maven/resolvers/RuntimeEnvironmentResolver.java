package org.qtjambi.buildtool.maven.resolvers;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.qtjambi.buildtool.maven.IEnvironmentResolver;
import org.qtjambi.buildtool.maven.Platform;
import org.qtjambi.buildtool.maven.utils.Utils;

/**
 * This environment is about providing the right things to run outputted Qt or Java+Qt applications.
 * @author Darryl
 *
 */
public class RuntimeEnvironmentResolver extends DefaultEnvironmentResolver implements IEnvironmentResolver {
	private Map<String,String> commandMap;

	private List<String> pathAppend;
	private List<String> ldLibraryPathAppend;
	private List<String> dyldLibraryPathAppend;
	private Map<String,String> envvarMap;

	public RuntimeEnvironmentResolver(Platform platform) {
		super(platform);
		commandMap = new HashMap<String,String>();
	}

	public void setHome(String home, boolean autoConfigure) {

		// AUTO
		//  pathAppend += ${home}/bin
		//  pathAppend += ${home}/lib (windows)
		//  ldLibraryPathAppend += ${home}/lib (linux)
		//  dyldLibraryPathAppend += ${home}/lib (macosx)
		if(autoConfigure) {
			File dirHome = new File(home);
			if(dirHome.exists() && dirHome.isDirectory()) {
				File dirHomeLib = new File(home, "lib");
				if(dirHomeLib.exists() && dirHomeLib.isDirectory()) {
					if(platform.isWindows(false))
						pathAppend = Utils.safeListStringAppend(pathAppend, "<" + dirHomeLib.getAbsolutePath());	// prepend
					if(platform.isLinux(false))
						ldLibraryPathAppend = Utils.safeListStringAppend(ldLibraryPathAppend, "<" + dirHomeLib.getAbsolutePath());	// prepend
					if(platform.isMacosx(false))
						dyldLibraryPathAppend = Utils.safeListStringAppend(dyldLibraryPathAppend, "<" + dirHomeLib.getAbsolutePath());	// prepend
				}

				File dirHomeBin = new File(home, "bin");
				if(dirHomeBin.exists() && dirHomeBin.isDirectory()) {
					pathAppend = Utils.safeListStringAppend(pathAppend, "<" + dirHomeBin.getAbsolutePath());	// prepend
				}
			}
		}
	}

	public void applyEnvironmentVariables(Map<String, String> envvar) {
		super.applyEnvironmentVariables(envvar);

		if(envvarMap != null)
			Utils.applyEnvVarMap(envvar, envvarMap);

		if(pathAppend != null)
			Utils.applyEnvVarPath(envvar, K_PATH, pathAppend);

		if(ldLibraryPathAppend != null)
			Utils.applyEnvVarPath(envvar, K_LD_LIBRARY_PATH, ldLibraryPathAppend);

		if(dyldLibraryPathAppend != null)
			Utils.applyEnvVarPath(envvar, K_DYLD_LIBRARY_PATH, dyldLibraryPathAppend);

		IEnvironmentResolver environmentResolver;

		environmentResolver = platform.getGlobalEnvironmentResolver();
		if(environmentResolver != null)
			environmentResolver.applyEnvironmentVariablesNoParent(envvar);

		// FIXME: Make this toolchain related
		environmentResolver = platform.getMingwEnvironmentResolver();
		if(environmentResolver != null)
			environmentResolver.applyEnvironmentVariablesNoParent(envvar);

		environmentResolver = platform.getQtEnvironmentResolver();
		if(environmentResolver != null)
			environmentResolver.applyEnvironmentVariablesNoParent(envvar);

		environmentResolver = platform.getJavaEnvironmentResolver();
		if(environmentResolver != null)
			environmentResolver.applyEnvironmentVariablesNoParent(envvar);
	}

	public String resolveCommand(File dir, String command) {
		if(commandMap.containsKey(command))
			return commandMap.get(command);
		if(command.indexOf(File.separator) >= 0) {	// Windows can't execute relative paths must convert to absolute
			if(dir != null && command.startsWith(File.separator) == false)
				command = dir.getAbsolutePath() + File.separator + platform.makeExeFilename(command);
			return command;
		}

		// Resolve ???
		String commandPath = platform.makeExeFilename(command);
		return commandPath;
	}
}
