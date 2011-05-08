package org.qtjambi.buildtools.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public abstract class ExecutableUtils {
	public static final String K_executable					= "executable";
	public static final String K_exe						= "exe";
	public static final String K_slash_filelist_properties	= "/filelist.properties";

	public static Executable extractFromClasspath(String toplevel) {
		if(toplevel == null)
			toplevel = "";
		Properties fileListProps;
		{
			InputStream inStream = null;
			try {
				inStream = ExecutableUtils.class.getClassLoader().getResourceAsStream(toplevel + K_slash_filelist_properties);
				if(inStream == null)
					return null;
				// Load properties
				fileListProps = new Properties();
				fileListProps.load(inStream);
			} catch (IOException e) {
				Main.seenError();
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

		List<File> targetExecutableList = new ArrayList<File>();
		File extractDir = createTemporaryDirectory("." + K_exe);
		if(extractDir == null)
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
					dir = new File(extractDir, dirName);
					if(dir.isDirectory() == false) {
						if(dir.mkdir() == false)
							throw new RuntimeException("mkdir " + dir.getAbsolutePath() + "; failed");
						//System.out.println("directoryCreated: " + dir.getAbsolutePath());
					}
				} else {
					dir = extractDir;
				}

				// We allow directory creation to happen with a trailing "/" in filelist.properties
				if(fileName != null) {
					File file = new File(dir, fileName);
					// Extract file
					FileOutputStream fileOut = null;
					InputStream inStream = null;
					try {
						inStream = ExecutableUtils.class.getClassLoader().getResourceAsStream(toplevel + "/" + relPath);
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
				String v = fileListProps.getProperty(relPath);
				if(v != null) {
					if(v.equalsIgnoreCase(K_executable))
						targetExecutableList.add(new File(extractDir, relPath));
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			Main.seenError();
			// Cleanup
			deleteRecursive(extractDir);
			extractDir.delete();
			return null;
		}

		File targetExecutable = null;
		if(targetExecutableList.size() > 0)
			targetExecutable = targetExecutableList.get(0);

		Executable executable = new Executable();
		executable.setExtractDir(extractDir);
		executable.setTargetExecutable(targetExecutable);
		return executable;
	}

	public static File createTemporaryDirectory(String suffix) {
		if(suffix == null)
			suffix = ".dir";
		//long id = Thread.currentThread().getId();	// Crappy Java APIs
		Random random = new Random();
		int randomId = random.nextInt(0x7fffffff);	// keep it positive
		String dirname = "mvn" + K_exe + Long.valueOf(randomId).toString() + suffix;

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

	/**
	 * Be careful using this.
	 * @param dir
	 * @return boolean Successful state
	 */
	public static boolean deleteRecursive(File dir) {
		// PARANOID CHECK
		String pathAsString = dir.getAbsolutePath();
		if(pathAsString.length() == 0)
			return false;
		if(pathAsString.startsWith(File.separator) == false) {
			// FIXME: Do platform check, startWith() for linux/macosx, driver letter check as well for windows
			if(pathAsString.length() < 3 || (pathAsString.charAt(1) != ':' && pathAsString.charAt(2) != File.separatorChar))
				throw new RuntimeException("deleteRecursive(): " + dir.getAbsolutePath());
		}
		{
			int i, j;
			i = pathAsString.indexOf(File.separator);
			j = pathAsString.lastIndexOf(File.separator);
			if(i == j || (j - i) < 2)
				throw new RuntimeException("deleteRecursive(): " + dir.getAbsolutePath());
		}
		// PARANOID CHECK
		boolean verbose = false;
		if(System.getenv("MAVEN_EXE_VERBOSE") != null)
			verbose = true;
		boolean bf = true;
		File[] fileA = dir.listFiles();
		for(File f : fileA) {
			if(f.isDirectory()) {
				if(deleteRecursive(f) == false)
					bf = false;
			}
			try {
				if(verbose)
					System.out.println("delete: " + f.getAbsolutePath());
				if(f.delete() == false)
					bf = false;
			} catch (SecurityException e) {
				System.err.println("delete: " + f.getAbsolutePath());
				e.printStackTrace();
				Main.seenError();
				bf = false;
			}
		}
		return bf;
	}
}
