package org.qtjambi.maven.plugins.qmake;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * 
 * @goal testCompile
 * @requiresProject
 * @Xexecute lifecycle="qmake-lifecycle" phase="test-compile"
 * @author <a href="mailto:darryl.miles@darrylmiles.org">Darryl L. Miles</a>
 *
 */
public class QmakeTestCompileMojo extends AbstractQmakeMojo {
	/**
	 * @parameter default-value="${basedir}/src/test/qmake"
	 */
	private File testSourceDirectory;

	/**
	 * @required
	 * @parameter expression="${qmake.testOutputDirectory}" default-value="${project.build.directory}/qmake-test"
	 */
	private File testOutputDirectory;

	/**
	 * 
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info(QmakeTestCompileMojo.class.getCanonicalName() + ":execute()");
		if(testSourceDirectory.exists())
			throw new MojoFailureException("NOT IMPLEMENTED: Remove directory " + testSourceDirectory.getAbsolutePath());
	}
}
