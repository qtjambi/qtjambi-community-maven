package org.qtjambi.maven.plugins.qmake;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * 
 * @goal generateSources
 * @requiresProject
 * @Xexecute lifecycle="qmake-lifecycle" phase="generate-sources"
 * @author <a href="mailto:darryl.miles@darrylmiles.org">Darryl L. Miles</a>
 *
 */
public class QmakeGenerateSourcesMojo extends AbstractMojo {
	/**
	 * 
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info(QmakeGenerateSourcesMojo.class.getCanonicalName() + ":execute()");
		// FIXME: Run qmake
	}
}
