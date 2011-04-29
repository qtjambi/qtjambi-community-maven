package org.qtjambi.maven.plugins.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.qtjambi.maven.plugins.utils.internal.Arguments;
import org.qtjambi.maven.plugins.utils.internal.ProcessBuilder;
import org.qtjambi.maven.plugins.utils.resolvers.DefaultEnvironmentResolver;
import org.qtjambi.maven.plugins.utils.resolvers.GccEnvironmentResolver;
import org.qtjambi.maven.plugins.utils.resolvers.JavaEnvironmentResolver;
import org.qtjambi.maven.plugins.utils.resolvers.MingwEnvironmentResolver;
import org.qtjambi.maven.plugins.utils.resolvers.MsvcEnvironmentResolver;
import org.qtjambi.maven.plugins.utils.shared.QmakeUtils;
import org.qtjambi.maven.plugins.utils.shared.Utils;

/**
 * The purpose of this is to allow the maintainers of QtJambi to
 *  request platform / diagnostic information from users to help
 *  resolve build or runtime issues.
 *
 * @goal report
 * @author Darryl L. Miles
 *
 */
public class DiagnosticMojo extends AbstractMojo {
	private boolean initDone;

	/**
	 * @parameter expression="${project.properties}"
	 */
	private Map<String,String> projectProperties;

	public void execute() throws MojoExecutionException, MojoFailureException {
		init();

		Context context;
		{
			Platform platform = new Platform();
			platform.detect(getLog());

			Arguments arguments = new Arguments();
			arguments.detect(platform, getLog());

			context = new Context(platform, arguments, getLog());
		}

		// Maven information, Maven Java runtime information
		// MAVEN_HOME, maven.version, path to mvn/mvn.bat
		dumpMavenInfo();

		// System information
		// CPU information
		Properties props = System.getProperties();
		TreeMap<Object,Object>propsSortedMap = new TreeMap<Object,Object>(props);	// sort it
		for(Map.Entry<Object,Object> entry : propsSortedMap.entrySet()) {
			Object kObj = entry.getKey();
			String k = kObj.toString();
			Object vObj = entry.getValue();
			String v = vObj.toString();

			getLog().info("sysprop." + k + "=" + v);
		}

		Map<String,String> envvarMap = System.getenv();
		TreeMap<String,String> envvarSortedMap = new TreeMap<String,String>(envvarMap);	// sort it
		for(Map.Entry<String,String> entry : envvarSortedMap.entrySet()) {
			String k = entry.getKey();
			String v = entry.getValue();

			// INCLUDE LIB LIBPATH 
			if(k.equals("PATH")) {
				String[] pathSplit = Utils.stringSplit(v, File.pathSeparator);
				final int len = k.length();
				String spc = "";
				for(int i = 0; i < len; i++)
					spc += " ";
				boolean first = true;
				for(String p : pathSplit) {
					if(first) {
						getLog().info("envvar." + k + "=" + p);
						first = false;
					} else {
						getLog().info("       " + spc + File.pathSeparator + p);
					}
				}
			} else {
				getLog().info("envvar." + k + "=" + v);
			}
		}

		// OS information
		if(context.getPlatform().isLinux(true))
			dumpLinuxInfo(context.getPlatform());
		if(context.getPlatform().isWindows(true))
			dumpWindowsInfo(context.getPlatform());
		if(context.getPlatform().isMacosx(true))
			dumpMacosxInfo(context.getPlatform());

		// Path check
		pathCheck();
		pathCheck(context.getPlatform(), "java");
		pathCheck(context.getPlatform(), "javac");

		pathCheck(context.getPlatform(), "qmake");

		pathCheck(context.getPlatform(), "mingw32-make");	// MinGW/MinGW-W64
		pathCheck(context.getPlatform(), "make");			// gmake ?
		pathCheck(context.getPlatform(), "nmake");			// MSVC

		pathCheck(context.getPlatform(), "gcc");
		pathCheck(context.getPlatform(), "ld");

		pathCheck(context.getPlatform(), "cl");
		pathCheck(context.getPlatform(), "lib");

		Platform platform = context.getPlatform();
		// Java information (JRE / JDK version detect)
		if(!platform.isWindows(true)) {
			// We don't need the -nodename (-n)
			runCheck(context, new DefaultEnvironmentResolver(platform), "uname", new Object[] { "-srmpio" });
		}
		runCheck(context, new JavaEnvironmentResolver(platform), "java", new Object[] { "-version" });
		runCheck(context, new JavaEnvironmentResolver(platform), "javac", new Object[] { "-version" });
		runCheck(context, new GccEnvironmentResolver(platform), "gcc", new Object[] { "-v" });
		runCheck(context, new GccEnvironmentResolver(platform), "ld", new Object[] { "-v" });
		runCheck(context, new MingwEnvironmentResolver(platform), "mingw32-make", new Object[] { "-v" });
		if(context.getPlatform().isWindows(true)) {
			runCheck(context, new MsvcEnvironmentResolver(platform), "cl", new Object[] { "/help" });	// Does not appear to have anything
			runCheck(context, new MsvcEnvironmentResolver(platform), "lib", new Object[] { "/verbose" });
		}

		// Package version checks
		// GCC/ld
		// 
		// Phonon

		// Qt SDK installation check
		String qmakeVersion = qmakeVersionDetect(context);
		if(qmakeVersion == null)
			getLog().error(" QMAKE_VERSION=<unable_to_detect_version>");
		else
			getLog().info(" QMAKE_VERSION=" + qmakeVersion);

		// Test build CPP application with QMAKE
		// extract and build qt_test
		Project project = null;
		try {
			project = Project.extractFromClasspath(context, "qt_test");
			if(project != null) {
				project.runQmake(null);
				project.runMake();
				project.runProjectProgram("qt_test");
			}
		} finally {
			if(project != null)
				project.cleanup();
			project = null;
		}

		// Generator run test
		// juic run test
		// qdoc3 run test

		//throw new MojoFailureException("Not Implemented");
	}

	private String qmakeVersionDetect(Context context) throws MojoFailureException {
		QmakeUtils qmakeUtils = new QmakeUtils(context);
		return qmakeUtils.getQmakeVersion();
	}

	private void pathCheck(Platform platform, String filename) {
		filename = platform.makeExeFilename(filename);
		String pathSeparator = File.pathSeparator;
		String path = System.getenv("PATH");
		if(path == null)
			return;
		Set<String> seenSet = new HashSet<String>();
		Set<String> dupeSet = new HashSet<String>();
		int i = 0;
		int n;
		while((n = path.indexOf(pathSeparator, i)) >= 0) {
			String pathElement = path.substring(i, n);
			if(seenSet.contains(pathElement)) {
				continue;	// only report the first one
			} else {
				seenSet.add(pathElement);
				File dir = new File(pathElement);
				if(dir.exists() && dir.isDirectory()) {
					File file = new File(dir, filename);
					if(file.exists() && file.isFile()) {
						if(file.canExecute()) {
							getLog().info("found " + filename + " at " + file.getAbsolutePath());
						} else {
							getLog().info("found " + filename + " at " + file.getAbsolutePath() + "; canExecute()==false");
						}
						if(dupeSet.contains(filename))
							getLog().warn("duplicate " + filename);
						else
							dupeSet.add(filename);
					}
				}
			}
			i = n + 1;
		}
	}

	private void pathCheck() {
		String pathSeparator = File.pathSeparator;
		String path = System.getenv(DefaultEnvironmentResolver.K_PATH);
		if(path == null) {
			getLog().warn(" PATH envvar is not set; strange");
			return;
		}
		Set<String> seenSet = new HashSet<String>();
		int i = 0;
		int n;
		while((n = path.indexOf(pathSeparator, i)) >= 0) {
			String pathElement = path.substring(i, n);
			if(seenSet.contains(pathElement)) {
				getLog().warn(" PATH element duplicate: " + pathElement);
				continue;
			} else {
				seenSet.add(pathElement);
				File dir = new File(pathElement);
				if(!dir.exists()) {
					getLog().warn(" PATH element: " + dir.getAbsolutePath() + "; does not exist");
				} else if(!dir.isDirectory()) {
					getLog().warn(" PATH element: " + dir.getAbsolutePath() + "; is not a directory");
				}
			}
			i = n + 1;
		}

	}

	private void runCheck(Context context, IEnvironmentResolver environmentResolver, String filename, Object[] args) {
		Map<String,String> env = System.getenv();
		environmentResolver.applyEnvironmentVariables(env);
		String pathValue = env.get(DefaultEnvironmentResolver.K_PATH);
		if(pathValue == null)
			return;
		String[] pathA = Utils.stringArraySplit(pathValue, File.pathSeparator);

		for(String p : pathA) {
			File dir = new File(p);
			String thisFilename = environmentResolver.resolveCommand(null, filename);	// resolve ABS exec 
			File file = new File(dir, thisFilename);
			if(dir.exists() && dir.isDirectory()) {
				if(file.exists() && file.isFile() && file.canExecute()) {
					Integer exitStatus = runCheckOnce(context, environmentResolver, file.getAbsolutePath(), args);
					// FIXME: Log this fact
				}
				// If the dir does not exist pathCheck() will have logged it to the user
			}
		}
	}

	private Integer runCheckOnce(Context context, IEnvironmentResolver environmentResolver, String filename, Object[] args) {
		// FIXME: We should run all commands found in PATH
		List<String> command = new ArrayList<String>();
		filename = context.getPlatform().makeExeFilename(filename);
		command.add(filename);
		for(Object o : args)
			command.add(o.toString());
		ProcessBuilder processBuilder = new ProcessBuilder(command);

		File directory = null;

		if(directory != null)
			processBuilder.directory(directory);

		Map<String,String> env = processBuilder.environment();
		environmentResolver.applyEnvironmentVariables(env);

		OutputStream stdinStream = null;
		InputStream stdoutStream = null;
		InputStream stderrStream = null;
		Process process = null;
		Integer exitStatus = null;
		try {
			try {
				process = processBuilder.start();
			} catch(IOException eat) {
				return null;
			}

			stdinStream = process.getOutputStream();
			stdinStream.close();
			stdinStream = null;

			{
				stderrStream = process.getErrorStream();
				InputStreamReader isr = new InputStreamReader(stderrStream);
				BufferedReader br = new BufferedReader(isr);
				String s;
				while((s = br.readLine()) != null) {
					StringBuilder sb = new StringBuilder();
					sb.append(filename);
					sb.append(" >E> ");
					sb.append(s);
					getLog().info(sb.toString());
				}
				br.close();
				br = null;
				isr.close();
				isr = null;
				stderrStream.close();
				stderrStream = null;
			}

			{
				stdoutStream = process.getInputStream();
				InputStreamReader isr = new InputStreamReader(stdoutStream);
				BufferedReader br = new BufferedReader(isr);
				String s;
				while((s = br.readLine()) != null) {
					StringBuilder sb = new StringBuilder();
					sb.append(filename);
					sb.append(" >>> ");
					sb.append(s);
					getLog().info(sb.toString());
				}
				br.close();
				br = null;
				isr.close();
				isr = null;
				stdoutStream.close();
				stdoutStream = null;
			}

			exitStatus = process.waitFor();
			if(exitStatus.intValue() != 0)
				getLog().info(filename + " === exitStatus=" + exitStatus);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(stdinStream != null) {
				try {
					stdinStream.close();
				} catch (IOException eat) {
				}
				stdinStream = null;
			}
			if(stdoutStream != null) {
				try {
					stdoutStream.close();
				} catch (IOException eat) {
				}
				stdoutStream = null;
			}
			if(stderrStream != null) {
				try {
					stderrStream.close();
				} catch (IOException eat) {
				}
				stderrStream = null;
			}
			if(process != null) {
				process.destroy();
				process = null;
			}
		}
		return exitStatus;
	}

	private void init() {
		if(initDone)
			return;

		initDone = true;
	}

	private void dumpMavenInfo() {
		getLog().info("Maven:");
		dumpEnvVar(getLog(), "MAVEN_HOME", true);
		dumpSystemProperty(getLog(), "maven.home", true);
	}

	private void dumpWindowsInfo(Platform platform) {
		getLog().info("Operating System:");
		// OS, Version, ServicePack
		getLog().info("os.name=" + platform.getOsName() + "; os.version=" + platform.getOsVersion() + "; os.arch=" + platform.getOsArch());
	}

	private void dumpMacosxInfo(Platform platform) {
		getLog().info("Operating System:");
		getLog().info("os.name=" + platform.getOsName() + "; os.version=" + platform.getOsVersion() + "; os.arch=" + platform.getOsArch());
	}

	private void dumpLinuxInfo(Platform platform) {
		getLog().info("Operating System:");
		File f = new File("/etc/system-release");
		if(f.exists() && dumpFile(getLog(), f))
			return;

		f = new File("/etc/issue");
		if(f.exists() && dumpFile(getLog(), f))
			return;
	}

	public static void dumpEnvVar(Log log, String name, boolean alwaysDump) {
		String v = System.getenv(name);
		if(v != null || alwaysDump) {
			if(v != null)
				log.info("envvar." + name + "=" + v.toString());
			else
				log.info("envvar." + name + " is not set");
		}
	}

	public static void dumpSystemProperty(Log log, String name, boolean alwaysDump) {
		String v = System.getProperty(name);
		if(v != null || alwaysDump) {
			if(v != null)
				log.info("sysprop." + name + "=" + v.toString());
			else
				log.info("sysprop." + name + " is not set");
		}
	}

	public static boolean dumpFile(Log log, File f) {
		FileReader fileReader = null;
		try {
			fileReader = new FileReader(f);
			BufferedReader reader = new BufferedReader(fileReader);

			String line;
			while((line = reader.readLine()) != null) {
				StringBuilder sb = new StringBuilder();
				sb.append(f.getAbsolutePath());
				sb.append(" > > ");
				sb.append(line);
				log.info(sb.toString());
			}

			fileReader.close();
			fileReader = null;
			return true;
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			if(fileReader != null) {
				try {
					fileReader.close();
				} catch (IOException eat) {
				}
				fileReader = null;
			}
		}
		return false;
	}

	private String getProjectProperty(String key) {
		if(projectProperties == null)
			return null;
		Object o = projectProperties.get(key);
		if(o == null)
			return null;
		return o.toString();
	}
}
