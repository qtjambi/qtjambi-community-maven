package org.qtjambi.maven.plugins.qmake;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.qtjambi.maven.plugins.utils.Context;
import org.qtjambi.maven.plugins.utils.Platform;
import org.qtjambi.maven.plugins.utils.Project;
import org.qtjambi.maven.plugins.utils.internal.Arguments;
import org.qtjambi.maven.plugins.utils.internal.ProcessBuilder;
import org.qtjambi.maven.plugins.utils.shared.Utils;

/**
 * 
 * @goal compile
 * @author <a href="mailto:darryl.miles@darrylmiles.org">Darryl L. Miles</a>
 *
 */
public class QmakeMojo extends AbstractMojo {
	/**
	 * @parameter
	 */
	private String testString;

	/**
	 * @parameter
	 */
	private String qtDir;

	/**
	 * @parameter
	 */
	private Map<String,String> environmentVariables;

	/**
	 * @parameter default-value="${basedir}/src/main/native"
	 */
	private File sourceDirectory;

	/**
	 * @required
	 * @readonly
	 * @parameter default-value="${project.build.directory}/qmake"
	 */
	private File outputDirectory;

	/**
	 * @parameter default-value="false"
	 */
	private boolean debug;

	/**
	 * @parameter
	 */
	private Integer debugLevel;

	/**
	 * @parameter default-value="false"
	 */
	private boolean optimize;

	/**
	 * @parameter default-value="false"
	 */
	private boolean verbose;

	/**
	 * @parameter
	 */
	private String qmakeArguments;

	/**
	 * @parameter expression="${project.properties}"
	 */
	private Map<String,String> projectProperties;

	/**
	 * @parameter
	 */
	private Set<String> includes = new HashSet<String>();

	/**
	 * @parameter
	 */
	private Set<String> excludes = new HashSet<String>();

	/**
	 * Allow forcing of behavior based on a specific qmake version.
	 * @parameter
	 */
	private String qmakeVersion;
	// FIXME: Provide goal to test/check for mismatch.

	private boolean initDone;
	private Context context;
	private String execSuffix = "";
	private String K_bin_qmake;		// auto-filled by platform "bin/qmake"


	// Search sourceDirectory for *.pro files

	/**
	 * @execute phase="initialize"
	 */
	public void executeInitialize() throws MojoExecutionException, MojoFailureException {
		init();

		getLog().debug("testString=" + testString);
		getLog().debug("qtDir=" + qtDir);
		getLog().debug("environmentVariables=" + environmentVariables);
		getLog().debug("sourceDirectory=" + sourceDirectory);
		getLog().debug("outputDirectory=" + outputDirectory);
		getLog().debug("debug=" + debug);
		getLog().debug("debugLevel=" + debugLevel);
		getLog().debug("optimize=" + optimize);
		getLog().debug("verbose=" + verbose);
		getLog().debug("qmakeArguments=" + qmakeArguments);
		getLog().debug("projectProperties=" + projectProperties);
		getLog().debug("includes=" + includes);
		getLog().debug("excludes=" + excludes);

		getLog().info(QmakeMojo.class.getName() + " QMAKE");
		if(detectQmakeVersion() == false)
			throw new MojoFailureException("Unable to detect qmake version");
		getLog().info(QmakeMojo.class.getName() + " QMAKE_VERSION=" + qmakeVersion);

		DirectoryScanner ds = getDirectoryScanner();
		ds.scan();
		
	}

	/**
	 * @execute
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info(QmakeMojo.class.getCanonicalName() + ":execute()");

		{
			Platform platform = new Platform();
			platform.detect(getLog());
			Arguments arguments = new Arguments();
			arguments.detect(platform, getLog());
			context = new Context(platform, arguments, getLog());
		}
		if(debug && debugLevel == null)
			debugLevel = Integer.valueOf(1);

		DirectoryScanner ds = getDirectoryScanner();
		ds.scan();
		String[] fileA = ds.getIncludedFiles();

		if(getLog().isDebugEnabled()) {
			getLog().debug("includes[" + includes.size() + "] = " + Utils.debugStringArrayPretty(includes.toArray()));
			getLog().debug("excludes[" + excludes.size() + "] = " + Utils.debugStringArrayPretty(excludes.toArray()));
			getLog().debug("found[" + fileA.length + "] = " + Utils.debugStringArrayPretty(fileA));
		}

		//if(fileA.length == 1) {
		//	processOne(null, ds.getBasedir(), fileA[0]);
		//} else {	// subdir
			for(String f : fileA) {
				// sub/dir/foo.pro => sub__dir__foo.pro
				String newF = f.replace(File.separator, "__");
				processOne(newF, ds.getBasedir(), f);
			}
		//}
	}

	private boolean processOne(String buildDirectoryString, File sourceBaseDir, String qtMakeProFileString) throws MojoExecutionException, MojoFailureException {
		File buildDirectory;
		if(buildDirectoryString != null)
			buildDirectory = new File(outputDirectory, buildDirectoryString);
		else
			buildDirectory = outputDirectory;
		if(buildDirectory.exists() == false) {
			if(!buildDirectory.mkdirs())
				throw new MojoExecutionException("mkdirs() " + buildDirectory.toString() + " failed");
		}
		File qtMakeProFile = new File(sourceBaseDir, qtMakeProFileString);

		Project project = null;
		try {
			project = new Project(context);
			project.setSourceDir(sourceDirectory, false);
			project.setTargetDir(buildDirectory, false);
			if(debugLevel != null)
				project.setQmakeDebugLevel(debugLevel);
			if(!project.runQmake(new File[] { qtMakeProFile }))
				throw new MojoExecutionException("qmake execution failed");
			if(!project.runMake())
				throw new MojoExecutionException("make execution failed");
			return true;
		} catch(Exception e) {
			throw new MojoFailureException(e, e.getMessage(), e.getMessage());
		}
	}

	private String getProjectProperty(String key) {
		if(projectProperties == null)
			return null;
		Object o = projectProperties.get(key);
		if(o == null)
			return null;
		return o.toString();
	}

	/**
	 * Removes trailing (and excessive trailing) directory separators.
	 * @param path
	 * @return
	 */
	private String pathCanonTrailing(String path) {
		final String fileSeparator = File.separator;
		final int fileSeparatorLen = fileSeparator.length();
		while(true) {
			int len = path.length();
			if(len >= fileSeparatorLen) {
				String s = path.substring(len - fileSeparatorLen);
				if(s.compareTo(fileSeparator) == 0) {
					path = path.substring(0, len - fileSeparatorLen);
					continue;		// try again
				}
			}
			break;
		}
		return path;
	}

	protected DirectoryScanner getDirectoryScanner() {
		DirectoryScanner scanner = null;
		scanner = new DirectoryScanner();
		scanner.setFollowSymlinks(true);
		scanner.setBasedir(sourceDirectory);

		if(includes.isEmpty() && excludes.isEmpty()) {
			includes.add( "**/*.pro" );
			scanner.setIncludes(includes.toArray(new String[includes.size()]));
			return scanner;
		} else {
			if(includes.isEmpty()) {
				includes.add( "**/*.pro" );
			}
			scanner.setIncludes(includes.toArray(new String[includes.size()]));
			scanner.setExcludes(excludes.toArray(new String[excludes.size()]));
		}

		return scanner;
	}

	private void init() {
		if(initDone)
			return;

		if(File.separator.compareTo("\\") == 0)		// FIXME so proper windows detection
			execSuffix = ".exe";

		if(K_bin_qmake == null)
			K_bin_qmake = "bin" + File.separator + "qmake" + execSuffix;

		// Set from project.qtDir property
		String mavenQtdirString = getProjectProperty("project.qtdir");
		getLog().info("maven:project.qtdir=" + mavenQtdirString);
		// Set from $QTDIR environment variable
		String envvarQtdirString = System.getenv("QTDIR");
		getLog().info("envvar:QTDIR=" + envvarQtdirString);
		String syspropQtdirString = System.getProperty("QTDIR");
		getLog().info("sysprop:QTDIR=" + syspropQtdirString);
		// Check qmake exists on path, if so go with $PATH / system defaults
		String envvarPathString = System.getenv("PATH");
		String[] pathSplit = Utils.stringSplit(envvarPathString, File.pathSeparator);
		boolean first = true;
		for(String p : pathSplit) {
			if(first) {
				getLog().debug("PATH=" + p);
				first = false;
			} else {
				getLog().debug("    =" + p);
			}
				
		}

		if(qtDir == null) {
		} else {
			// Remove trailing directory separator
			qtDir = pathCanonTrailing(qtDir);
		}

		if(qtDir != null) {		// Check QTDIR exists (if set)
			File qtDirFile = new File(qtDir);
			if(qtDirFile.exists() == false) {
				getLog().warn("QTDIR=" + qtDir + ": does not exist");
			} else if(qtDirFile.isDirectory() == false) {
				getLog().warn("QTDIR=" + qtDir + ": is not a directory");
			}
		}

		// Check qmake exists to run, warn/error if not

		initDone = true;
	}

	private String resolveQtDirPath(String p) {
		StringBuffer sb = new StringBuffer();
		if(qtDir != null) {
			sb.append(qtDir);
			sb.append(File.separator);
		}
		sb.append(p);	// should not have a leading slash
		return sb.toString();
	}

	/**
	 * 
	 * @param line	The full one-line of text to process from "qmake -query" output
	 * @param propName	The name of the property we are looking for
	 * @return Returns null upon no match (or empty string match)
	 */
	private String qmakeQueryTryMatch(final String line, final String propName) {
		int idx;
		if((idx = line.indexOf(propName)) >= 0) {
			final int lineLen = line.length();
			StringBuilder sb = new StringBuilder();
			idx += propName.length();
			while(idx < lineLen) {
				char c = line.charAt(idx);
				// Allow other whitespace to exist in the line
				if(c == '\r' || c == '\n')
					break;
				sb.append(c);
				idx++;
			}
			if(sb.length() > 0)
				return sb.toString();
		}
		return null;
	}

	private boolean detectQmakeVersion() {
		boolean bf = false;

		// Run "qmake -query" parse output
		//  We know about "QMAKE_VERSION:2.01a"
		List<String> command = new ArrayList<String>();
		command.add(resolveQtDirPath(K_bin_qmake));
		command.add("-query");

		ProcessBuilder processBuilder = new ProcessBuilder(command);

		File directory = null;
//		for(Object k : getPluginContext().keySet()) {
//			String kS = k.toString();
//			Object v = getPluginContext().get(k);
//			String vS = (String) v.toString();
//			getLog().info(kS + " => " + vS);
//		}
		
		{
			String directoryString = getProjectProperty("project.basedir");
			if(directoryString != null) {
				directory = new File(directoryString);
				if(directory.exists() == false) {
					String msg = String.format("workingDirectory={0} does not exist", directoryString);
					getLog().warn(msg);
				} else {
					String msg = String.format("workingDirectory={0} does not exist", directoryString);
					getLog().info(msg);
				}
			}
		}
		if(directory != null)
			processBuilder.directory(directory);

		Map<String,String> env = processBuilder.environment();
		if(qtDir != null)
			env.put("QTDIR", qtDir);
		if(qtDir != null)
			env.put("QT_INSTALL_PREFIX", qtDir);

		String newQmakeVersion = null;

		OutputStream stdinStream = null;
		InputStream stdoutStream = null;
		InputStream stderrStream = null;
		Process process = null;
		Integer exitStatus = null;
		try {
			process = processBuilder.start();

			stdinStream = process.getOutputStream();
			stdinStream.close();
			stdinStream = null;

			stderrStream = process.getErrorStream();
			stderrStream.close();
			stderrStream = null;

			stdoutStream = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(stdoutStream);
			BufferedReader br = new BufferedReader(isr);
			String s;
			while((s = br.readLine()) != null) {
				final String K_QMAKE_VERSION_COLON = "QMAKE_VERSION:";

				String x;
				if((x = qmakeQueryTryMatch(s, K_QMAKE_VERSION_COLON)) != null)
					newQmakeVersion = x;


				getLog().info(s);
			}
			br.close();
			br = null;
			isr.close();
			isr = null;
			stdoutStream.close();
			stdoutStream = null;

			exitStatus = process.waitFor();
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

		if(exitStatus != null && exitStatus.intValue() == 0) {
			if(newQmakeVersion != null)
				qmakeVersion = newQmakeVersion;
			bf = true;
		}
		return bf;
	}
}
