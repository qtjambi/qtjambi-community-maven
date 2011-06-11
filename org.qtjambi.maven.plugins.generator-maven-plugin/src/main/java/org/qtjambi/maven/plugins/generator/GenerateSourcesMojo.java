package org.qtjambi.maven.plugins.generator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.qtjambi.maven.plugins.jxe.JxeUtil;
import org.qtjambi.maven.plugins.utils.shared.Utils;

/**
 * @goal generateSources
 * @author Darryl
 *
 */
public class GenerateSourcesMojo extends AbstractMojo {
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
	 * @parameter expression="${qt.lib.directory}" default-value="${qtsdk.home}/lib"
	 */
	private File qtLibDirectory;
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
		getLog().debug("typeSystemXml=" + typeSystemXml);
		getLog().debug("masterHeaderInclude=" + masterHeaderInclude);
		getLog().debug("inputDirectory=" + inputDirectory);
		getLog().debug("cppOutputDirectory=" + cppOutputDirectory);
		getLog().debug("javaOutputDirectory=" + javaOutputDirectory);
		getLog().debug("outputDirectory=" + outputDirectory);
		getLog().debug("qtIncludeDirectory=" + qtIncludeDirectory);
		getLog().debug("qtLibDirectory=" + qtLibDirectory);
		getLog().debug("kdePhononDirector=" + kdePhononDirectory);
		getLog().debug("generatorArguments=" + Utils.debugStringArrayPretty(generatorArguments));

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
		Artifact jxeFound = null;
		int foundCount = 0;
		for(Artifact artifact : pluginArtifacts) {
			// Search for generator-maven-plugin
			//getLog().debug(artifact.getId() + " " + artifact.getFile());
			String classifier = artifact.getClassifier();
			if(classifier != null && classifier.compareToIgnoreCase("jxe") == 0) {
				foundCount++;
				if(foundCount == 1)
					jxeFound = artifact;
				else
					jxeFound = null;
				//getLog().debug(" " + artifact.getClassifier() + " " + artifact.getFile());
			}
		}
		if(foundCount == 0) {
			throw new MojoFailureException("Unable to locate generator-jxe.jar");
		} else if(foundCount > 1) {
			throw new MojoFailureException("Found more than one classifier=jxe when searching for generator-jxe.jar");
		}
		File jxeExecutable = null;
		if(jxeFound != null) {
			getLog().debug(jxeFound.toString() + " " + jxeFound.getFile());
			jxeExecutable = jxeFound.getFile();
		}

		List<String> argList = new ArrayList<String>();

		if(kdePhononDirectory != null)
			argList.add("--phonon-include=" + kdePhononDirectory.getAbsolutePath());
		if(qtIncludeDirectory != null)
			argList.add("--qt-include-directory=" + qtIncludeDirectory.getAbsolutePath());
		if(qtLibDirectory != null)
			argList.add("--qt-lib-directory=" + qtLibDirectory.getAbsolutePath());
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
		if(includePaths != null && includePaths.length > 0)
			includePath = Utils.stringConcat(includePaths, File.pathSeparator);
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
		getLog().debug("args"+args.toString());
		Integer jxeExitStatus = JxeUtil.exec(jxeExecutable, args);

		if(jxeExitStatus == null || jxeExitStatus != 0) {
			getLog().info(jxeExecutable + " exitStatus=" + jxeExitStatus);
			throw new MojoFailureException("non-zero [" + jxeExitStatus + "] exit status from " + jxeExecutable);
		} else {
			getLog().debug(jxeExecutable + " exitStatus=" + jxeExitStatus);
		}

		getLog().info(GenerateSourcesMojo.class.getName() + " FINISH");
	}
}
