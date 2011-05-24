package org.qtjambi.maven.plugins.utils.resolvers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.qtjambi.maven.plugins.utils.IEnvironmentResolver;
import org.qtjambi.maven.plugins.utils.Platform;
import org.qtjambi.maven.plugins.utils.envvar.EnvironmentEditor;
import org.qtjambi.maven.plugins.utils.envvar.EnvironmentPathEditor;
import org.qtjambi.maven.plugins.utils.envvar.OpPathAppend;
import org.qtjambi.maven.plugins.utils.envvar.OpPathPrepend;
import org.qtjambi.maven.plugins.utils.envvar.OpSet;
import org.qtjambi.maven.plugins.utils.shared.Utils;

public class MingwEnvironmentResolver extends DefaultEnvironmentResolver implements IEnvironmentResolver {
	public static final String K_mingw32_make = "mingw32-make";

	private String home;
	private String crossCompilePrefix;
	private Map<String,String> commandMap;
	private String commandMake;

	private EnvironmentPathEditor pathEditor;
	private EnvironmentEditor envvarEditor;

	public MingwEnvironmentResolver(Platform platform) {
		super(platform);
		commandMap = new HashMap<String,String>();
		commandMake = K_mingw32_make;
		pathEditor = new EnvironmentPathEditor();
		envvarEditor = new EnvironmentEditor();
	}

	public void setHome(String home, boolean autoConfigure) {
		this.home = home;

		// AUTO
		//  pathAppend += ${home}/bin
		if(autoConfigure) {
			// We need to resolve the make command
			// FIXME: Check for it to be explicitly set mingw.make.path=C:\MinGW\bin\mingw32-make
			// See mingw64 handling

			File dirHome = new File(home);
			if(dirHome.exists() && dirHome.isDirectory()) {
				File dirHomeBin = new File(home, "bin");
				if(dirHomeBin.exists() && dirHomeBin.isDirectory()) {
					pathEditor.add(new OpPathPrepend(dirHomeBin.getAbsolutePath()));
				}
			}
		}
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
		envvarEditor.apply(envvar);
		pathEditor.apply(envvar, K_PATH);
	}

	@Override
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
				if(file.exists() && file.isFile() && Utils.invokeFileCanExecuteDefault(file, true)) {
					commandPath = file.getAbsolutePath();
					commandMap.put(command, commandPath);
				}
			}
		}
		return commandPath;
	}

	@Override
	public String resolveCommandMake() {
		return commandMake;
	}
}
