package org.qtjambi.maven.plugins.qmake;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.DirectoryScanner;
import org.qtjambi.maven.plugins.utils.Context;
import org.qtjambi.maven.plugins.utils.Platform;
import org.qtjambi.maven.plugins.utils.Project;
import org.qtjambi.maven.plugins.utils.envvar.OpPathCorrectSeparator;
import org.qtjambi.maven.plugins.utils.envvar.OpPathRemoveMissing;
import org.qtjambi.maven.plugins.utils.envvar.OpSetIfUnset;
import org.qtjambi.maven.plugins.utils.internal.Arguments;
import org.qtjambi.maven.plugins.utils.shared.MojoExceptionHelper;
import org.qtjambi.maven.plugins.utils.shared.Utils;

/**
 * 
 * @goal compile
 * @Xexecute lifecycle="qmake-lifecycle" phase="compile"
 * @requiresProject
 * @author <a href="mailto:darryl.miles@darrylmiles.org">Darryl L. Miles</a>
 *
 */
public class QmakeCompileMojo extends AbstractQmakeMojo {
	/**
	 * FIXME: This should be set from projectProperties by scanning for qmake.envvar.NAME
	 * @parameter
	 */
	private Map<String,String> environmentVariables;

	/**
	 * @parameter expression="${qmake.sourceDirectory}" default-value="${basedir}/src/main/qmake"
	 */
	private File sourceDirectory;

	/**
	 * @required
	 * @parameter expression="${qmake.outputDirectory}" default-value="${project.build.directory}/qmake"
	 */
	private File outputDirectory;

	/**
	 * @parameter expression="${qmake.debug}" default-value="false"
	 */
	private boolean debug;

	/**
	 * @parameter expression="${qmake.debugLevel}"
	 */
	private Integer debugLevel;

	/**
	 * @parameter expression="${qmake.verbose}" default-value="false"
	 */
	private boolean verbose;

	/**
	 * @parameter expression="${qmake.recursive}" default-value="false"
	 */
	private Boolean qmakeRecursive;

	/**
	 * @parameter expression="${qmake.args}"
	 */
	private String[] qmakeArgs;

	/**
	 * "release" or "debug" or "all" might be good choices here.
	 * @parameter expression="${qmake.make.target}"
	 */
	private String qmakeMakeTarget;

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
	 * @parameter expression="${qmake.version}"
	 */
	private String qmakeVersion;
	// FIXME: Provide goal to test/check for mismatch.

	/**
	 * Valid values: jdk, jre, any (aka "true"), other values are treated as a path.
	 * @parameter expression="${qmake.envvar.export.JAVA_HOME}"
	 */
	private String exportJavaHome;

	/**
	 * This exists to allow for native cross-compile with Java JDK, i.e. the JDK/JRE we 
	 * are running Maven with does not need to be the same JDK we build our qmake
	 * applications with, this being the 'target'.
	 * Valid values: jdk, jre, any (aka "true"), other values are treated as a path.
	 * We might also want: quiet/noisy
	 * We might also want: validate path is jre/jdk.  Maybe: "jdk:" prefix?
	 * @parameter expression="${qmake.envvar.export.JAVA_HOME_TARGET}"
	 */
	private String exportJavaHomeTarget;

	/**
	 * This parameter will auto-correct PATH environment variables that have the
	 * wrong directory delimiter.  Some toolchains (such as MSVC) will simply
	 * error if those paths are using Unix slash delimiter when left as-is.
	 * @parameter expression="${qmake.envvar.fixupPath}" default-value="false"
	 */
	private boolean envvarFixupPath;


	private Context context;


	// Search sourceDirectory for *.pro files

	/**
	 * 
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().debug(QmakeCompileMojo.class.getCanonicalName() + ":execute()");

		{
			Platform platform = new Platform();
			platform.detect(getLog());
			Arguments arguments = new Arguments();
			arguments.detect(platform, getLog());
			boolean needSetupResolvers = false;
			if(environmentVariables != null) {
				for(Map.Entry entry : environmentVariables.entrySet()) {
					Object key = entry.getKey();
					String keyString = null;
					if(key != null)
						keyString = key.toString();

					Object value = entry.getValue();
					String valueString = null;
					if(value != null)
						valueString = value.toString();

					if(keyString != null) {
						if(keyString.equals("PATH"))
							arguments.getPathAppend().add(valueString);
						else
							arguments.getEnvvarGlobal().put(keyString, valueString);
						needSetupResolvers = true;
					}
				}
			}
			if(exportJavaHome != null) {
				String javaHome = arguments.resolveJavaHome(exportJavaHome);
				if(javaHome != null) {
					arguments.getEnvvarGlobal().put("JAVA_HOME", new OpSetIfUnset(javaHome));
					needSetupResolvers = true;
				}
			}
			if(exportJavaHomeTarget != null) {
				String javaHomeTarget = arguments.resolveJavaHomeTarget(exportJavaHomeTarget);
				if(javaHomeTarget != null) {
					arguments.getEnvvarGlobal().put("JAVA_HOME_TARGET", new OpSetIfUnset(javaHomeTarget));
					needSetupResolvers = true;
				}
			}
			if(envvarFixupPath) {
				arguments.getEnvvarGlobal().put("PATH", new OpPathCorrectSeparator());
				needSetupResolvers = true;
			}
			arguments.getEnvvarGlobal().put("PATH", new OpPathRemoveMissing());
			needSetupResolvers = true;
			if(needSetupResolvers)
				arguments.setupResolvers(platform);
			context = new Context(platform, arguments, getLog());
		}
		if(debug && debugLevel == null)
			debugLevel = Integer.valueOf(1);

		// FIXME provide mechanism to just let qmake do recursive
		DirectoryScanner ds = getDirectoryScanner();
		ds.scan();
		String[] fileA = ds.getIncludedFiles();

		if(getLog().isDebugEnabled()) {
			getLog().debug("includes[" + includes.size() + "] = " + Utils.debugStringArrayPretty(includes.toArray()));
			getLog().debug("excludes[" + excludes.size() + "] = " + Utils.debugStringArrayPretty(excludes.toArray()));
			getLog().debug("found[" + fileA.length + "] = " + Utils.debugStringArrayPretty(fileA));
			if(environmentVariables != null && !environmentVariables.isEmpty())
				getLog().debug("environmentVariables[" + environmentVariables.size() + "] = " + Utils.debugStringMapPretty(environmentVariables));
		}

		//if(fileA.length == 1) {
		//	processOne(null, ds.getBasedir(), fileA[0]);
		//} else {	// subdir
			for(String f : fileA) {
				// We want to shuffle up one
				String newF = Utils.stringReplaceShuffleChar(f, '/', '_', 1);
				// String newF = f.replace(File.separator, "__");	// coarse-equivalent
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
			getLog().debug("created directory: " + buildDirectory.getAbsolutePath());
		}
		File qtMakeProFile = new File(sourceBaseDir, qtMakeProFileString);

		Project project = null;
		try {
			project = new Project(context);
			
			project.setSourceDir(sourceDirectory, false);
			project.setTargetDir(buildDirectory, false);
			if(debugLevel != null)
				project.setQmakeDebugLevel(debugLevel);
			if(qmakeRecursive != null)
				project.setQmakeRecursive(qmakeRecursive);
			if(!project.runQmake(qmakeArgs, new File[] { qtMakeProFile }))
				throw new MojoExecutionException("qmake execution failed");
			String[] makeArguments = null;
			if(qmakeMakeTarget != null)
				makeArguments = new String[] { qmakeMakeTarget };
			if(!project.runMake(makeArguments))
				throw new MojoExecutionException("make execution failed");
			return true;
		} catch(Exception e) {
			throw new MojoExceptionHelper<MojoFailureException>().rethrow(e);
		}
	}

	protected DirectoryScanner getDirectoryScanner() {
		DirectoryScanner scanner = null;
		scanner = new DirectoryScanner();
		scanner.setFollowSymlinks(true);
		scanner.setBasedir(sourceDirectory);

		if(includes.isEmpty() && excludes.isEmpty()) {
			includes.add( "**/*.pro" );
			scanner.setIncludes(includes.toArray(new String[includes.size()]));
		} else {
			if(includes.isEmpty())
				includes.add( "**/*.pro" );
			scanner.setIncludes(includes.toArray(new String[includes.size()]));
			scanner.setExcludes(excludes.toArray(new String[excludes.size()]));
		}

		return scanner;
	}
}
