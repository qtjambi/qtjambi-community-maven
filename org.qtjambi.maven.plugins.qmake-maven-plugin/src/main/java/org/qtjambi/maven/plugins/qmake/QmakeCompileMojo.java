package org.qtjambi.maven.plugins.qmake;

import java.io.File;
import java.util.HashSet;
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
import org.qtjambi.maven.plugins.utils.shared.MojoExceptionHelper;
import org.qtjambi.maven.plugins.utils.shared.Utils;

/**
 * 
 * @goal compile
 * @execute lifecycle="qmake-lifecycle" phase="compile"
 * @requiresProject
 * @author <a href="mailto:darryl.miles@darrylmiles.org">Darryl L. Miles</a>
 *
 */
public class QmakeCompileMojo extends AbstractMojo {
	/**
	 * @parameter expression=${qtsdk.home}
	 */
	private String qtDir;

	/**
	 * @parameter
	 */
	private Map<String,String> environmentVariables;

	/**
	 * @parameter default-value="${basedir}/src/main/qmake"
	 */
	private File sourceDirectory;

	/**
	 * @required
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

	private Context context;


	// Search sourceDirectory for *.pro files

	/**
	 * 
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info(QmakeCompileMojo.class.getCanonicalName() + ":execute()");

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
			if(!project.runQmake(new File[] { qtMakeProFile }))
				throw new MojoExecutionException("qmake execution failed");
			if(!project.runMake())
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
