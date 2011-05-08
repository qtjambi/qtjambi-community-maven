package org.qtjambi.maven.plugins.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.qtjambi.maven.plugins.utils.resolvers.DefaultEnvironmentResolver;
import org.qtjambi.maven.plugins.utils.resolvers.RuntimeEnvironmentResolver;
import org.qtjambi.maven.plugins.utils.shared.ProcessUtils;
import org.qtjambi.maven.plugins.utils.shared.ProcessUtils.ProcessReturn;
import org.qtjambi.maven.plugins.utils.shared.Utils;

public class Project {
	private Context context;
	private boolean errorState;
	private File sourceDir;
	private boolean weCreatedSourceDir;
	private File targetDir;
	private boolean weCreatedTargetDir;
	private int qmakeDebugLevel;
	private Boolean qmakeRecursive;

	public Project(Context context) {
		this.context = context;
	}

	public boolean runQmake(String[] qmakeArgumentsA, File[] files) {
		if(errorState)
			return false;
		org.qtjambi.maven.plugins.utils.internal.ProcessBuilder processBuilder = new org.qtjambi.maven.plugins.utils.internal.ProcessBuilder(context.getLog());

		IEnvironmentResolver environmentResolver = context.getPlatform().getQtEnvironmentResolver();
		String qtQmake = environmentResolver.resolveCommandMake();

		Map<String,String> env = processBuilder.environment();
		environmentResolver.applyEnvironmentVariables(env);

		{
			List<String> command = new ArrayList<String>();
			String commandExe = environmentResolver.resolveCommand(sourceDir, qtQmake);
			commandExe = resolveAbsolutePath(env, commandExe);
			command.add(commandExe);

			if(qmakeDebugLevel > 0) {
				for(int i = 0; i < qmakeDebugLevel; i++)
					command.add("-d");
			}
			if(qmakeRecursive != null) {
				if(qmakeRecursive.booleanValue())
					command.add("-r");		// old option still understood
				else
					command.add("-norecursive");
			}
			if(qmakeArgumentsA != null) {
				for(String s : qmakeArgumentsA)
					command.add(s);
			}
			if(files == null) {
				if(targetDir != null && sourceDir != null) {		// shadow build, search sourceDir
					command.add(sourceDir.getAbsolutePath() + File.pathSeparatorChar);
					//command.add(".." + File.separator + sourceDir.getName());
				}
			} else {
				for(File f : files)
					command.add(f.getAbsolutePath());
			}
			processBuilder.command(command);
		}

		if(targetDir != null)
			processBuilder.directory(targetDir);
		else if(sourceDir != null)
			processBuilder.directory(sourceDir);

		Integer exitStatus;
		try {
			ProcessReturn processReturn = ProcessUtils.run(processBuilder);
			exitStatus = processReturn.exitStatus;
			if(exitStatus == null || exitStatus.intValue() != 0) {
				errorState = true;
				return false;
			}
		} catch(Exception e) {
			e.printStackTrace();
			errorState = true;
			return false;
		}
		// FIXME: Record qmake works or not to inhibit the next step or not
		return true;
	}

	public boolean runMake(String[] makeArgumentsA) {
		if(errorState)
			return false;

		org.qtjambi.maven.plugins.utils.internal.ProcessBuilder processBuilder = new org.qtjambi.maven.plugins.utils.internal.ProcessBuilder(context.getLog());

		IEnvironmentResolver environmentResolver = context.getPlatform().environmentResolverWithToolchain();
		String make = environmentResolver.resolveCommandMake();

		Map<String,String> env = processBuilder.environment();
		environmentResolver.applyEnvironmentVariables(env);

		{
			List<String> command = new ArrayList<String>();
			String commandExe = environmentResolver.resolveCommand(sourceDir, make);
			commandExe = resolveAbsolutePath(env, commandExe);
			command.add(commandExe);

			if(makeArgumentsA != null) {
				for(String s : makeArgumentsA)
					command.add(s);
			}

			processBuilder.command(command);
		}

		if(targetDir != null)
			processBuilder.directory(targetDir);
		else if(sourceDir != null)
			processBuilder.directory(sourceDir);

		Integer exitStatus;
		try {
			ProcessReturn processReturn = ProcessUtils.run(processBuilder);
			exitStatus = processReturn.exitStatus;
			if(exitStatus == null || exitStatus.intValue() != 0) {
				errorState = true;
				return false;
			}
		} catch(Exception e) {
			e.printStackTrace();
			errorState = true;
			return false;
		}
		return true;
	}

	public boolean runProjectProgram(String progName) {
		if(errorState)
			return false;
		org.qtjambi.maven.plugins.utils.internal.ProcessBuilder processBuilder = new org.qtjambi.maven.plugins.utils.internal.ProcessBuilder(context.getLog());

		IEnvironmentResolver environmentResolver = new RuntimeEnvironmentResolver(context.getPlatform());

		Map<String,String> env = processBuilder.environment();
		environmentResolver.applyEnvironmentVariables(env);

		{
			String progPath = progName;
			if(context.getPlatform().isWindows(false))
				progPath = "release" + File.separator + progName;
			else if(context.getPlatform().isLinux(false))
				progPath = "." + File.separator + progName;
			else if(context.getPlatform().isMacosx(false))
				progPath = "." + File.separator + progName;

			File dir = sourceDir;
			if(targetDir != null)
				dir = targetDir;

			List<String> command = new ArrayList<String>();
			String commandExe = environmentResolver.resolveCommand(dir, progPath);
			commandExe = resolveAbsolutePath(env, commandExe);
			command.add(commandExe);

			processBuilder.command(command);
		}

		if(targetDir != null)
			processBuilder.directory(targetDir);
		else if(sourceDir != null)
			processBuilder.directory(sourceDir);

		Integer exitStatus;
		try {
			ProcessReturn processReturn = ProcessUtils.run(processBuilder);
			exitStatus = processReturn.exitStatus;
			if(exitStatus == null || exitStatus.intValue() != 0) {
				errorState = true;
				return errorState;
			}
		} catch(Exception e) {
			e.printStackTrace();
			errorState = true;
			return false;
		}
		return true;
	}

	public boolean cleanup() {
		if(weCreatedTargetDir && targetDir != null) {
			System.out.println("cleanup() " + targetDir.getAbsolutePath());
			Utils.deleteRecursive(targetDir);
			boolean bf = targetDir.delete();
			System.out.println("cleanup() = " + bf + "; targetDir");
			targetDir = null;
		}
		if(weCreatedSourceDir && sourceDir != null) {
			System.out.println("cleanup() " + sourceDir.getAbsolutePath());
			Utils.deleteRecursive(sourceDir);
			boolean bf = sourceDir.delete();
			System.out.println("cleanup() = " + bf + "; sourceDir");
			sourceDir = null;
		}
		return true;
	}

	public void reset() {
		errorState = false;
		// run "make distclean" ?
		if(weCreatedTargetDir && targetDir != null) {
			// Cleanup all the files under, but keep the targetDir itself
			Utils.deleteRecursive(targetDir);
		}
	}

	public boolean getErrorState() {
		return errorState;
	}

	public void setSourceDir(File sourceDir, boolean weCreatedSourceDir) {
		this.sourceDir = sourceDir;
		this.weCreatedSourceDir = weCreatedSourceDir;
	}

	public void setTargetDir(File targetDir, boolean weCreatedTargetDir) {
		this.targetDir = targetDir;
		this.weCreatedTargetDir = weCreatedTargetDir;
	}

	public void setQmakeDebugLevel(int qmakeDebugLevel) {
		this.qmakeDebugLevel = qmakeDebugLevel;
	}

	public void setQmakeRecursive(Boolean qmakeRecursive) {
		this.qmakeRecursive = qmakeRecursive;
	}

	// FIXME unused
	public static URL[] getResourceList(String toplevel) {
		URL url = Project.class.getClassLoader().getResource(toplevel);
		if(url == null)
			return null;
		if(url.getProtocol().equals("file")) {
			File file;
			try {
				file = new File(url.toURI());
				File[] fA = file.listFiles();
				List<URL> urlList = new ArrayList<URL>();
				for(File f : fA) {
					urlList.add(f.toURI().toURL());
				}
				return urlList.toArray(new URL[urlList.size()]);
			} catch (URISyntaxException e) {
			} catch (MalformedURLException e) {
			}
			return null;
		}
		if(url.getProtocol().equals("jar")) {
			try {
				String jarPath = url.getPath().substring(5, url.getPath().indexOf("!")); //strip out only the JAR file
				JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
				Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
				List<URL> result = new ArrayList<URL>();
				while(entries.hasMoreElements()) {
					JarEntry jarEntry = entries.nextElement();
					String name = jarEntry.getName();
					if(name.startsWith(toplevel)) { //filter according to the toplevel
						URL jarEntryUrl = new URL("jar:file:" + jarPath + "!/" + jarEntry.getName());
						result.add(jarEntryUrl);
					}
				}
				return result.toArray(new URL[result.size()]);
			} catch (MalformedURLException e) {
			} catch (UnsupportedEncodingException e) {
			} catch (IOException e) {
			}
			return null;
		}
		return null;
	}

	public static final String K_slash_filelist_properties = "/filelist.properties";

	public static Project extractFromClasspath(Context context, String toplevel) {
		Properties fileListProps;
		{
			InputStream inStream = null;
			try {
				inStream = Project.class.getClassLoader().getResourceAsStream(toplevel + K_slash_filelist_properties);
				if(inStream == null)
					return null;
				// Load properties
				fileListProps = new Properties();
				fileListProps.load(inStream);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} finally {
				if(inStream != null) {
					try {
						inStream.close();
					} catch(IOException eat) {
					}
					inStream = null;
				}
			}
		}

		File sourceDir = createTemporaryDirectory(".src");
		if(sourceDir == null)
			return null;
		//System.out.println("mkdir() = " + myDir.getAbsolutePath());
		try {
			// Create each file
			Enumeration<Object> en = fileListProps.keys();
			while(en.hasMoreElements()) {
				Object o = en.nextElement();
				String relPath = o.toString();

				String dirName;
				String fileName;

				int i;
				if((i = relPath.indexOf("/")) >= 0) {
					if(i == 0)
						throw new RuntimeException("Illegal configuration, absolute path used in " + toplevel + K_slash_filelist_properties);

					i = relPath.lastIndexOf("/");

					dirName = relPath.substring(0, i);
					fileName = relPath.substring(i + 1);
					if(fileName.length() == 0)
						fileName = null;
				} else {
					dirName = null;
					fileName = relPath;
				}

				// Ensure directory is created
				File dir;
				if(dirName != null) {
					dir = new File(sourceDir, dirName);
					if(dir.isDirectory() == false) {
						if(dir.mkdir() == false)
							throw new RuntimeException("mkdir " + dir.getAbsolutePath() + "; failed");
						//System.out.println("directoryCreated: " + dir.getAbsolutePath());
					}
				} else {
					dir = sourceDir;
				}

				// We allow directory creation to happen with a trailing "/" in filelist.properties
				if(fileName != null) {
					File file = new File(dir, fileName);
					// Extract file
					FileOutputStream fileOut = null;
					InputStream inStream = null;
					try {
						inStream = Project.class.getClassLoader().getResourceAsStream(toplevel + "/" + relPath);
						if(inStream == null)
							throw new RuntimeException("missing file listed in filelist.properties: " + relPath);
						fileOut = new FileOutputStream(file);

						byte[] bA = new byte[1024];
						int n;
						while((n = inStream.read(bA)) > 0) {
							fileOut.write(bA, 0, n);
						}
						fileOut.flush();
						fileOut.close();
						fileOut = null;

						inStream.close();
						inStream = null;

						//System.out.println("fileWritten: " + file.getAbsolutePath());
					} finally {
						if(fileOut != null) {
							try {
								fileOut.close();
							} catch(IOException eat) {
							}
							fileOut = null;
						}
						if(inStream != null) {
							try {
								inStream.close();
							} catch(IOException eat) {
							}
							inStream = null;
						}
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			// Cleanup
			Utils.deleteRecursive(sourceDir);
			sourceDir.delete();
			return null;
		}

		File targetDir = createTemporaryDirectory(".tgt");
		if(targetDir == null) {	// cleanup
			Utils.deleteRecursive(sourceDir);
			sourceDir.delete();
			return null;
		}

		//System.out.println("project extracted: " + myDir.getAbsolutePath());
		Project project = new Project(context);
		if(targetDir != null)
			project.setTargetDir(targetDir, true);
		if(sourceDir != null)
			project.setSourceDir(sourceDir, true);
		return project;
	}

	public static Project extractFromClasspathOld(Context context, String toplevel) {
		try {
			URL[] urlA = getResourceList(toplevel);
			if(urlA == null)
				return null;
			for(URL url : urlA) {
				System.out.println("URL=" + url.toString());
			}
		//} catch (IOException e) {
		//	e.printStackTrace();
		} finally {
		}

		File myDir = createTemporaryDirectory(null);
		if(myDir == null)
			return null;
		System.out.println("mkdir() = " + myDir.getAbsolutePath());
		try {
			File foo = File.createTempFile("pfx", ".tmp", myDir);
			File subdir = new File(myDir, "subdir");
			subdir.mkdir();
			File bar = File.createTempFile("pfx", ".tmp", subdir);
		} catch(IOException e) {
			e.printStackTrace();
		}

		Project project = new Project(context);
		project.sourceDir = myDir;
		project.weCreatedSourceDir = true;
		return project;
	}

	public static File createTemporaryDirectory(String suffix) {
		if(suffix == null)
			suffix = ".dir";
		//long id = Thread.currentThread().getId();	// Crappy Java APIs
		Random random = new Random();
		int randomId = random.nextInt(0x7fffffff);	// keep it positive
		String dirname = "qtmvn" + Long.valueOf(randomId).toString() + suffix;

		String s = System.getProperty("java.io.tmpdir");
		if(s != null) {
			File tmpdir = new File(s);
			if(tmpdir.exists() && tmpdir.isDirectory()) {
				File myDir = new File(tmpdir, dirname);
				if(!myDir.exists() && myDir.mkdir() && myDir.exists() && myDir.isDirectory())
					return myDir;
			}
		}
		s = System.getenv("TEMP");
		if(s != null) {
			File tmpdir = new File(s);
			if(tmpdir.exists() && tmpdir.isDirectory()) {
				File myDir = new File(tmpdir, dirname);
				if(!myDir.exists() && myDir.mkdir() && myDir.exists() && myDir.isDirectory())
					return myDir;
			}
		}
		s = System.getenv("TMP");
		if(s != null) {
			File tmpdir = new File(s);
			if(tmpdir.exists() && tmpdir.isDirectory()) {
				File myDir = new File(tmpdir, dirname);
				if(!myDir.exists() && myDir.mkdir() && myDir.exists() && myDir.isDirectory())
					return myDir;
			}
		}
		return null;
	}

	// On windows at least the EXE to run but be specified as an absolute path
	private String resolveAbsolutePath(Map<String,String> env, String commandExe) {
		if(commandExe.indexOf(File.separatorChar) < 0) {
			String envvarPath = env.get(DefaultEnvironmentResolver.K_PATH);
			String newCommandExe = Utils.searchPath(envvarPath, commandExe);
			if(newCommandExe != null) {
				context.getLog().debug("SEARCH: " + newCommandExe);
				commandExe = newCommandExe;
			}
		}
		return commandExe;
	}
}
