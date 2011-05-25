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

public class MingwW64EnvironmentResolver extends DefaultEnvironmentResolver implements IEnvironmentResolver {
	public static final String K_mingw32_make = "mingw32-make";

	private String home;
	private String crossCompilePrefix;
	private Map<String,String> commandMap;
	private String commandMake;

	private EnvironmentPathEditor pathEditor;
	private EnvironmentEditor envvarEditor;

	public MingwW64EnvironmentResolver(Platform platform) {
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
			boolean foundMingw32Make = false;
			// FIXME: Check for it to be explicitly set mingw.make.path=C:\MinGW\bin\mingw32-make
			// Now see if user setup mingw.home and check that
			if(foundMingw32Make == false) {
				
			}
			if(foundMingw32Make == false) {
				String mingwHome = System.getProperty("mingw.home");
				if(mingwHome != null) {
					File dirMingwHome = new File(mingwHome);
					if(dirMingwHome.exists() && dirMingwHome.isDirectory()) {
						// This is a recommendation I make, to setup for MinGW-W64
						//  copy the file: mingw32-make.exe libintl*.dll libiconv*.dll
						// into this directory, so no chance of accidentally picking up gcc.exe 
						File dirMingwHomeMybin = new File(dirMingwHome, "mybin");
						if(dirMingwHomeMybin.exists() && dirMingwHomeMybin.isDirectory()) {
							File exe = new File(dirMingwHomeMybin, K_mingw32_make);
							if(exe.exists() && exe.isFile() && Utils.invokeFileCanExecuteDefault(exe, true)) {
								// We don't add it to the $PATH because we might accidentally use the gcc.exe in there too
								commandMake = exe.getAbsolutePath();	// don't add .exe
								pathEditor.add(new OpPathAppend(dirMingwHomeMybin.getAbsolutePath()));
								foundMingw32Make = true;
							} else {
								File exe2 = new File(dirMingwHomeMybin, K_mingw32_make + ".exe");
								if(exe2.exists() && exe2.isFile() && Utils.invokeFileCanExecuteDefault(exe2, true)) {
									commandMake = exe.getAbsolutePath();
									pathEditor.add(new OpPathAppend(dirMingwHomeMybin.getAbsolutePath()));
									foundMingw32Make = true;
								}
								// FIXME: Emit a log entry
							}
						}
						if(foundMingw32Make == false) {
							File dirMingwHomeBin = new File(dirMingwHome, "bin");
							if(dirMingwHomeBin.exists() && dirMingwHomeBin.isDirectory()) {
								File exe = new File(dirMingwHomeBin, K_mingw32_make);
								if(exe.exists() && exe.isFile() && Utils.invokeFileCanExecuteDefault(exe, true)) {
									// We don't add it to the $PATH because we might accidentally use the gcc.exe in there too
									commandMake = exe.getAbsolutePath();
									pathEditor.add(new OpPathAppend(dirMingwHomeBin.getAbsolutePath()));
									foundMingw32Make = true;
								} else {
									File exe2 = new File(dirMingwHomeBin, K_mingw32_make + ".exe");
									if(exe2.exists() && exe2.isFile() && Utils.invokeFileCanExecuteDefault(exe2, true)) {
										commandMake = exe.getAbsolutePath();	// don't add .exe
										pathEditor.add(new OpPathAppend(dirMingwHomeBin.getAbsolutePath()));
										foundMingw32Make = true;
									}
									// FIXME: Emit a log entry
								}
							}
						}
					}
				}
			}

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
	public String resolveCommandMake() {
		return commandMake;
	}
}
