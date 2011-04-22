package org.qtjambi.buildtool.maven;

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

import org.qtjambi.buildtool.maven.resolvers.RuntimeEnvironmentResolver;
import org.qtjambi.buildtool.maven.utils.ProcessUtils;
import org.qtjambi.buildtool.maven.utils.Utils;

public class Project {
	private Context context;
	private File toplevelDir;
	private boolean weCreated;

	public Project(Context context) {
		this.context = context;
	}

	public boolean runQmake() {
		IEnvironmentResolver environmentResolver = context.getPlatform().getQtEnvironmentResolver();

		String qtQmake = environmentResolver.resolveCommandMake();

		List<String> command = new ArrayList<String>();
		String commandExe = environmentResolver.resolveCommand(toplevelDir, qtQmake);
		command.add(commandExe);
		//for(Object o : args)
		//	command.add(o.toString());
		org.qtjambi.buildtool.maven.internal.ProcessBuilder processBuilder = new org.qtjambi.buildtool.maven.internal.ProcessBuilder(context.getLog(), command);

		if(toplevelDir != null)
			processBuilder.directory(toplevelDir);

		Map<String,String> env = processBuilder.environment();
		environmentResolver.applyEnvironmentVariables(env);

		Integer exitStatus;
		try {
			exitStatus = ProcessUtils.run(processBuilder);
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		// FIXME: Record qmake works or not to inhibit the next step or not
		return true;
	}

	public boolean runMake() {
		IEnvironmentResolver environmentResolver = context.getPlatform().environmentResolverWithToolchain();

		String make = environmentResolver.resolveCommandMake();

		List<String> command = new ArrayList<String>();
		String commandExe = environmentResolver.resolveCommand(toplevelDir, make);
		command.add(commandExe);
		//for(Object o : args)
		//	command.add(o.toString());
		org.qtjambi.buildtool.maven.internal.ProcessBuilder processBuilder = new org.qtjambi.buildtool.maven.internal.ProcessBuilder(context.getLog(), command);

		if(toplevelDir != null)
			processBuilder.directory(toplevelDir);

		Map<String,String> env = processBuilder.environment();
		environmentResolver.applyEnvironmentVariables(env);

		Integer exitStatus;
		try {
			exitStatus = ProcessUtils.run(processBuilder);
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean runProjectProgram(String progName) {
		IEnvironmentResolver environmentResolver = new RuntimeEnvironmentResolver(context.getPlatform());

		String progPath = progName;
		if(context.getPlatform().isWindows(false))
			progPath = "release" + File.separator + progName;
		else if(context.getPlatform().isLinux(false))
			progPath = "." + File.separator + progName;
		else if(context.getPlatform().isMacosx(false))
			progPath = "." + File.separator + progName;

		List<String> command = new ArrayList<String>();
		String commandExe = environmentResolver.resolveCommand(toplevelDir, progPath);
		command.add(commandExe);
		//for(Object o : args)
		//	command.add(o.toString());
		org.qtjambi.buildtool.maven.internal.ProcessBuilder processBuilder = new org.qtjambi.buildtool.maven.internal.ProcessBuilder(context.getLog(), command);

		if(toplevelDir != null)
			processBuilder.directory(toplevelDir);

		Map<String,String> env = processBuilder.environment();
		environmentResolver.applyEnvironmentVariables(env);

		Integer exitStatus;
		try {
			exitStatus = ProcessUtils.run(processBuilder);
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean cleanup() {
		if(weCreated && toplevelDir != null) {
			System.out.println("cleanup() " + toplevelDir.getAbsolutePath());
			Utils.deleteRecursive(toplevelDir);
			boolean bf = toplevelDir.delete();
			System.out.println("cleanup() = " + bf);
			toplevelDir = null;
		}
		return true;
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

		File myDir = createTemporaryDirectory();
		if(myDir == null)
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
					dir = new File(myDir, dirName);
					if(dir.isDirectory() == false) {
						if(dir.mkdir() == false)
							throw new RuntimeException("mkdir " + dir.getAbsolutePath() + "; failed");
						//System.out.println("directoryCreated: " + dir.getAbsolutePath());
					}
				} else {
					dir = myDir;
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
			Utils.deleteRecursive(myDir);
			return null;
		}

		//System.out.println("project extracted: " + myDir.getAbsolutePath());
		Project project = new Project(context);
		project.toplevelDir = myDir;
		project.weCreated = true;
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

		File myDir = createTemporaryDirectory();
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
		project.toplevelDir = myDir;
		project.weCreated = true;
		return project;
	}

	public static File createTemporaryDirectory() {
		
		//long id = Thread.currentThread().getId();	// Crappy Java APIs
		Random random = new Random();
		int randomId = random.nextInt(0x7fffffff);	// keep it positive
		String dirname = "qtmvn" + Long.valueOf(randomId).toString() + ".dir";

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
}
