package org.qtjambi.maven.plugins.generator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.qtjambi.maven.plugins.jxe.JxeExecParam;
import org.qtjambi.maven.plugins.jxe.JxeUtil;
import org.qtjambi.maven.plugins.utils.shared.Utils;

/**
 * @goal generateSources
 * @author Darryl
 *
 */
public class GenerateSourcesMojo extends AbstractMojo {
	/**
	 * @parameter expression="${generator.skip}" default-value="false"
	 */
	private boolean skip;

	/**
	 * @parameter expression="${generator.masterheaderinclude}" default-value="${basedir}/src/main/generator/masterinclude.h"
	 */
	private File masterHeaderInclude;
	/**
	 * @parameter expression="${generator.typesystemxml}" default-value="${basedir}/src/main/generator/all.xml"
	 */
	private File typeSystemXml;
	/**
	 * @parameter default-value="${basedir}/src/main/generator"
	 */
	private File inputDirectory;
	/**
	 * @parameter default-value="${project.build.directory}/generated-sources/qtjambi.generator/cpp"
	 */
	private File cppOutputDirectory;
	/**
	 * @parameter default-value="${project.build.directory}/generated-sources/qtjambi.generator/java"
	 */
	private File javaOutputDirectory;
	/**
	 * @parameter default-value="${project.build.directory}/generator"
	 */
	private File outputDirectory;
	/**
	 * @parameter expression="${qt.include.directory}" default-value="${qtsdk.home}/include"
	 */
	private File qtIncludeDirectory;
	/**
	 * @parameter expression="${kde.phonon.directory}"
	 */
	private File kdePhononDirectory;
	/**
	 * @parameter
	 */
	private String[] includePaths;
	/**
	 * @parameter expression="${generator.args}"
	 */
	private String[] generatorArguments;
	/**
	 * @parameter expression="${generator.cwd}"
	 */
	private File generatorCurrentWorkingDirectory;

	/**
	 * This parameter will enable the appending of JAVA_HOME_TARGET (or JAVA_HOME)
	 * include directory and the platform specific directory to the paths.
	 * @parameter expression="${generator.includePaths.appendJavaInclude}" default-value="auto"
	 */
	private String includePathsAppendJavaInclude;

	/**
	 * This parameter will enable the appending of compiler related
	 * include directory.
	 * @parameter expression="${generator.includePaths.appendCompilerInclude}" default-value="auto"
	 */
	private String includePathsAppendCompilerInclude;

	/**
	 * @parameter expression="${generator.java.home}"
	 */
	private String paramJavaHome;

	/**
	 * @parameter expression="${generator.java.home.target}"
	 */
	private String paramJavaHomeTarget;

	/**
	 * @parameter expression="${generator.java.osarch.target}"
	 */
	private String paramJavaOsarchTarget;

	/**
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	private MavenProject project;

	/**
	 * @parameter expression="${plugin.artifacts}"
	 * @required
	 * @readonly
	 */
	private List<Artifact> pluginArtifacts;

	/**
	 * @parameter expression="${plugin}"
	 * @required
	 * @readonly
	 */
	private PluginDescriptor plugin;

	/**
	 * 
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		// Check output dir exists?  Create, check it is empty, clean it
		// Process XML xmlmerge (done in ANT for now)
		// Process XML 
		// Generate
		getLog().info(GenerateSourcesMojo.class.getName() + " START");
		getLog().debug("sysprop(\"user.dir\")=" + System.getProperty("user.dir"));
		getLog().debug("typeSystemXml=" + typeSystemXml);
		getLog().debug("masterHeaderInclude=" + masterHeaderInclude);
		getLog().debug("inputDirectory=" + inputDirectory);
		getLog().debug("cppOutputDirectory=" + cppOutputDirectory);
		getLog().debug("javaOutputDirectory=" + javaOutputDirectory);
		getLog().debug("outputDirectory=" + outputDirectory);
		getLog().debug("qtIncludeDirectory=" + qtIncludeDirectory);
		getLog().debug("kdePhononDirectory=" + kdePhononDirectory);
		getLog().debug("generatorArguments=" + Utils.debugStringArrayPretty(generatorArguments));
		getLog().debug("includePaths=" + Utils.debugStringArrayPretty(includePaths));
		getLog().debug("generatorCurrentWorkingDirectory=" + generatorCurrentWorkingDirectory);
		getLog().debug("includePathsAppendJavaInclude=" + includePathsAppendJavaInclude);
		getLog().debug("includePathsAppendCompilerInclude=" + includePathsAppendCompilerInclude);
		getLog().debug("paramJavaHome=" + paramJavaHome);
		getLog().debug("paramJavaHomeTarget=" + paramJavaHomeTarget);
		getLog().debug("paramJavaOsarchTarget=" + paramJavaOsarchTarget);

		if(skip) {
			getLog().info("skipping generator; generator.skip=" + skip);
			return;
		}

		if(masterHeaderInclude == null || masterHeaderInclude.exists() == false) {
			throw new MojoFailureException("generator.masterheaderinclude does not exist: " + masterHeaderInclude);
		}
		if(typeSystemXml == null || typeSystemXml.exists() == false) {
			throw new MojoFailureException("generator.typesystemxml does not exist: " + typeSystemXml);
		}

		File newCompileSourceRoot = null;
		if(!outputDirectory.exists()) {
			outputDirectory.mkdirs();
		}
		newCompileSourceRoot = new File(outputDirectory, "java");

		if(cppOutputDirectory != null && !cppOutputDirectory.exists()) {
			cppOutputDirectory.mkdirs();
		}
		if(javaOutputDirectory != null && !javaOutputDirectory.exists()) {
			javaOutputDirectory.mkdirs();
			newCompileSourceRoot = javaOutputDirectory;
		}

		if(project != null && newCompileSourceRoot != null) {
			project.addCompileSourceRoot(newCompileSourceRoot.getAbsolutePath());
		}

		// generate
		// run: *-jxe.jar

		// Find the downloaded local copy of the *-jxe.jar
		List<Artifact> jxeFoundList = new ArrayList<Artifact>();
		for(Artifact artifact : pluginArtifacts) {
			// Search for generator-maven-plugin
			//getLog().debug(artifact.getId() + " " + artifact.getFile());
			String classifier = artifact.getClassifier();
			if(classifier != null && classifier.compareToIgnoreCase("jxe") == 0) {
				jxeFoundList.add(artifact);
				//getLog().debug(" " + artifact.getClassifier() + " " + artifact.getFile());
			}
		}
		if(jxeFoundList.size() == 0) {
			throw new MojoFailureException("Unable to locate generator-jxe.jar");
		} else if(jxeFoundList.size() > 1) {
			for(Artifact a : jxeFoundList) {
				getLog().error(" jxe found at " + a.toString());
			}
			throw new MojoFailureException("Found more than one classifier=jxe when searching for generator-jxe.jar");
		}
		File jxeExecutable = null;
		Artifact jxeFound = jxeFoundList.get(0);
		if(jxeFound != null) {
			getLog().debug(jxeFound.toString() + " " + jxeFound.getFile());
			jxeExecutable = jxeFound.getFile();
		}

		List<String> argList = new ArrayList<String>();

		if(kdePhononDirectory != null)
			argList.add("--phonon-include=" + kdePhononDirectory.getAbsolutePath());
		if(qtIncludeDirectory != null)
			argList.add("--qt-include-directory=" + qtIncludeDirectory.getAbsolutePath());
		if(inputDirectory != null)
			argList.add("--input-directory=" + inputDirectory.getAbsolutePath());
		if(outputDirectory != null)
			argList.add("--output-directory=" + outputDirectory.getAbsolutePath());
		if(cppOutputDirectory != null)
			argList.add("--cpp-output-directory=" + cppOutputDirectory.getAbsolutePath());
		if(javaOutputDirectory != null)
			argList.add("--java-output-directory=" + javaOutputDirectory.getAbsolutePath());

		//List<String> includePathList  = new ArrayList<String>();
		//String[] includePathA = includePathList.toArray(new String[includePathList.size()]);
		String includePath = null;
		String[] includePathsA = includePaths;
		if(includePaths != null && includePaths.length > 0) {
			if(includePathsAppendJavaInclude != null) {	// FIXME quiet/verbose/disable/auto
				boolean verbose = true;
				boolean allAll = true;

				String javaHomeTarget = paramJavaHomeTarget;		// FIXME get it from Initialize
				if(javaHomeTarget == null)
					javaHomeTarget = paramJavaHome;

				File includeDir = new File(javaHomeTarget, "include");
				if(includeDir.exists() && includeDir.isDirectory()) {
					// Add $JAVA_HOME/include
					String path = includeDir.getAbsolutePath();
					if(verbose)
						getLog().info("Adding includePath entry: " + path);
					if(Utils.stringArrayContains(includePathsA, path) == false)
						includePathsA = Utils.safeStringArrayAppend(includePathsA, path); 
				}

				String javaOsarchTarget = "win32";		// FIXME get it from Initialize
				if(javaOsarchTarget != null) {
					File includeOsArchDir = new File(includeDir, javaOsarchTarget);
					if(includeOsArchDir.exists()) {
						String path = includeOsArchDir.getAbsolutePath();
						if(verbose)
							getLog().info("Adding includePath entry: " + path);
						if(Utils.stringArrayContains(includePathsA, path) == false)
							includePathsA = Utils.safeStringArrayAppend(includePathsA, path);
					}
				}

				List<File> foundList = new ArrayList<File>();
				int foundCount = 0;
				if(includeDir.exists()) {
					File[] listFiles = includeDir.listFiles();
					for(File f : listFiles) {
						if(f.exists() && f.isDirectory()) {
							foundCount++;
							foundList.add(f);
						}
					}
				}
				if(allAll) {
					for(File f : foundList) {
						// Detect subdir
						// Add $JAVA_HOME/include/win32
						String path = f.getAbsolutePath();
						if(verbose)
							getLog().info("Adding includePath entry: " + path);
						if(Utils.stringArrayContains(includePathsA, path) == false)
							includePathsA = Utils.safeStringArrayAppend(includePathsA, path);
					}
				}
			}
			if(includePathsAppendCompilerInclude != null) {	// FIXME
				String s = "";
				getLog().info("Adding includePath entry: " + s);
			}
			includePath = Utils.stringConcat(includePathsA, File.pathSeparator);
		}
		if(includePath != null)
			argList.add("--include-paths=" + includePath);

		if(generatorArguments != null) {
			for(String s : generatorArguments) {
				argList.add(s);
			}
		}

		// Mandatory arguments in this order
		argList.add(masterHeaderInclude.getAbsolutePath());
		argList.add(typeSystemXml.getAbsolutePath());

		String[] args = argList.toArray(new String[argList.size()]);

		getLog().debug("jxeExecutable="+jxeExecutable);
		int i = 0;
		for(String s : args) {
			getLog().debug("arg[" + (i++) + "]=" + s);
		}

		JxeExecParam jxeExecParam = new JxeExecParam();
		jxeExecParam.setCurrentWorkingDirectory(generatorCurrentWorkingDirectory);

		Integer jxeExitStatus = JxeUtil.exec(jxeExecutable, args, jxeExecParam);

		if(jxeExitStatus == null || jxeExitStatus != 0) {
			getLog().info(jxeExecutable + " exitStatus=" + jxeExitStatus);
			throw new MojoFailureException("non-zero [" + jxeExitStatus + "] exit status from " + jxeExecutable);
		} else {
			getLog().debug(jxeExecutable + " exitStatus=" + jxeExitStatus);
		}

		getLog().info(GenerateSourcesMojo.class.getName() + " FINISH");
	}
}
