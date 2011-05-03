package org.qtjambi.maven.plugins.qmake;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * 
 * @goal testCompile
 * @requiresProject
 * @execute lifecycle="qmake-lifecycle" phase="compile"
 * @author <a href="mailto:darryl.miles@darrylmiles.org">Darryl L. Miles</a>
 *
 */
public class TestCompileMojo extends AbstractMojo {
	/**
	 * @parameter default-value="${basedir}/src/test/qmake"
	 */
	private File sourceDirectory;

	/**
	 * 
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info(TestCompileMojo.class.getCanonicalName() + ":execute()");
		if(sourceDirectory.exists())
			throw new MojoFailureException("NOT IMPLEMENTED: Remove directory " + sourceDirectory.getAbsolutePath());
	}
}
