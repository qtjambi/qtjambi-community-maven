package org.qtjambi.maven.plugins.utils;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * @phase generate-sources
 * @author Darryl
 *
 */
public class GeneratorMojo extends AbstractMojo {
	/**
	 * @parameter default-value="${sourceDirectory}/generator/all.xml"
	 */
	private String typeSystemXml;
	/**
	 * @parameter default-value="${sourceDirectory}/generator/masterinclude.h"
	 */
	private String masterHeaderInclude;
	/**
	 * @parameter default-value="${outputDirectory}/generated-sources/${pluginId}/generator"
	 */
	private String outputDirectory;
	/**
	 * @parameter
	 */
	private String qtIncludeDirectory;
	/**
	 * @parameter
	 */
	private String qtLibDirectory;
	/**
	 * @parameter
	 */
	private String kdePhononDirectory;
	/**
	 * @parameter
	 */
	private String generatorArguments;

	public void execute() throws MojoExecutionException, MojoFailureException {
		// Check output dir exists?  Create, check it is empty, clean it
		// Process XML xmlmerge
		// Process XML 
		// Generate
		getLog().debug("typeSystemXml=" + typeSystemXml);
		getLog().debug("masterHeaderInclude=" + masterHeaderInclude);
		getLog().debug("outputDirectory=" + outputDirectory);
		getLog().debug("qtIncludeDirectory=" + qtIncludeDirectory);
		getLog().debug("qtLibDirectory=" + qtLibDirectory);
		getLog().debug("kdePhononDirector=" + kdePhononDirectory);
		getLog().debug("generatorArguments=" + generatorArguments);
	}

}
