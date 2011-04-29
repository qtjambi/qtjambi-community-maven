package org.qtjambi.maven.plugins.utils.resolvers;

import java.io.File;
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

	public void setHome(String home, boolean autoConfigure) {
		this.home = home;

		// AUTO
		//  pathAppend += ${home}/bin
		//  pathAppend += ${home}/lib (windows)
		if(autoConfigure) {
			// INCLUDE=
			//	 C:\Program Files (x86)\Microsoft Visual Studio 9.0\VC\ATLMFC\INCLUDE
			//	;C:\Program Files (x86)\Microsoft Visual Studio 9.0\VC\INCLUDE
			//  ;C:\Program Files\Microsoft SDKs\Windows\v7.0\include
			//  ;
			// LIB=
			//   C:\Program Files (x86)\Microsoft Visual Studio 9.0\VC\ATLMFC\LIB
			//  ;C:\Program Files (x86)\Microsoft Visual Studio 9.0\VC\LIB
			//  ;C:\Program Files\Microsoft SDKs\Windows\v7.0\lib
			//  ;
			// LIBPATH=
			//   C:\Windows\Microsoft.NET\Framework\
			//  ;C:\Windows\Microsoft.NET\Framework\v2.0.50727;
			//  ;C:\Program Files (x86)\Microsoft Visual Studio 9.0\VC\ATLMFC\LIB
			//  ;C:\Program Files (x86)\Microsoft Visual Studio 9.0\VC\LIB
			//  ;
			// DevEnvDir=C:\Program Files (x86)\Microsoft Visual Studio 9.0\Common7\IDE
			// VCINSTALLDIR=C:\Program Files (x86)\Microsoft Visual Studio 9.0\VC
			// VS100COMNTOOLS=C:\Program Files (x86)\Microsoft Visual Studio 10.0\Common7\Tools\
			// VS90COMNTOOLS=C:\Program Files (x86)\Microsoft Visual Studio 9.0\Common7\Tools\
			// VSINSTALLDIR=C:\Program Files (x86)\Microsoft Visual Studio 9.0
			// PATH=C:\Program Files (x86)\Microsoft Visual Studio 9.0\Common7\IDE
			//     ;C:\Program Files (x86)\Microsoft Visual Studio 9.0\VC\BIN
			//     ;C:\Program Files (x86)\Microsoft Visual Studio 9.0\Common7\Tools
			//     ;C:\Program Files (x86)\Microsoft Visual Studio 9.0\Common7\Tools\bin
			//     ;C:\Windows\Microsoft.NET\Framework\
			//     ;C:\Windows\Microsoft.NET\Framework\\Microsoft .NET Framework 3.5 (Pre-Release Version)
			//     ;C:\Windows\Microsoft.NET\Framework\v2.0.50727
			//     ;C:\Program Files (x86)\Microsoft Visual Studio 9.0\VC\VCPackages
			//     ;C:\Program Files\Microsoft SDKs\Windows\v7.0\bin
			//     ;C:\Windows\system32
			//     ;C:\Windows
			//     ;C:\Windows\System32\Wbem
			//     ;C:\Windows\System32\WindowsPowerShell\v1.0\
			//     ;C:\Program Files (x86)\Microsoft Visual Studio 9.0\Common7\IDE\PrivateAssemblies\

			// Auto-detect MSVC version (from nothing)
			File systemDrive = new File("C:\\");	// %SystemDrive% ?
			File programFilesX86 = new File(systemDrive, "Program Files (x86)");
			File programFiles = new File(systemDrive, "Program Files");

			// Auto-detect MSVC version from msvc.home
			File systemRoot = new File(systemDrive, "WINDOWS");	// %windir% %SystemRoot% ?
			File windowsMicrosoftFramework = new File(systemRoot, "Microsoft.NET\\Framework");
			File windowsMicrosoftFrameworkV2 = new File(windowsMicrosoftFramework, "v2.0.50727");

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

				File dirHomeBin = new File(home, "VC\\bin");
				if(dirHomeBin.exists() && dirHomeBin.isDirectory()) {
					pathAppend = Utils.safeListStringAppend(pathAppend, "<" + dirHomeBin.getAbsolutePath());	// prepend
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
