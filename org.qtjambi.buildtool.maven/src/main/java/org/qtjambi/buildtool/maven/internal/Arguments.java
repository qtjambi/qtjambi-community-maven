package org.qtjambi.buildtool.maven.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.plugin.logging.Log;
import org.qtjambi.buildtool.maven.Platform;
import org.qtjambi.buildtool.maven.resolvers.DefaultEnvironmentResolver;
import org.qtjambi.buildtool.maven.resolvers.MingwEnvironmentResolver;
import org.qtjambi.buildtool.maven.resolvers.QtEnvironmentResolver;
import org.qtjambi.buildtool.maven.utils.Utils;

// TODO: Dump available platforms, disambiguate
public class Arguments {
	public static final String K_toolchain			= "toolchain";			// gcc, mingw, mingw_w64/mingw-w64, msvc/msvc2010
	public static final String K_cross_compile		= "cross_compile";
	public static final String K_qtsdk_home			= "qtsdk.home";
	// FIXME: -platform $QMAKESPECS override for building (not implemented)
	public static final String K_qt_platform		= "qt.platform";
	public static final String K_qt_makespecs		= "qt.makespecs";
	public static final String K_msvc_home			= "msvc.home";
	public static final String K_mingw_home			= "mingw.home";
	public static final String K_mingw_w64_home 	= "mingw_w64.home";		// preferred
	public static final String K_mingw__w64_home	= "mingw-w64.home";
	public static final String K_java_home			= "java.home";
	public static final String K_jre_home			= "jre.home";
	public static final String K_jdk_home			= "jdk.home";
	// Easy way to modify path
	public static final String K_path_append				= "path.append";
	// Easy way to modify LD_LIBRARY_PATH
	public static final String K_ld_library_path_append		= "ld_library_path.append";			// linux
	public static final String K_dyld_library_path_append	= "dyld_library_path.append";		// macosx
	public static final String K_envvar_global				= "envvar.global";
	public static final String K_envvar_qt					= "envvar.qt";
	public static final String K_envvar_gcc					= "envvar.gcc";
	public static final String K_envvar_msvc				= "envvar.msvc";
	public static final String K_envvar_mingw				= "envvar.mingw";
	public static final String K_envvar_mingw_w64			= "envvar.mingw_w64";
	public static final String K_envvar_java				= "envvar.java";


	public static final String K_CROSS_COMPILE		= "CROSS_COMPILE";
	public static final String K_QMAKESPECS			= "QMAKESPECS";
	public static final String K_QTSDK_HOME			= "QTSDK_HOME";
	public static final String K_MSVC_HOME			= "MSVC_HOME";
	public static final String K_MINGW_HOME			= "MINGW_HOME";
	public static final String K_MINGW_W64_HOME		= "MINGW_W64_HOME";		// prefered
	public static final String K_MINGW__W64_HOME	= "MINGW-W64_HOME";
	public static final String K_JAVA_HOME			= "JAVA_HOME";
	public static final String K_JRE_HOME			= "JRE_HOME";
	public static final String K_JDK_HOME			= "JDK_HOME";

	private static final String[] crossCompileTryList = {
		"x86_64-w64-mingw32-"
	};

	private String crossCompilePrefix;
	private String qtPlatform;
	private String qtMakespecs;
	private List<String> pathAppend;
	private List<String> ldLibraryPathAppend;
	private List<String> dyldLibraryPathAppend;
	private Map<String,String> envvarGlobal;
	private Map<String,String> envvarQt;		// :+-= prefix (append pathSep, append verbatim, unset, set verbatim)
	private Map<String,String> envvarGcc;
	private Map<String,String> envvarMsvc;
	private Map<String,String> envvarMingw;
	private Map<String,String> envvarMingwW64;
	private Map<String,String> envvarJava;

	private String qtsdkHome;
	private String msvcHome;
	private String mingwHome;
	private String mingwW64Home;
	private boolean javaHomeSet;
	private String jreHome;
	private String jdkHome;

	public Arguments() {
		pathAppend = new ArrayList<String>();
		ldLibraryPathAppend = new ArrayList<String>();
		dyldLibraryPathAppend = new ArrayList<String>();

		envvarGlobal = new HashMap<String, String>();
		envvarQt = new HashMap<String, String>();
		envvarMsvc = new HashMap<String, String>();
		envvarGcc = new HashMap<String, String>();
		envvarMingw = new HashMap<String, String>();
		envvarMingwW64 = new HashMap<String, String>();
		envvarJava = new HashMap<String, String>();
	}

	public void setup(Platform platform, Log log) {
		String s;

		if(qtPlatform == null) {
			s = System.getProperty(K_qt_platform);
			if(s != null) {
				if(log != null)
					log.debug(K_qt_platform + ": is set \"" + s + "\"");
				qtPlatform = s;
			}
		}

		if(qtMakespecs == null) {
			s = System.getProperty(K_qt_makespecs);
			if(s != null) {
				if(log != null)
					log.debug(K_qt_makespecs + ": is set \"" + s + "\"");
				qtMakespecs = s;
			}
		}
		if(qtMakespecs == null) {
			s = System.getenv(K_QMAKESPECS);
			if(s != null) {
				if(log != null)
					log.debug(K_QMAKESPECS + ": is set \"" + s + "\"");
				qtMakespecs = s;
			}
		}

		Map<String,String> tmpMap;
		tmpMap = filterSystemProperties(K_path_append);
		for(String v : tmpMap.values())
			pathAppend.add(v);					// ignore key

		tmpMap = filterSystemProperties(K_ld_library_path_append);
		for(String v : tmpMap.values())
			ldLibraryPathAppend.add(v);			// ignore key

		tmpMap = filterSystemProperties(K_dyld_library_path_append);
		for(String v : tmpMap.values())
			dyldLibraryPathAppend.add(v);		// ignore key

		// FIXME: If there are no explicit settings we want to auto-apply some

		tmpMap = filterSystemProperties(K_envvar_global);
		for(Map.Entry<String,String> e : tmpMap.entrySet())
			envvarGlobal.put(e.getKey(), e.getValue());

		// envvar.qt.PATH.0=:${qt.home}/bin
		// envvar.qt.PATH.0=:${qt.home}/lib
		tmpMap = filterSystemProperties(K_envvar_qt);
		for(Map.Entry<String,String> e : tmpMap.entrySet())
			envvarQt.put(e.getKey(), e.getValue());

		tmpMap = filterSystemProperties(K_envvar_gcc);
		for(Map.Entry<String,String> e : tmpMap.entrySet())
			envvarGcc.put(e.getKey(), e.getValue());

		// envvar.msvc.PATH.0=:${msvc.home}\VC\bin
		// more...
		tmpMap = filterSystemProperties(K_envvar_msvc);
		for(Map.Entry<String,String> e : tmpMap.entrySet())
			envvarMsvc.put(e.getKey(), e.getValue());

		// envvar.mingw.PATH.0=:${mingw.home}\bin
		tmpMap = filterSystemProperties(K_envvar_mingw);
		for(Map.Entry<String,String> e : tmpMap.entrySet())
			envvarMingw.put(e.getKey(), e.getValue());

		// envvar.mingw_w64.PATH.0=:${mingw_w64.home}\bin
		tmpMap = filterSystemProperties(K_envvar_mingw_w64);
		for(Map.Entry<String,String> e : tmpMap.entrySet())
			envvarMingwW64.put(e.getKey(), e.getValue());

		// envvar.java.PATH.0=:${java.home}\bin
		tmpMap = filterSystemProperties(K_envvar_java);
		for(Map.Entry<String,String> e : tmpMap.entrySet())
			envvarJava.put(e.getKey(), e.getValue());
	}

	public void setupResolvers(Platform platform) {
		DefaultEnvironmentResolver globalEnvironmentResolver = new DefaultEnvironmentResolver(platform);
		{
			globalEnvironmentResolver.setPathAppend(pathAppend);
			globalEnvironmentResolver.setLdLibraryPathAppend(ldLibraryPathAppend);
			globalEnvironmentResolver.setDyldLibraryPathAppend(dyldLibraryPathAppend);
			globalEnvironmentResolver.setEnvvarMap(envvarGlobal);
		}
		platform.setGlobalEnvironmentResolver(globalEnvironmentResolver);

		MingwEnvironmentResolver mingwEnvironmentResolver = new MingwEnvironmentResolver(platform);
		if(mingwEnvironmentResolver instanceof DefaultEnvironmentResolver) {	// Globals
			DefaultEnvironmentResolver defaultEnvironmentResolver = (DefaultEnvironmentResolver) mingwEnvironmentResolver;
			defaultEnvironmentResolver.setPathAppend(pathAppend);
			defaultEnvironmentResolver.setLdLibraryPathAppend(ldLibraryPathAppend);
			defaultEnvironmentResolver.setDyldLibraryPathAppend(dyldLibraryPathAppend);
			defaultEnvironmentResolver.setEnvvarMap(envvarGlobal);
		}
		mingwEnvironmentResolver.setEnvvarMap(envvarMingw);
		if(mingwHome != null)
			mingwEnvironmentResolver.setHome(mingwHome, true);
		platform.setMingwEnvironmentResolver(mingwEnvironmentResolver);

		QtEnvironmentResolver qtEnvironmentResolver = new QtEnvironmentResolver(platform);
		if(qtEnvironmentResolver instanceof DefaultEnvironmentResolver) {		// Globals
			DefaultEnvironmentResolver defaultEnvironmentResolver = (DefaultEnvironmentResolver) qtEnvironmentResolver;
			defaultEnvironmentResolver.setPathAppend(pathAppend);
			defaultEnvironmentResolver.setLdLibraryPathAppend(ldLibraryPathAppend);
			defaultEnvironmentResolver.setDyldLibraryPathAppend(dyldLibraryPathAppend);
			defaultEnvironmentResolver.setEnvvarMap(envvarGlobal);
		}
		qtEnvironmentResolver.setEnvvarMap(envvarQt);
		if(qtsdkHome != null)
			qtEnvironmentResolver.setHome(qtsdkHome, true);
		if(qtMakespecs != null)
			qtEnvironmentResolver.setQmakespecs(qtMakespecs);
		platform.setQtEnvironmentResolver(qtEnvironmentResolver);
	}

	public Map<String,String> filterSystemProperties(String prefix) {
		Properties props = System.getProperties();
		final String prefixDot = prefix + ".";
		Map<String,String> map = new HashMap<String,String>();
		for(Map.Entry<Object, Object> e : props.entrySet()) {
			Object kObj = e.getKey();
			String k = kObj.toString();
			Object vObj = e.getValue();
			String v = vObj.toString();

			if(k.equals(prefix)) {
				map.put(k, v);
			} else if(k.startsWith(prefixDot)) {
				int o = prefixDot.length();
				String sk = k.substring(o);
				int digitCount = Utils.countLeadingDigits(sk);
				if(digitCount > 0) {
					if(sk.length() > digitCount && sk.charAt(digitCount) == '.' && sk.length() > digitCount + 1) {
						// Strip numeric order "prefix.0.abc" => "abc"
						sk = sk.substring(digitCount + 1);
					} else {
						//newKey = sk;		// "prefix.0" => "0"
					}
				}

				map.put(sk, v);
			}
		}
		return map;
	}

	public List<String> filterEnvVars(String prefix) {
		Map<String,String> envvars = System.getenv();
		final String prefixDot = prefix + ".";
		List<String> list = new ArrayList<String>();
		for(Map.Entry<String, String> e : envvars.entrySet()) {
			String k = e.getKey();
			String v = e.getValue();

			if(k.equals(prefix)) {
				list.add(k);
			} else if(k.startsWith(prefixDot)) {
				String sk = k.substring(prefixDot.length());
				list.add(k);
			}
		}
		return list;
	}


	public void detect(Platform platform, Log log) {
		String s;

		setup(platform, log);

		if(crossCompilePrefix == null) {
			s = System.getProperty(K_cross_compile);
			if(s != null) {
				if(log != null)
					log.debug(K_cross_compile + ": is set \"" + s + "\"");
				crossCompilePrefix = s;
			}
		}
		if(crossCompilePrefix == null) {
			s = System.getenv(K_CROSS_COMPILE);
			if(s != null) {
				if(log != null)
					log.debug(K_CROSS_COMPILE + ": is set \"" + s + "\"");
				crossCompilePrefix = s;
			}
		}

		if(jreHome == null) {
			s = System.getProperty(K_jre_home);
			if(s != null) {
				if(log != null)
					log.debug(K_jre_home + ": is set \"" + s + "\"");
				checkJreHome(platform, s, log);
			}
		}
		if(jreHome == null) {
			s = System.getenv(K_JRE_HOME);
			if(s != null) {
				if(log != null)
					log.debug(" envvar." + K_JRE_HOME + ": is set \"" + s + "\"");
				checkJreHome(platform, s, log);
			}
		}

		if(jdkHome == null) {
			s = System.getProperty(K_jdk_home);
			if(s != null) {
				if(log != null)
					log.debug(K_jdk_home + ": is set \"" + s + "\"");
				checkJdkHome(platform, s, log);
			}
		}
		if(jdkHome == null) {
			s = System.getenv(K_JDK_HOME);
			if(s != null) {
				if(log != null)
					log.debug(" envvar." + K_JDK_HOME + ": is set \"" + s + "\"");
				checkJdkHome(platform, s, log);
			}
		}

		if(javaHomeSet == false) {
			s = System.getProperty(K_java_home);
			if(s != null) {
				if(log != null)
					log.debug(K_java_home + ": is set \"" + s + "\"");
				// This will auto-detect JRE/JDK and set (if not already found and set)
				checkJavaHome(platform, s, log);
			}
		}
		if(javaHomeSet == false) {
			s = System.getenv(K_JAVA_HOME);
			if(s != null) {
				if(log != null)
					log.debug(" envvar." + K_JAVA_HOME + ": is set \"" + s + "\"");
				checkJavaHome(platform, s, log);
			}
		}

		if(qtsdkHome == null) {
			s = System.getProperty(K_qtsdk_home);
			if(s != null) {
				if(log != null)
					log.debug(K_qtsdk_home + ": is set \"" + s + "\"");
				checkQtsdkHome(platform, s, log);
			}
		}
		if(qtsdkHome == null) {
			s = System.getenv(K_QTSDK_HOME);
			if(s != null) {
				if(log != null)
					log.debug(" envvar." + K_QTSDK_HOME + ": is set \"" + s + "\"");
				checkQtsdkHome(platform, s, log);
			}
		}

		if(msvcHome == null) {
			s = System.getProperty(K_msvc_home);
			if(s != null) {
				if(log != null)
					log.debug(K_msvc_home + ": is set \"" + s + "\"");
				checkMsvcHome(platform, s, log);
			}
		}
		if(msvcHome == null) {
			s = System.getenv(K_MSVC_HOME);
			if(s != null) {
				if(log != null)
					log.debug(" envvar." + K_MSVC_HOME + ": is set \"" + s + "\"");
				checkMsvcHome(platform, s, log);
			}
		}

		if(mingwHome == null) {
			s = System.getProperty(K_mingw_home);
			if(s != null) {
				if(log != null)
					log.debug(K_mingw_home + ": is set \"" + s + "\"");
				checkMingwHome(platform, s, log);
			}
		}
		if(mingwHome == null) {
			s = System.getenv(K_MINGW_HOME);
			if(s != null) {
				if(log != null)
					log.debug(" envvar." + K_MINGW_HOME + ": is set \"" + s + "\"");
				checkMingwHome(platform, s, log);
			}
		}

		if(mingwW64Home == null) {
			s = System.getProperty(K_mingw_w64_home);
			if(s == null)
				s = System.getProperty(K_mingw__w64_home);
			if(s != null) {
				if(log != null)
					log.debug(K_mingw_w64_home + ": is set \"" + s + "\"");
				checkMingwW64Home(platform, s, log);
			}
		}
		if(mingwW64Home == null) {
			s = System.getenv(K_MINGW_W64_HOME);
			if(s == null)
				s = System.getenv(K_MINGW__W64_HOME);
			if(s != null) {
				if(log != null)
					log.debug(" envvar." + K_MINGW_W64_HOME + ": is set \"" + s + "\"");
				checkMingwW64Home(platform, s, log);
			}
		}

		setupResolvers(platform);
	}

	private boolean checkJavaHome(Platform platform, String path, Log log) {
		File dir = new File(path);
		if(!checkDirectoryExists(dir, log))
			return false;
		if(!checkIsDirectory(dir, log))
			return false;

		File dirBin = new File(dir, "bin");
		if(!checkDirectoryExists(dirBin, log))
			return false;

		// Both JRE and JDK have this
		File exeJava = new File(dir, Utils.resolveFileSeparator(new String[] { "bin", platform.makeExeFilename("java") }));
		if(!checkFileExists(exeJava, log))
			return false;
		if(!checkIsFile(exeJava, log))
			return false;

		boolean bf = true;

		// Detect if JDK or JRE
		// $JAVA_HOME/lib/tools.jar  (JDK)
		// $JAVA_HOME/jre/lib/rt.jar  (JDK)
		// or
		// $JAVA_HOME/lib/rt.jar  (JRE)
		File libToolsJar = new File(dir, Utils.resolveFileSeparator(new String[] { "lib", "tools.jar" }));
		File jreLibRtJar = new File(dir, Utils.resolveFileSeparator(new String[] { "jre", "lib", "rt.jar" }));
		File libRtJar = new File(dir, Utils.resolveFileSeparator(new String[] { "lib", "rt.jar" }));
		if(libToolsJar.exists() && libToolsJar.isFile() && jreLibRtJar.exists() && jreLibRtJar.isFile()) {
			// Looks like a JDK
			if(jdkHome == null) {
				jdkHome = dir.getAbsolutePath();
				javaHomeSet = true;
				if(log != null)
					log.debug(" Setting jdk.home from java.home at " + dir.getAbsolutePath());
			}
		} else if(libRtJar.exists() && libRtJar.isFile()) {
			// Looks like a JRE
			if(jreHome == null) {
				jreHome = dir.getAbsolutePath();
				javaHomeSet = true;
				if(log != null)
					log.debug(" Setting jre.home from java.home at " + dir.getAbsolutePath());
			}
		} else {
			if(log != null)
				log.debug(" Unable to detect if this is JRE or JDK at " + dir.getAbsolutePath());
			bf = false;
		}

		return bf;
	}

	private boolean checkJreHome(Platform platform, String path, Log log) {
		File dir = new File(path);
		if(!checkDirectoryExists(dir, log))
			return false;
		if(!checkIsDirectory(dir, log))
			return false;

		File dirBin = new File(dir, "bin");
		if(!checkDirectoryExists(dirBin, log))
			return false;

		File exeJava = new File(dir, Utils.resolveFileSeparator(new String[] { "bin", platform.makeExeFilename("java") }));
		if(!checkFileExists(exeJava, log))
			return false;

		boolean jreLooksGood = true;

		// Check this is a JRE
		File libRtJar = new File(dir, Utils.resolveFileSeparator(new String[] { "lib", "rt.jar" }));
		if(!libRtJar.exists() || !libRtJar.isFile()) {
			if(log != null)
				log.debug(" Expected JRE file not found at " + libRtJar.getAbsolutePath());
			jreLooksGood = false;
		}
		File binJava = new File(dir, Utils.resolveFileSeparator(new String[] { "bin", platform.makeExeFilename("java") }));
		if(!binJava.exists() || !binJava.isFile()) {
			if(log != null)
				log.debug(" Expected JRE file not found at " + binJava.getAbsolutePath());
			jreLooksGood = false;
		}

		if(jreLooksGood) {
			jreHome = dir.getAbsolutePath();
			if(log != null)
				log.debug(" Using jre.home with " + dir.getAbsolutePath());
		} else {
			if(log != null)
				log.debug(" Unable to detect JRE at " + dir.getAbsolutePath());
		}

		return jreLooksGood;
	}

	private boolean checkJdkHome(Platform platform, String path, Log log) {
		File dir = new File(path);
		if(!checkDirectoryExists(dir, log))
			return false;
		if(!checkIsDirectory(dir, log))
			return false;

		File dirBin = new File(dir, "bin");
		if(!checkDirectoryExists(dirBin, log))
			return false;

		String exeJavaFilename = platform.makeExeFilename("java");
		File exeJava = new File(dirBin, exeJavaFilename);
		if(!checkFileExists(exeJava, log))
			return false;

		boolean jdkLooksGood = true;

		// Check this is a JDK
		File libToolsJar = new File(dir, Utils.resolveFileSeparator(new String[] { "lib", "tools.jar" }));
		File jreLibRtJar = new File(dir, Utils.resolveFileSeparator(new String[] { "jre", "lib", "rt.jar" }));
		if(!libToolsJar.exists() || !libToolsJar.isFile()) {
			if(log != null)
				log.debug(" Expected JDK file not found at " + libToolsJar.getAbsolutePath());
			jdkLooksGood = false;
		}
		if(jdkLooksGood && (!jreLibRtJar.exists() || !jreLibRtJar.isFile())) {
			if(log != null)
				log.debug(" Expected JDK file not found at " + jreLibRtJar.getAbsolutePath());
			jdkLooksGood = false;
		}
		File binJavac = new File(dir, Utils.resolveFileSeparator(new String[] { "bin", platform.makeExeFilename("javac") }));
		if(!binJavac.exists() || !binJavac.isFile()) {
			if(log != null)
				log.debug(" Expected JDK file not found at " + binJavac.getAbsolutePath());
			jdkLooksGood = false;
		}

		if(jdkLooksGood) {
			jdkHome = dir.getAbsolutePath();
			if(log != null)
				log.debug(" Using jdk.home with " + dir.getAbsolutePath());
		} else {
			if(log != null)
				log.debug(" Unable to detect JDK at " + dir.getAbsolutePath());
		}

		return jdkLooksGood;
	}

	private boolean checkQtsdkHome(Platform platform, String path, Log log) {
		File dir = new File(path);
		if(!checkDirectoryExists(dir, log))
			return false;
		if(!checkIsDirectory(dir, log))
			return false;

		File dirBin = new File(dir, "bin");
		if(!checkDirectoryExists(dirBin, log))
			return false;

		File exeQmake = new File(dir, Utils.resolveFileSeparator(new String[] { "bin", platform.makeExeFilename("qmake") }));
		if(!checkFileExists(exeQmake, log))
			return false;
		if(!checkIsFile(exeQmake, log))
			return false;

		boolean qtsdkLooksGood = true;
		if(qtsdkLooksGood) {
			qtsdkHome = dir.getAbsolutePath();
			if(log != null)
				log.debug(" Using qtsdk.home with " + dir.getAbsolutePath());
		} else {
			if(log != null)
				log.debug(" Unable to detect QTSDK_HOME at " + dir.getAbsolutePath());
		}

		return qtsdkLooksGood;
	}

	private boolean checkMsvcHome(Platform platform, String path, Log log) {
		File dir = new File(path);
		if(!checkDirectoryExists(dir, log))
			return false;
		if(!checkIsDirectory(dir, log))
			return false;

		File dirVcBin = new File(dir, Utils.resolveFileSeparator(new String[] { "VC", "bin" }));
		if(!checkDirectoryExists(dirVcBin, log))
			return false;

		File exeQmake = new File(dir, Utils.resolveFileSeparator(new String[] { "VC", "bin", platform.makeExeFilename("cl") }));
		if(!checkFileExists(exeQmake, log))
			return false;
		if(!checkIsFile(exeQmake, log))
			return false;

		boolean msvcLooksGood = true;
		if(msvcLooksGood) {
			msvcHome = dir.getAbsolutePath();
			if(log != null)
				log.debug(" Using msvc.home with " + dir.getAbsolutePath());
		} else {
			if(log != null)
				log.debug(" Unable to detect MSVC_HOME at " + dir.getAbsolutePath());
		}

		return msvcLooksGood;
	}

	private boolean checkMingwHome(Platform platform, String path, Log log) {
		File dir = new File(path);
		if(!checkDirectoryExists(dir, log))
			return false;
		if(!checkIsDirectory(dir, log))
			return false;

		File dirBin = new File(dir, Utils.resolveFileSeparator(new String[] { "bin" }));
		if(!checkDirectoryExists(dirBin, log))
			return false;

		boolean mingwLooksGood = true;
		boolean searchCrossCompile = false;

		File exeGcc = new File(dir, Utils.resolveFileSeparator(new String[] { "bin", platform.makeExeFilename("gcc") }));
		if(!checkFileExists(exeGcc, log)) {
			searchCrossCompile = true;
		} else {
			if(!checkIsFile(exeGcc, log))
				searchCrossCompile = true;
		}

		if(searchCrossCompile) {
			if(crossCompilePrefix != null) {
				String exeFilename = crossCompilePrefix + "gcc";
				File exeCrossCompileGcc = new File(dir, Utils.resolveFileSeparator(new String[] { "bin", platform.makeExeFilename(exeFilename) }));
				if(!checkFileExists(exeCrossCompileGcc, log)) {
					mingwLooksGood = false;
				} else {
					if(!checkIsFile(exeCrossCompileGcc, log))
						mingwLooksGood = false;
				}
			} else {
				mingwLooksGood = false;
			}
		}

		// Not sure why a Make tool would have cross-compile prefix so we don't test for that
		File exeMingwMake = new File(dir, Utils.resolveFileSeparator(new String[] { "bin", platform.makeExeFilename("mingw32-make") }));
		if(!checkFileExists(exeMingwMake, log)) {
			mingwLooksGood = false;
		} else {
			if(!checkIsFile(exeMingwMake, log))
				mingwLooksGood = false;
		}

		if(mingwLooksGood) {
			mingwHome = dir.getAbsolutePath();
			if(log != null)
				log.debug(" Using mingw.home with " + dir.getAbsolutePath());
		} else {
			if(log != null)
				log.debug(" Unable to detect MINGW_HOME at " + dir.getAbsolutePath());
		}

		return mingwLooksGood;
	}

	private boolean checkMingwW64Home(Platform platform, String path, Log log) {
		File dir = new File(path);
		if(!checkDirectoryExists(dir, log))
			return false;
		if(!checkIsDirectory(dir, log))
			return false;

		File dirBin = new File(dir, Utils.resolveFileSeparator(new String[] { "bin" }));
		if(!checkDirectoryExists(dirBin, log))
			return false;

		boolean mingwLooksGood = true;
		boolean searchCrossCompile = false;

		File exeGcc = new File(dir, Utils.resolveFileSeparator(new String[] { "bin", platform.makeExeFilename("gcc") }));
		if(!checkFileExists(exeGcc, log)) {
			searchCrossCompile = true;
		} else {
			if(!checkIsFile(exeGcc, log))
				searchCrossCompile = true;
		}

		if(searchCrossCompile) {
			if(crossCompilePrefix != null) {
				// FIXME: print out trying for CROSS_COMPILE
				String exeFilename = crossCompilePrefix + "gcc";
				File exeCrossCompileGcc = new File(dir, Utils.resolveFileSeparator(new String[] { "bin", platform.makeExeFilename(exeFilename) }));
				if(!checkFileExists(exeCrossCompileGcc, log)) {
					mingwLooksGood = false;
				} else {
					if(!checkIsFile(exeCrossCompileGcc, log))
						mingwLooksGood = false;
				}
			} else {
				mingwLooksGood = false;
			}
		}

		// FIXME: Alternatively this just needs to be available on the $PATH
		boolean searchPathForMingwMake = false;
		File exeMingwMake = new File(dir, Utils.resolveFileSeparator(new String[] { "bin", platform.makeExeFilename("mingw32-make") }));
		if(!checkFileExists(exeMingwMake, log)) {
			searchPathForMingwMake = false;
		} else {
			if(!checkIsFile(exeMingwMake, log))
				searchPathForMingwMake = false;
		}

		if(searchPathForMingwMake) {
			if(false)
				mingwLooksGood = false;
		}

		if(mingwLooksGood) {
			mingwW64Home = dir.getAbsolutePath();
			if(log != null)
				log.debug(" Using mingw_w64.home with " + dir.getAbsolutePath());
		} else {
			if(log != null)
				log.debug(" Unable to detect MINGW_W64_HOME at " + dir.getAbsolutePath());
		}

		return mingwLooksGood;
	}

	private boolean checkDirectoryExists(File dir, Log log) {
		if(dir.exists() == false) {
			if(log != null)
				log.debug(" directory does not exist: " + dir.getAbsolutePath());
			return false;
		}
		return true;
	}

	private boolean checkIsDirectory(File dir, Log log) {
		if(dir.isDirectory() == false) {
			if(log != null)
				log.debug(" path is not a directory: " + dir.getAbsolutePath());
			return false;
		}
		return true;
	}

	private boolean checkFileExists(File file, Log log) {
		if(file.exists() == false) {
			if(log != null)
				log.debug(" file does not exist: " + file.getAbsolutePath());
			return false;
		}
		return true;
	}

	private boolean checkIsFile(File file, Log log) {
		if(file.isFile() == false) {
			if(log != null)
				log.debug(" file does not exist: " + file.getAbsolutePath());
			return false;
		}
		return true;
	}

	public String getCrossCompilePrefix() {
		return crossCompilePrefix;
	}

	public String getQtPlatform() {
		return qtPlatform;
	}

	public String getQtMakespecs() {
		return qtMakespecs;
	}

	public List<String> getPathAppend() {
		return pathAppend;
	}

	public List<String> getLdLibraryPathAppend() {
		return ldLibraryPathAppend;
	}

	public List<String> getDyldLibraryPathAppend() {
		return dyldLibraryPathAppend;
	}

	public Map<String, String> getEnvvarGlobal() {
		return envvarGlobal;
	}

	public Map<String, String> getEnvvarQt() {
		return envvarQt;
	}

	public Map<String, String> getEnvvarGcc() {
		return envvarGcc;
	}

	public Map<String, String> getEnvvarMsvc() {
		return envvarMsvc;
	}

	public Map<String, String> getEnvvarMingw() {
		return envvarMingw;
	}

	public Map<String, String> getEnvvarMingwW64() {
		return envvarMingwW64;
	}

	public Map<String, String> getEnvvarJava() {
		return envvarJava;
	}

}
