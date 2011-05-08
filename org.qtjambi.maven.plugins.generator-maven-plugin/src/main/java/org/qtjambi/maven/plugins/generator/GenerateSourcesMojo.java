package org.qtjambi.maven.plugins.generator;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.qtjambi.maven.plugins.utils.shared.Utils;

/**
 * @goal generateSources
 * @author Darryl
 *
 */
public class GenerateSourcesMojo extends AbstractMojo {
	/**
	 * @parameter expression="${generator.typesystemxml}" default-value="${basedir}/src/main/generator/all.xml"
	 */
	private String typeSystemXml;
	/**
	 * @parameter expression="${generator.masterheaderinclude}" default-value="${basedir}/src/main/generator/masterinclude.h"
	 */
	private String masterHeaderInclude;
	/**
	 * @parameter default-value="${project.build.directory}/generated-sources/qtjambi.generator/generator"
	 */
	private String outputDirectory;
	/**
	 * @parameter expression="${qt.include.directory}" default-value="${qtsdk.home}/include"
	 */
	private String qtIncludeDirectory;
	/**
	 * @parameter expression="${qt.lib.directory}" default-value="${qtsdk.home}/lib"
	 */
	private String qtLibDirectory;
	/**
	 * @parameter expression="${kde.phonon.directory}"
	 */
	private String kdePhononDirectory;
	/**
	 * @parameter expression="${generator.args}"
	 */
	private String[] generatorArguments;

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
		getLog().debug("outputDirectory=" + outputDirectory);
		getLog().debug("qtIncludeDirectory=" + qtIncludeDirectory);
		getLog().debug("qtLibDirectory=" + qtLibDirectory);
		getLog().debug("kdePhononDirector=" + kdePhononDirectory);
		getLog().debug("generatorArguments=" + Utils.debugStringArrayPretty(generatorArguments));
		getLog().info(GenerateSourcesMojo.class.getName() + " FINISH");
	}

}
