package org.qtjambi.maven.plugins.jxe;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.qtjambi.maven.plugins.jxe.datum.FilelistAssembly;

/**
 * 
 * @author <a href="mailto:darryl.miles@darrylmiles.org">Darryl L. Miles</a>
 *
 */
public abstract class AbstractJxeMojo extends AbstractMojo {
	/**
	 * Skip running anything.
	 * 
	 * @parameter expression="${jxe.skip}" default-value="false"
	 */
	private boolean skip;

	/**
	 * @required
	 * @readonly
	 * @parameter expression="${jxe.outputDirectory}" default-value="${project.build.directory}"
	 */
	private File outputDirectory;

	/**
	 * @parameter expression="${project}"
	 * @readonly
	 * @required
	 */
	private MavenProject mavenProject;

	/**
	 * @parameter expression=""
	 */
	private FilelistAssembly filelistAssembly;

	/**
	 * @parameter expression="${jxe.outputPropertiesFile}" default-value="${project.build.directory}/classes/filelist.properties"
	 */
	private File outputPropertiesFile;

	/**
	 * @parameter expression="${jxe.ensureExecutable}" default-value="true"
	 */
	private boolean ensureExecutable;

	/**
	 * @parameter expression="${jxe.autoDetectExecutable}" default-value="true"
	 */
	private boolean autoDetectExecutable;

	/**
	 * 
	 */
	private JxeInfo jxeInfo;

	public File getOutputDirectory() {
		return this.outputDirectory;
	}
	public MavenProject getMavenProject() {
		return this.mavenProject;
	}
	public JxeInfo getJxeInfo() throws MojoExecutionException {
		if(jxeInfo == null) {
			jxeInfo = new JxeInfo(mavenProject.getGroupId(), mavenProject.getArtifactId(), mavenProject.getVersion(), getLog());
		}
		return jxeInfo;
	}
	public FilelistAssembly getFilelistAssembly() {
		return this.filelistAssembly;
	}
	public File getOutputPropertiesFile() {
		return this.outputPropertiesFile;
	}
	public boolean isEnsureExecutable() {
		return ensureExecutable;
	}
	public boolean isAutoDetectExecutable() {
		return autoDetectExecutable;
	}

	public final void execute() throws MojoExecutionException, MojoFailureException {
		if(skip) {
			getLog().info(getClass().getName() + " skipped" );
			return;
		}

		try {
			jxeExecute();
		} catch(MojoExecutionException mee) {
			throw mee;
		} catch(MojoFailureException mfe) {
			throw mfe;
		}
	}

	public abstract void jxeExecute() throws MojoFailureException, MojoExecutionException;
}
