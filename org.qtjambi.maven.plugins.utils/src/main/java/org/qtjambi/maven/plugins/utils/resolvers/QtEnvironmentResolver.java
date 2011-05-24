package org.qtjambi.maven.plugins.utils.resolvers;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.qtjambi.maven.plugins.utils.IEnvironmentResolver;
import org.qtjambi.maven.plugins.utils.Platform;
import org.qtjambi.maven.plugins.utils.envvar.EnvironmentEditor;
import org.qtjambi.maven.plugins.utils.envvar.EnvironmentPathEditor;
import org.qtjambi.maven.plugins.utils.envvar.OpPathPrepend;
import org.qtjambi.maven.plugins.utils.shared.Utils;

public class QtEnvironmentResolver extends DefaultEnvironmentResolver implements IEnvironmentResolver {
	public static final String K_qmake = "qmake";

	public static final String K_QMAKESPECS			= "QMAKESPECS";

	private String home;
	private Map<String,String> commandMap;
	private String qmakespecs;
	private String commandMake;

	private EnvironmentPathEditor pathEditor;
	private EnvironmentEditor envvarEditor;

	public QtEnvironmentResolver(Platform platform) {
		super(platform);
		commandMap = new HashMap<String,String>();
		commandMake = K_qmake;
		pathEditor = new EnvironmentPathEditor();
		envvarEditor = new EnvironmentEditor();
	}

	public void setHome(String home, boolean autoConfigure) {
		this.home = home;

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
						pathEditor.add(new OpPathPrepend(dirHomeLib.getAbsolutePath()));
					if(platform.isLinux(false))
						envvarEditor.add(K_LD_LIBRARY_PATH, new OpPathPrepend(dirHomeLib.getAbsolutePath()));
					if(platform.isMacosx(false))
						envvarEditor.add(K_DYLD_LIBRARY_PATH, new OpPathPrepend(dirHomeLib.getAbsolutePath()));
				}

				File dirHomeBin = new File(home, "bin");
				if(dirHomeBin.exists() && dirHomeBin.isDirectory()) {
					pathEditor.add(new OpPathPrepend(dirHomeBin.getAbsolutePath()));
				}
			}
			// envvarMap
			//  unset QTDIR
			//  unset QTINC
			//  unset QTLIB
			//  unset QT_IM_MODULE
			//  unset QT_PLUGIN_PATH
		}
	}

	public void setQmakespecs(String qmakespecs) {
		this.qmakespecs = qmakespecs;
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

	private void applyEnvironmentVariablesNoParent(Map<String, String> envvar, QtEnvironmentResolver uniqueSignature) {
		envvarEditor.apply(envvar);
		pathEditor.apply(envvar, K_PATH);

		if(qmakespecs != null)
			envvar.put(K_QMAKESPECS, qmakespecs);
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
