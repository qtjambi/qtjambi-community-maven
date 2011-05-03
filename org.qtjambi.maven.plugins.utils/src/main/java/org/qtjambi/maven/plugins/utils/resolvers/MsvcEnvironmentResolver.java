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
				File dirHomeBin = new File(dirHomeVcInstallDir, "bin");
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
					File dirLib = new File(dirHomeVcInstallDir, "lib");
					if(dirLib.exists() && dirLib.isDirectory())
						envvarEditor.add(K_LIB, new OpPathAppend(dirLib.getAbsolutePath()));
				}

				{
					// LIBPATH=%VCINSTALLDIR%\lib
					File dirLibpath = new File(dirHomeVcInstallDir, "lib");
					if(dirLibpath.exists() && dirLibpath.isDirectory())
						envvarEditor.add(K_LIBPATH, new OpPathAppend(dirLibpath.getAbsolutePath()));
				}

			}

			File microsoftSdks = new File(programFilesX86, "Microsoft SDKs\\Windows\\v7.0A");
			// MSVC2008 v7.0
			// MSVC2010 v7.0A
			if(microsoftSdks.exists() && microsoftSdks.isDirectory()) {
				envvarEditor.add(K_WindowsSdkDir, new OpSet(microsoftSdks.getAbsolutePath()));

				// PATH=%WindowsSdkDir%\bin
				File sdkBin = new File(microsoftSdks, "bin");
				if(sdkBin.exists() && sdkBin.isDirectory())
					pathEditor.add(new OpPathAppend(sdkBin.getAbsolutePath()));

				// INCLUDE=%WindowsSdkDir%\include
				File sdkInclude = new File(microsoftSdks, "include");
				if(sdkInclude.exists() && sdkInclude.isDirectory())
					envvarEditor.add(K_INCLUDE, new OpPathAppend(sdkInclude.getAbsolutePath()));

				// LIB=%WindowsSdkDir%\lib
				File sdkLib = new File(microsoftSdks, "lib");
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
