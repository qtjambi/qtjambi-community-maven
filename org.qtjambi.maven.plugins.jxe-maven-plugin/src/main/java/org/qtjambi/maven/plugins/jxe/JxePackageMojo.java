package org.qtjambi.maven.plugins.jxe;

import java.io.File;
import java.io.IOException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.util.DirectoryScanner;
//import org.codehaus.plexus.archiver.manager.ArchiverManager;

/**
 * 
 * 
 * @goal jxe-package
 * @phase package
 * @requiresProject
 * @author <a href="mailto:darryl.miles@darrylmiles.org">Darryl L. Miles</a>
 *
 */
public class JxePackageMojo extends AbstractJxeMojo {
	/**
	 * To look up Archiver/UnArchiver implementations
	 * 
	 * @Xcomponent role="org.codehaus.plexus.archiver.manager.ArchiverManager"
	 * @Xrequired
	 */
	//private ArchiverManager archiverManager;

	/**
	 * Used for attaching the artifact in the project
	 * 
	 * @component
	 */
	private MavenProjectHelper projectHelper;

	public final void jxeExecute() throws MojoExecutionException, MojoFailureException {
		// let the layout decide which nars to attach
		//getLayout().attachJxes(getTargetDirectory(), archiverManager, projectHelper, getMavenProject(), getJxeInfo());

		DirectoryScanner ds = getDirectoryScanner();
		ds.scan();
		String[] fileA = ds.getIncludedFiles();

		try {
			File propertiesDir = new File(getOutputDirectory(), "classes/META-INF/jxe/" +
					getMavenProject().getGroupId() + "/" + getMavenProject().getArtifactId());
			if(!propertiesDir.exists())
				propertiesDir.mkdirs();
			File propertiesFile = new File(propertiesDir, JxeInfo.JXE_PROPERTIES);
			getJxeInfo().writeToFile(propertiesFile);
		} catch(IOException ioe) {
			throw new MojoExecutionException("Cannot write jxe properties file", ioe);
		}
	}

	protected DirectoryScanner getDirectoryScanner() {
		DirectoryScanner scanner = null;
		scanner = new DirectoryScanner();
		scanner.setFollowSymlinks(true);
		scanner.setBasedir(getOutputDirectory());

		//if(includes.isEmpty() && excludes.isEmpty()) {
		//	includes.add( "**/*.pro" );
		//	scanner.setIncludes(includes.toArray(new String[includes.size()]));
		//} else {
		//	if(includes.isEmpty())
		//		includes.add( "**/*.pro" );
		//	scanner.setIncludes(includes.toArray(new String[includes.size()]));
		//	scanner.setExcludes(excludes.toArray(new String[excludes.size()]));
		//}

		return scanner;
	}
}
