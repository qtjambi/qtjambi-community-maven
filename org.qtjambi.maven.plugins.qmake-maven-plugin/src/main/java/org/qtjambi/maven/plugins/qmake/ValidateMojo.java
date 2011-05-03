package org.qtjambi.maven.plugins.qmake;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * 
 * @goal validate
 * @requiresProject
 * @execute lifecycle="qmake-lifecycle" phase="validate"
 * @author <a href="mailto:darryl.miles@darrylmiles.org">Darryl L. Miles</a>
 *
 */
public class ValidateMojo extends AbstractMojo {
	/**
	 * 
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info(ValidateMojo.class.getCanonicalName() + ":execute()");
	}
}
