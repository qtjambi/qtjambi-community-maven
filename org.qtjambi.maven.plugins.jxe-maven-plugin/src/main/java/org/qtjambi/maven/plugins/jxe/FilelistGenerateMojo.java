package org.qtjambi.maven.plugins.jxe;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * 
 * @goal jxe-filelist-generate
 * @phase package
 * @requiresProject
 * @author <a href="mailto:darryl.miles@darrylmiles.org">Darryl L. Miles</a>
 *
 */
public class FilelistGenerateMojo extends AbstractJxeMojo {

	/**
	 * 
	 */
	public void jxeExecute() throws MojoExecutionException, MojoFailureException {
		getLog().info(FilelistGenerateMojo.class.getCanonicalName() + ":execute()");
	}
}
