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

public class MsvcEnvironmentResolver extends DefaultEnvironmentResolver implements IEnvironmentResolver {
	public static final String K_nmake = "nmake";

	public static final String K_INCLUDE		= "INCLUDE";
	public static final String K_LIBPATH		= "LIBPATH";
	public static final String K_LIB			= "LIB";
	public static final String K_WindowsSdkDir	= "WindowsSdkDir";

	private String home;
	private Map<String,String> commandMap;
	private String commandMake;

	private EnvironmentPathEditor pathEditor;
	private EnvironmentEditor envvarEditor;

	public MsvcEnvironmentResolver(Platform platform) {
		super(platform);
		commandMap = new HashMap<String,String>();
		commandMake = K_nmake;
		pathEditor = new EnvironmentPathEditor();
		envvarEditor = new EnvironmentEditor();
	}

	public void setHome(String home, boolean x64, boolean autoConfigure) {
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
			//   C:\Program Files (x86)\Microsoft Visual Studio 9.0\VC\ATLMFC\LIB  (+ \amd64)
			//  ;C:\Program Files (x86)\Microsoft Visual Studio 9.0\VC\LIB (+ \amd64)
			//  ;C:\Program Files\Microsoft SDKs\Windows\v7.0\lib (+ \x64)
			//  ;
			// LIBPATH=
			//   C:\Windows\Microsoft.NET\Framework\
			//  ;C:\Windows\Microsoft.NET\Framework64\v3.5 (+ line only for x64)
			//  ;C:\Windows\Microsoft.NET\Framework64\v2.0.50727;
			//  ;C:\Program Files (x86)\Microsoft Visual Studio 9.0\VC\ATLMFC\LIB (+ \amd64)
			//  ;C:\Program Files (x86)\Microsoft Visual Studio 9.0\VC\LIB (+ \amd64)
			//  ;
			// DevEnvDir=C:\Program Files (x86)\Microsoft Visual Studio 9.0\Common7\IDE
			// VCINSTALLDIR=C:\Program Files (x86)\Microsoft Visual Studio 9.0\VC
			// VS100COMNTOOLS=C:\Program Files (x86)\Microsoft Visual Studio 10.0\Common7\Tools\
			// VS90COMNTOOLS=C:\Program Files (x86)\Microsoft Visual Studio 9.0\Common7\Tools\
			// VSINSTALLDIR=C:\Program Files (x86)\Microsoft Visual Studio 9.0
			// PATH=C:\Program Files (x86)\Microsoft Visual Studio 9.0\Common7\IDE
			//     ;C:\Program Files (x86)\Microsoft Visual Studio 9.0\VC\BIN (+ \amd64)
			//     ;C:\Program Files (x86)\Microsoft Visual Studio 9.0\Common7\Tools
			//     ;C:\Program Files (x86)\Microsoft Visual Studio 9.0\Common7\Tools\bin
			//     ;C:\Windows\Microsoft.NET\Framework\
			//     ;C:\Windows\Microsoft.NET\Framework\\Microsoft .NET Framework 3.5 (Pre-Release Version)
			//     ;C:\Windows\Microsoft.NET\Framework64\v3.5	(+ line only for x64)
			//     ;C:\Windows\Microsoft.NET\Framework\v2.0.50727
			//     ;C:\Program Files (x86)\Microsoft Visual Studio 9.0\VC\VCPackages
			//     ;C:\Program Files\Microsoft SDKs\Windows\v7.0\bin  (+ \x64)
			//     ;C:\Windows\system32
			//     ;C:\Windows
			//     ;C:\Windows\System32\Wbem
			//     ;C:\Windows\System32\WindowsPowerShell\v1.0\
			//     ;C:\Program Files (x86)\Microsoft Visual Studio 9.0\Common7\IDE\PrivateAssemblies\

			// Auto-detect MSVC version (from nothing)
			File systemDrive = new File("C:\\");	// %SystemDrive% ?
			File programFilesKind;
			File programFilesX86 = new File(systemDrive, "Program Files (x86)");
			File programFiles = new File(systemDrive, "Program Files");
			if(x64)
				programFilesKind = programFiles;
			else
				programFilesKind = programFilesX86;

			// Auto-detect MSVC version from msvc.home
			File systemRoot = new File(systemDrive, "WINDOWS");	// %windir% %SystemRoot% ?
			File windowsMicrosoftFramework = null;
			if(x64)
				windowsMicrosoftFramework = new File(systemRoot, "Microsoft.NET\\Framework64");
			else
				windowsMicrosoftFramework = new File(systemRoot, "Microsoft.NET\\Framework");
			File windowsMicrosoftFrameworkV2 = new File(windowsMicrosoftFramework, "v2.0.50727");
			File windowsMicrosoftFrameworkV35 = new File(windowsMicrosoftFramework, "v3.5");

			File dirHome = new File(home);
			if(dirHome.exists() && dirHome.isDirectory()) {
				// VSINSTALLDIR=%MSVC_HOME%
				File dirHomeVsInstallDir = dirHome;
				if(dirHomeVsInstallDir.exists() && dirHomeVsInstallDir.isDirectory())
					envvarEditor.add("VSINSTALLDIR", new OpSet(dirHomeVsInstallDir.getAbsolutePath()));

				// VCINSTALLDIR=%MSVC_HOME%\VC
				File dirHomeVcInstallDir = new File(dirHome, "VC");
				if(dirHomeVcInstallDir.exists() && dirHomeVcInstallDir.isDirectory())
					envvarEditor.add("VCINSTALLDIR", new OpSet(dirHomeVcInstallDir.getAbsolutePath()));

				// DevEnvDir=%VSINSTALLDIR%\Common7\IDE
				File dirHomeDevEnvDir = new File(dirHomeVsInstallDir, "Common7\\IDE");
				if(dirHomeDevEnvDir.exists() && dirHomeDevEnvDir.isDirectory())
					envvarEditor.add("DevEnvDir", new OpSet(dirHomeDevEnvDir.getAbsolutePath()));

				// PATH=%VCINSTALLDIR%\bin;%VSINSTALLDIR%\Common7\Tools;%VSINSTALLDIR%\Common7\IDE;%VCINSTALLDIR%\VCPackages
				File dirHomeBin;
				if(x64)
					dirHomeBin = new File(dirHomeVcInstallDir, "bin\\amd64");
				else
					dirHomeBin = new File(dirHomeVcInstallDir, "bin");
				if(dirHomeBin.exists() && dirHomeBin.isDirectory())
					pathEditor.add(new OpPathAppend(dirHomeBin.getAbsolutePath()));
				File dirHomeCommon7Tools = new File(dirHomeVsInstallDir, "Common7\\Tools");
				if(dirHomeCommon7Tools.exists() && dirHomeCommon7Tools.isDirectory())
					pathEditor.add(new OpPathAppend(dirHomeCommon7Tools.getAbsolutePath()));
				File dirHomeCommon7Ide = new File(dirHomeVsInstallDir, "Common7\\IDE");
				if(dirHomeCommon7Ide.exists() && dirHomeCommon7Ide.isDirectory())
					pathEditor.add(new OpPathAppend(dirHomeCommon7Ide.getAbsolutePath()));
				File dirHomeVcPackages = new File(dirHomeVcInstallDir, "VCPackages");
				if(dirHomeVcPackages.exists() && dirHomeVcPackages.isDirectory())
					pathEditor.add(new OpPathAppend(dirHomeVcPackages.getAbsolutePath()));

				{
					// INCLUDE=%VCINSTALLDIR%\include
					File dirInclude = new File(dirHomeVcInstallDir, "include");
					if(dirInclude.exists() && dirInclude.isDirectory())
						envvarEditor.add(K_INCLUDE, new OpPathAppend(dirInclude.getAbsolutePath()));
				}

				{
					// LIB=%VCINSTALLDIR%\lib
					File dirLib;
					if(x64)
						dirLib = new File(dirHomeVcInstallDir, "lib\\amd64");
					else
						dirLib = new File(dirHomeVcInstallDir, "lib");
					if(dirLib.exists() && dirLib.isDirectory())
						envvarEditor.add(K_LIB, new OpPathAppend(dirLib.getAbsolutePath()));
				}

				{
					// LIBPATH=%VCINSTALLDIR%\lib
					File dirLibpath;
					if(x64)
						dirLibpath = new File(dirHomeVcInstallDir, "lib\\amd64");
					else
						dirLibpath = new File(dirHomeVcInstallDir, "lib");
					if(dirLibpath.exists() && dirLibpath.isDirectory())
						envvarEditor.add(K_LIBPATH, new OpPathAppend(dirLibpath.getAbsolutePath()));
				}

			}

			File microsoftSdks;
			if(x64)
				microsoftSdks = new File(programFilesKind, "Microsoft SDKs\\Windows\\v7.1");
			else
				microsoftSdks = new File(programFilesKind, "Microsoft SDKs\\Windows\\v7.0A");
			// MSVC2008 v7.0 (x86 & x64)
			// MSVC2010 v7.0A (x86 only)
			// MSVC2010 v7.1 (x64 only)
			if(microsoftSdks.exists() && microsoftSdks.isDirectory()) {
				envvarEditor.add(K_WindowsSdkDir, new OpSet(microsoftSdks.getAbsolutePath()));

				// PATH=%WindowsSdkDir%\bin
				File sdkBin;
				if(x64)
					sdkBin = new File(microsoftSdks, "bin\\x64");
				else
					sdkBin = new File(microsoftSdks, "bin");
				if(sdkBin.exists() && sdkBin.isDirectory())
					pathEditor.add(new OpPathAppend(sdkBin.getAbsolutePath()));

				// INCLUDE=%WindowsSdkDir%\include
				File sdkInclude = new File(microsoftSdks, "include");
				if(sdkInclude.exists() && sdkInclude.isDirectory())
					envvarEditor.add(K_INCLUDE, new OpPathAppend(sdkInclude.getAbsolutePath()));

				// LIB=%WindowsSdkDir%\lib
				File sdkLib;
				if(x64)
					sdkLib = new File(microsoftSdks, "lib\\x64");
				else
					sdkLib = new File(microsoftSdks, "lib");
				if(sdkLib.exists() && sdkLib.isDirectory())
					envvarEditor.add(K_LIB, new OpPathAppend(sdkLib.getAbsolutePath()));
			}

			// envvarMap
			//  unset QTDIR
			//  unset QTINC
			//  unset QTLIB
			//  unset QT_IM_MODULE
			//  unset QT_PLUGIN_PATH
		}
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
