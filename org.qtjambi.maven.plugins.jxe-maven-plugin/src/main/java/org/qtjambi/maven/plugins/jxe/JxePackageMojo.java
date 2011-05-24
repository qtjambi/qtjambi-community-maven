package org.qtjambi.maven.plugins.jxe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.util.DirectoryScanner;
import org.qtjambi.maven.plugins.jxe.datum.FilelistAssembly;
import org.qtjambi.maven.plugins.jxe.datum.IFileSet;
//import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.qtjambi.maven.plugins.utils.shared.Utils;

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

	public static final String K_prefix_directory	= "directory";
	public static final String K_prefix_file 		= "file";
	public static final String K_prefix_executable	= "executable";
	public static final String STRING_empty			= "";

	public final void jxeExecute() throws MojoExecutionException, MojoFailureException {
		// let the layout decide which nars to attach
		//getLayout().attachJxes(getTargetDirectory(), archiverManager, projectHelper, getMavenProject(), getJxeInfo());

		emitFilelistProperties();

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

	private void emitFilelistProperties() throws MojoExecutionException {
		Properties filelistProps = new Properties();
		Set<String> filelistSet = new HashSet<String>();
		Set<String> executableSet = new HashSet<String>();

		FilelistAssembly fa = getFilelistAssembly();
		String[] fileA = new String[0];
		String[] execA = new String[0];

		File baseDirectory = fa.getDirectory();
		if(baseDirectory.exists() == false) {
			getLog().warn("directory " + baseDirectory.getAbsolutePath() + " does not exit, jxe filelist package skipped");
			return;
		}

		if(fa != null) {
			DirectoryScanner ds = getDirectoryScanner(fa.getDirectory(), fa);
			ds.scan();
			fileA = ds.getIncludedFiles();

			ds = getDirectoryScanner(fa.getDirectory(), fa.getTargetExecutable());
			ds.scan();
			execA = ds.getIncludedFiles();
		}

		resolveFilelistProperties(filelistProps, execA, true, filelistSet, executableSet);
		resolveFilelistProperties(filelistProps, fileA, false, filelistSet, executableSet);

		try {
			File propertiesDir = getOutputPropertiesFile().getParentFile();
			if(!propertiesDir.exists())
				propertiesDir.mkdirs();
			File propertiesFile = getOutputPropertiesFile();

			filelistProps.store(new FileOutputStream(propertiesFile), "");
		} catch(IOException ioe) {
			throw new MojoExecutionException("Cannot write filelist properties file", ioe);
		}
	}

	private void resolveFilelistProperties(Properties filelistProps, String[] fileA, boolean execPass, Set<String> filelistSet, Set<String> executableSet) {
		for(String fileString : fileA) {
			String fileKeyString = fileString.replace('\\', '/');

			File file = new File(getFilelistAssembly().getDirectory(), fileString);
			if(file.exists()) {
				if(file.isDirectory()) {
					filelistProps.put(K_prefix_directory + "." + fileKeyString + "/", STRING_empty);
					filelistSet.add(fileKeyString);
				} else if(file.isFile()) {
					if(executableSet.contains(fileKeyString))
						continue;		// already seen
					if(filelistSet.contains(fileKeyString)) {
						if(execPass)	// upgrade this to executable
							filelistProps.remove(fileKeyString);
						else
							continue;	// already seen
					}
					if(isAutoDetectExecutable() && checkAutoDetectExecutable(file)) {
						filelistProps.put(K_prefix_executable + "." + fileKeyString, STRING_empty);
						executableSet.add(fileKeyString);
					} else if(isEnsureExecutable() && execPass) {
						if(file.canExecute() == false)
							invokeFileSetExecutable(file, true);	// JDK5 safe: file.setExecutable(true);
						filelistProps.put(K_prefix_executable + "." + fileKeyString, STRING_empty);
						executableSet.add(fileKeyString);
					} else {
						filelistProps.put(K_prefix_file + "." + fileKeyString, STRING_empty);
						filelistSet.add(fileKeyString);
					}
				}
			}
		}
		return;
	}

	/**
	 * QtJambi support JDK5, and this was only introduced in JDK6, so this
	 *  is forward looking implementation of {@link File#setExecutable(boolean)}
	 * @param file
	 * @param executable
	 * @return
	 * @see File#setExecutable(boolean)
	 */
	public Boolean invokeFileSetExecutable(File file, boolean executable) {
		Boolean rv = null;
		try {
			// rv = file.setExecutable(executable);
			Method method = file.getClass().getMethod("setExecutable", boolean.class);
			Object rvObj = method.invoke(file, Boolean.valueOf(executable));
			if(rvObj instanceof Boolean) {
				rv = (Boolean) rvObj;
			}
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		}
		return rv;
	}

	private boolean checkAutoDetectExecutable(File file) {
		boolean bf = file.canExecute();
		if(bf) {
			String name = file.getName().toLowerCase();
			// Should make this configurable, instead of presuming
			//  we are handling windows binaries on a windows platform,
			//  this may not be the case.
			if(name.endsWith(".dll"))
				return false;
		}
		return bf;
	}

	protected DirectoryScanner getDirectoryScanner(File baseDirectory, IFileSet fileSet) {
		DirectoryScanner scanner = null;
		scanner = new DirectoryScanner();
		scanner.setFollowSymlinks(true);

		if(fileSet != null && baseDirectory != null) {
			scanner.setBasedir(baseDirectory);

			if(fileSet.getIncludes().length == 0 && fileSet.getExcludes().length == 0) {
				String[] includes = Utils.stringArrayAppend(fileSet.getIncludes(), "**/*");
				scanner.setIncludes(includes);
			} else {
				String[] includes = fileSet.getIncludes();
				if(fileSet.getIncludes().length == 0)
					includes = Utils.stringArrayAppend(includes, "**/*");
				scanner.setIncludes(includes);
				scanner.setExcludes(fileSet.getExcludes());
			}
		}

		return scanner;
	}
}
