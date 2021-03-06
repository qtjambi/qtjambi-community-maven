package org.qtjambi.maven.plugins.utils.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.qtjambi.maven.plugins.utils.Platform;
import org.qtjambi.maven.plugins.utils.Toolchain;
import org.qtjambi.maven.plugins.utils.resolvers.DefaultEnvironmentResolver;
import org.qtjambi.maven.plugins.utils.resolvers.GccEnvironmentResolver;
import org.qtjambi.maven.plugins.utils.resolvers.JavaEnvironmentResolver;
import org.qtjambi.maven.plugins.utils.resolvers.MingwEnvironmentResolver;
import org.qtjambi.maven.plugins.utils.resolvers.MingwW64EnvironmentResolver;
import org.qtjambi.maven.plugins.utils.resolvers.MsvcEnvironmentResolver;
import org.qtjambi.maven.plugins.utils.resolvers.QtEnvironmentResolver;
import org.qtjambi.maven.plugins.utils.shared.Utils;

// TODO: Dump available platforms, disambiguate
// TODO: This is definitely wrote one version to throw away and start over
//  ideas...
//     step1) global detection phase (i.e. obtain the facts), full detect, or detect only things we need
//     step2) dump detected
//     step3) apply override (command line, properties, environment variable)
//     step4) dump things changed (dump also duplicate/conflicting info and which is used)
//     step5) 
//   move things away from being coded, to being in property files loaded
public class Arguments {
	public static final String K_toolchain			= "toolchain";			// gcc, mingw, mingw_w64/mingw-w64, msvc/msvc2010, msvc.x64
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
	public static final String K_java_home_target	= "java.home.target";
	public static final String K_java_osarch_target	= "java.osarch.target";
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
	public static final String K_MINGW_W64_HOME		= "MINGW_W64_HOME";		// preferred
	public static final String K_MINGW__W64_HOME	= "MINGW-W64_HOME";
	public static final String K_JAVA_HOME			= "JAVA_HOME";			// what maven/javac runs in
	public static final String K_JAVA_HOME_TARGET	= "JAVA_HOME_TARGET";	// what we link JNI against
	public static final String K_JAVA_OSARCH_TARGET	= "JAVA_OSARCH_TARGET";	// subdir name in $JAVA_HOME_TARGET/include/????
	public static final String K_JRE_HOME			= "JRE_HOME";
	public static final String K_JDK_HOME			= "JDK_HOME";

	private static final String[] crossCompileTryList = {		// FIXME: Move to text resource
		"x86_64-w64-mingw32-"
	};

	private String crossCompilePrefix;
	private String qtPlatform;
	private String qtMakespecs;
	private Toolchain toolchain;
	private String toolchainKind;
	private List<String> pathAppend;
	private List<String> ldLibraryPathAppend;
	private List<String> dyldLibraryPathAppend;
	private Map<String,Object> envvarGlobal;
	private Map<String,Object> envvarQt;		// :+-= prefix (append pathSep, append verbatim, unset, set verbatim)
	private Map<String,Object> envvarGcc;
	private Map<String,Object> envvarMsvc;
	private Map<String,Object> envvarMingw;
	private Map<String,Object> envvarMingwW64;
	private Map<String,Object> envvarJava;

	private String qtsdkHome;
	private String msvcHome;
	private String mingwHome;
	private String mingwW64Home;
	private boolean javaHomeSet;
	private boolean javaHomeTargetSet;
	private boolean javaOsarchTargetSet;
	private String jreHome;
	private String jdkHome;
	private String javaHomeTarget;
	private String javaOsarchTarget;

	public Arguments() {
		pathAppend = new ArrayList<String>();
		ldLibraryPathAppend = new ArrayList<String>();
		dyldLibraryPathAppend = new ArrayList<String>();

		envvarGlobal = new HashMap<String, Object>();
		envvarQt = new HashMap<String, Object>();
		envvarMsvc = new HashMap<String, Object>();
		envvarGcc = new HashMap<String, Object>();
		envvarMingw = new HashMap<String, Object>();
		envvarMingwW64 = new HashMap<String, Object>();
		envvarJava = new HashMap<String, Object>();
	}

	public void setup(Platform platform, Log log) throws MojoFailureException {
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

		if(toolchain == null) {
			s = System.getProperty(K_toolchain);
			if(s != null) {
				toolchain = Utils.toolchainFromLabel(s);
				if(toolchain == null) {
					log.error(K_toolchain + ": is invalid \"" + s + "\"");
					// FIXME: Immediate failure error!
				}
				if(log != null)
					log.debug(K_toolchain + ": is set \"" + s + "\"");
			}
		}
		// toolchain.kind
		if(toolchainKind == null) {
			s = System.getProperty(K_toolchain);
			if(s != null) {
				toolchainKind = Utils.toolchainKindFromLabel(s);
				if(toolchainKind == null) {
					//log.error(K_toolchain + ": is invalid \"" + s + "\"");
					// FIXME: Immediate failure error!
				}
				if(log != null)
					log.debug(K_toolchain + ": is set \"" + toolchainKind + "\"");
			}
		}

		Map<String,String[]> tmpMap;
		tmpMap = filterSystemProperties(K_path_append);
		for(String[] vA : tmpMap.values())
			pathAppend.addAll(Arrays.asList(vA));			// ignore key

		tmpMap = filterSystemProperties(K_ld_library_path_append);
		for(String[] v : tmpMap.values())
			ldLibraryPathAppend.addAll(Arrays.asList(v));	// ignore key

		tmpMap = filterSystemProperties(K_dyld_library_path_append);
		for(String[] v : tmpMap.values())
			dyldLibraryPathAppend.addAll(Arrays.asList(v));	// ignore key

		// FIXME: If there are no explicit settings we want to auto-apply some

		tmpMap = filterSystemProperties(K_envvar_global);
		for(Map.Entry<String,String[]> e : tmpMap.entrySet())
			envvarGlobal.put(e.getKey(), e.getValue());

		// envvar.qt.PATH.0=:${qt.home}/bin
		// envvar.qt.PATH.0=:${qt.home}/lib
		tmpMap = filterSystemProperties(K_envvar_qt);
		for(Map.Entry<String,String[]> e : tmpMap.entrySet())
			envvarQt.put(e.getKey(), e.getValue());

		tmpMap = filterSystemProperties(K_envvar_gcc);
		for(Map.Entry<String,String[]> e : tmpMap.entrySet())
			envvarGcc.put(e.getKey(), e.getValue());

		// envvar.msvc.PATH.0=:${msvc.home}\VC\bin
		// more...
		tmpMap = filterSystemProperties(K_envvar_msvc);
		for(Map.Entry<String,String[]> e : tmpMap.entrySet())
			envvarMsvc.put(e.getKey(), e.getValue());

		// envvar.mingw.PATH.0=:${mingw.home}\bin
		tmpMap = filterSystemProperties(K_envvar_mingw);
		for(Map.Entry<String,String[]> e : tmpMap.entrySet())
			envvarMingw.put(e.getKey(), e.getValue());

		// envvar.mingw_w64.PATH.0=:${mingw_w64.home}\bin
		tmpMap = filterSystemProperties(K_envvar_mingw_w64);
		for(Map.Entry<String,String[]> e : tmpMap.entrySet())
			envvarMingwW64.put(e.getKey(), e.getValue());

		// envvar.java.PATH.0=:${java.home}\bin
		tmpMap = filterSystemProperties(K_envvar_java);
		for(Map.Entry<String,String[]> e : tmpMap.entrySet())
			envvarJava.put(e.getKey(), e.getValue());

		if(toolchain == null) {		// Attempt to auto-detect
			// If we were provided something, use it

			// If we have a default per platform, use it
			if(toolchain == null) {
				if(platform.isLinux(false))
					toolchain = Toolchain.gcc;
				if(platform.isWindows(false))
					toolchain = Toolchain.mingw;
				if(platform.isMacosx(false))
					toolchain = Toolchain.gcc;
			}

			if(toolchain == null) {
				// FIXME: This is a immediate failure error (we must pick something)
				throw new MojoFailureException("unable to auto-detect toolchain set " + K_toolchain + " property parameter");
			}
			log.warn(K_toolchain + " auto-detect to " + Utils.toolchainToLabel(toolchain));
		}
		platform.setToolchain(toolchain);
	}

	// FIXME: Consider making these available via a Factory pattern (setup on demand) so changes
	//  to config can be made easier.  Maybe lock down the config on first real use (by warning
	//  about modifications made after lockdown)
	public void setupResolvers(Platform platform) {
		DefaultEnvironmentResolver globalEnvironmentResolver = new DefaultEnvironmentResolver(platform);
		{
			globalEnvironmentResolver.setPathAppend(pathAppend);
			globalEnvironmentResolver.setLdLibraryPathAppend(ldLibraryPathAppend);
			globalEnvironmentResolver.setDyldLibraryPathAppend(dyldLibraryPathAppend);
			globalEnvironmentResolver.setEnvvarMap(envvarGlobal);
		}
		platform.setGlobalEnvironmentResolver(globalEnvironmentResolver);

		// JAVA
		JavaEnvironmentResolver javaEnvironmentResolver = new JavaEnvironmentResolver(platform);
		if(javaEnvironmentResolver instanceof DefaultEnvironmentResolver) {	// Globals
			DefaultEnvironmentResolver defaultEnvironmentResolver = (DefaultEnvironmentResolver) javaEnvironmentResolver;
			defaultEnvironmentResolver.setPathAppend(pathAppend);
			defaultEnvironmentResolver.setLdLibraryPathAppend(ldLibraryPathAppend);
			defaultEnvironmentResolver.setDyldLibraryPathAppend(dyldLibraryPathAppend);
			defaultEnvironmentResolver.setEnvvarMap(envvarGlobal);
		}
		javaEnvironmentResolver.setEnvvarMap(envvarJava);
		// FIXME CHECKME: autoConfigure?
		platform.setJavaEnvironmentResolver(javaEnvironmentResolver);

		// GCC
		GccEnvironmentResolver gccEnvironmentResolver = new GccEnvironmentResolver(platform);
		if(gccEnvironmentResolver instanceof DefaultEnvironmentResolver) {	// Globals
			DefaultEnvironmentResolver defaultEnvironmentResolver = (DefaultEnvironmentResolver) gccEnvironmentResolver;
			defaultEnvironmentResolver.setPathAppend(pathAppend);
			defaultEnvironmentResolver.setLdLibraryPathAppend(ldLibraryPathAppend);
			defaultEnvironmentResolver.setDyldLibraryPathAppend(dyldLibraryPathAppend);
			defaultEnvironmentResolver.setEnvvarMap(envvarGlobal);
		}
		gccEnvironmentResolver.setEnvvarMap(envvarGcc);
		gccEnvironmentResolver.autoConfigure();
		gccEnvironmentResolver.setCrossCompilePrefix(crossCompilePrefix);
		platform.setGccEnvironmentResolver(gccEnvironmentResolver);

		// MINGW
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
		mingwEnvironmentResolver.setCrossCompilePrefix(crossCompilePrefix);
		platform.setMingwEnvironmentResolver(mingwEnvironmentResolver);

		// MINGW-W64
		MingwW64EnvironmentResolver mingwW64EnvironmentResolver = new MingwW64EnvironmentResolver(platform);
		if(mingwW64EnvironmentResolver instanceof DefaultEnvironmentResolver) {	// Globals
			DefaultEnvironmentResolver defaultEnvironmentResolver = (DefaultEnvironmentResolver) mingwW64EnvironmentResolver;
			defaultEnvironmentResolver.setPathAppend(pathAppend);
			defaultEnvironmentResolver.setLdLibraryPathAppend(ldLibraryPathAppend);
			defaultEnvironmentResolver.setDyldLibraryPathAppend(dyldLibraryPathAppend);
			defaultEnvironmentResolver.setEnvvarMap(envvarGlobal);
		}
		mingwW64EnvironmentResolver.setEnvvarMap(envvarMingwW64);
		if(mingwW64Home != null)
			mingwW64EnvironmentResolver.setHome(mingwW64Home, true);
		mingwW64EnvironmentResolver.setCrossCompilePrefix(crossCompilePrefix);
		platform.setMingwW64EnvironmentResolver(mingwW64EnvironmentResolver);

		boolean x64Flag = false;
		if(toolchainKind != null) {
			if(toolchainKind.compareToIgnoreCase("x64") == 0)	// FIXME: Make it better
				x64Flag = true;
		}
		// MSVC
		MsvcEnvironmentResolver msvcEnvironmentResolver = new MsvcEnvironmentResolver(platform);
		if(msvcEnvironmentResolver instanceof DefaultEnvironmentResolver) {	// Globals
			DefaultEnvironmentResolver defaultEnvironmentResolver = (DefaultEnvironmentResolver) msvcEnvironmentResolver;
			defaultEnvironmentResolver.setPathAppend(pathAppend);
			defaultEnvironmentResolver.setLdLibraryPathAppend(ldLibraryPathAppend);
			defaultEnvironmentResolver.setDyldLibraryPathAppend(dyldLibraryPathAppend);
			defaultEnvironmentResolver.setEnvvarMap(envvarGlobal);
		}
		msvcEnvironmentResolver.setEnvvarMap(envvarMsvc);
		if(msvcHome != null)
			msvcEnvironmentResolver.setHome(msvcHome, x64Flag, true);
		platform.setMsvcEnvironmentResolver(msvcEnvironmentResolver);

		// QT
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

	public Map<String,String[]> filterSystemProperties(String prefix) {
		Properties props = System.getProperties();
		final String prefixDot = prefix + ".";
		Map<String,String[]> map = new HashMap<String,String[]>();
		for(Map.Entry<Object, Object> e : props.entrySet()) {
			Object kObj = e.getKey();
			String k = kObj.toString();
			Object vObj = e.getValue();
			String v = vObj.toString();

			boolean store = false;
			if(k.equals(prefix)) {
				store = true;
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
				k = sk;
				store = true;
			}
			if(store) {
				String[] sA = map.get(k);
				sA = Utils.safeStringArrayAppend(sA, v);
				map.put(k, sA);
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


	public void detect(Platform platform, Log log) throws MojoFailureException {
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

		if(javaHomeTargetSet == false) {
			s = System.getProperty(K_java_home_target);
			if(s != null) {
				if(log != null)
					log.debug(K_java_home_target + ": is set \"" + s + "\"");
				// This will auto-detect JRE/JDK and set (if not already found and set)
				checkJavaHomeTarget(platform, s, log);
			}
		}
		if(javaHomeTargetSet == false) {
			s = System.getenv(K_JAVA_HOME_TARGET);
			if(s != null) {
				if(log != null)
					log.debug(" envvar." + K_JAVA_HOME_TARGET + ": is set \"" + s + "\"");
				checkJavaHomeTarget(platform, s, log);
			}
		}

		if(javaOsarchTargetSet == false) {
			s = System.getProperty(K_java_osarch_target);
			if(s != null) {
				if(log != null)
					log.debug(K_java_osarch_target + ": is set \"" + s + "\"");
				// This will auto-detect JRE/JDK and set (if not already found and set)
				if(checkJavaOsarchTarget(platform, javaHomeTarget, s, log)) {
					javaOsarchTarget = s;
					javaOsarchTargetSet = true;
				}
			}
		}
		if(javaOsarchTargetSet == false) {
			s = System.getenv(K_JAVA_OSARCH_TARGET);
			if(s != null) {
				if(log != null)
					log.debug(" envvar." + K_JAVA_OSARCH_TARGET + ": is set \"" + s + "\"");
				if(checkJavaOsarchTarget(platform, javaHomeTarget, s, log)) {
					javaOsarchTarget = s;
					javaOsarchTargetSet = true;
				}
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

	private boolean checkJavaHomeTarget(Platform platform, String path, Log log) {
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
			if(javaHomeTarget == null) {
				javaHomeTarget = dir.getAbsolutePath();
				javaHomeTargetSet = true;
				if(log != null)
					log.debug(" Setting java.home.target with " + dir.getAbsolutePath());
			}
		} else if(libRtJar.exists() && libRtJar.isFile()) {
			if(log != null)
				log.debug(" Unable to detect if this is JDK at " + dir.getAbsolutePath() + "; looks like a JRE, must have JDK");
			bf = false;
		} else {
			if(log != null)
				log.debug(" Unable to detect if this is JDK at " + dir.getAbsolutePath());
			bf = false;
		}

		return bf;
	}

	private boolean checkJavaOsarchTarget(Platform platform, String javaHome, String osarch, Log log) {
		File dir = new File(javaHome);
		if(!checkDirectoryExists(dir, log))
			return false;
		if(!checkIsDirectory(dir, log))
			return false;

		File includeDir = new File(dir, "include");
		if(!checkDirectoryExists(includeDir, log))
			return false;

		boolean bf = true;

		File osarchDir = new File(dir, osarch);
		if(!checkDirectoryExists(osarchDir, log))
			return false;

		File jniMdHFile = new File(osarchDir, "jni_md.h");
		if(!jniMdHFile.exists()) {
			log.warn(" Expected to find file: " + osarchDir.getAbsolutePath() + File.pathSeparator + "jni_md.h");
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
		String autoDetectCrossCompilePrefix = null;

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
				boolean found = false;
				for(String testPrefix : crossCompileTryList) {
					String exeFilename = testPrefix + "gcc";
					File exeCrossCompileGcc = new File(dir, Utils.resolveFileSeparator(new String[] { "bin", platform.makeExeFilename(exeFilename) }));
					if(checkFileExists(exeCrossCompileGcc, log)) {
						found = true;
						autoDetectCrossCompilePrefix = testPrefix;
					} else {
						if(checkIsFile(exeCrossCompileGcc, log)) {
							found = true;
							autoDetectCrossCompilePrefix = testPrefix;
						}
					}
				}
				if(!found)
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
			if(autoDetectCrossCompilePrefix != null) {
				if(crossCompilePrefix != null && crossCompilePrefix.equals(autoDetectCrossCompilePrefix) == false) {
					if(log != null)
						log.warn(" auto-detected MINGW_W64 using cross-compile prefix: " + autoDetectCrossCompilePrefix + "; but set to CROSS_COMPILE=" + crossCompilePrefix);
				} else {
					if(log != null)
						log.debug(" auto-detected MINGW_W64 using cross-compile prefix: " + autoDetectCrossCompilePrefix);
					crossCompilePrefix = autoDetectCrossCompilePrefix;
				}
			}
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

	public String resolveJavaHome(String param) {
		String s = null;
		if(param == null)
			param = "any";	// if you call this method this is the default kind of lookup
		if(param.equals("any")) {
			s = jdkHome;
			if(s == null)
				s = jreHome;
		} else if(param.equals("jdk")) {
			s = jdkHome;
		} else if(param.equals("jre")) {
			s = jreHome;
		}
		return s;
	}

	public String getJavaHome() {
		if(javaHomeSet) {
			if(jdkHome != null)
				return jdkHome;
			if(jreHome != null)
				return jreHome;
		}
		return null;
	}

	public String resolveJavaHomeTarget(String param) {
		String s = null;
		if(param == null)
			param = "any";	// if you call this method this is the default kind of lookup
		if(param.equals("any")) {
			s = javaHomeTarget;
			if(s == null)
				s = jdkHome;
			if(s == null)
				s = jreHome;
		} else if(param.equals("jdk")) {
			s = javaHomeTarget;
			if(s == null)
				s = jdkHome;
		} else if(param.equals("jre")) {
			s = javaHomeTarget;
			if(s == null)
				s = jreHome;
		}
		return s;
	}

	public String getJavaOsarchTarget() {
		return javaOsarchTarget;
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

	public Map<String, Object> getEnvvarGlobal() {
		return envvarGlobal;
	}

	public Map<String, Object> getEnvvarQt() {
		return envvarQt;
	}

	public Map<String, Object> getEnvvarGcc() {
		return envvarGcc;
	}

	public Map<String, Object> getEnvvarMsvc() {
		return envvarMsvc;
	}

	public Map<String, Object> getEnvvarMingw() {
		return envvarMingw;
	}

	public Map<String, Object> getEnvvarMingwW64() {
		return envvarMingwW64;
	}

	public Map<String, Object> getEnvvarJava() {
		return envvarJava;
	}

}
