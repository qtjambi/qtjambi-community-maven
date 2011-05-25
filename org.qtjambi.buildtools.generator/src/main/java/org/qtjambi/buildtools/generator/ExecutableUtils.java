package org.qtjambi.buildtools.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public abstract class ExecutableUtils {
	public static final String K_file						= "file";
	public static final String K_directory					= "directory";
	public static final String K_executable					= "executable";
	public static final String K_exe						= "exe";
	public static final String K_dir						= "dir";
	public static final String K_slash_filelist_properties	= "/filelist.properties";

	public static String osName;

	public static enum HostKind {
		Unknown,
		Linux,
		Windows,
		Macosx
	}

	public static HostKind getHostKind() {
		String myOsName;
		synchronized(ExecutableUtils.class) {
			myOsName = osName;
			if(myOsName == null) {
				myOsName = System.getProperty("os.name");
				if(myOsName != null)
					myOsName = myOsName.toLowerCase();
				osName = myOsName;
			}
		}
		if(osName.startsWith("linux"))
			return HostKind.Linux;
		if(osName.startsWith("windows"))
			return HostKind.Windows;
		if(osName.startsWith("macosx"))
			return HostKind.Macosx;
		return HostKind.Unknown;
	}

	public static Executable extractFromClasspath(String toplevel) {
		if(toplevel == null)
			toplevel = "";
		Properties fileListProps;
		{
			InputStream inStream = null;
			try {
				inStream = ExecutableUtils.class.getClass().getResourceAsStream(toplevel + K_slash_filelist_properties);
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
		File extractDir = createTemporaryDirectory("." + K_dir);
		if(extractDir == null)
			return null;
		//System.out.println("mkdir() = " + myDir.getAbsolutePath());
		try {
			// Create each file
			Enumeration<Object> en = fileListProps.keys();
			while(en.hasMoreElements()) {
				Object kObj = en.nextElement();
				String k = kObj.toString();
				String relPath;
				boolean kindExecutable = false;
				if(k.startsWith(K_directory + ".")) {
					relPath = k.substring(K_directory.length() + 1);
				} else if(k.startsWith(K_file + ".")) {
					relPath = k.substring(K_file.length() + 1);
				} else if(k.startsWith(K_executable + ".")) {
					relPath = k.substring(K_executable.length() + 1);
					kindExecutable = true;
				} else {
					relPath = k;
				}

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

				// FIXME Sanity check, no ".." in dirName ?

				// Ensure directory is created
				File dir;
				if(dirName != null) {
					dir = new File(extractDir, dirName);
					if(dir.isDirectory() == false) {
						if(dir.mkdir() == false)
							throw new RuntimeException("mkdir " + dir.getAbsolutePath() + "; failed");
						if(Main.isVerbose())
							System.err.println("directoryCreated: " + dir.getAbsolutePath());
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
						inStream = ExecutableUtils.class.getClass().getResourceAsStream(toplevel + "/" + relPath);
						if(inStream == null)
							throw new RuntimeException("missing file listed in filelist.properties: " + relPath);
						fileOut = new FileOutputStream(file);

						byte[] bA = new byte[1024];
						long totalBytes = 0;
						int n;
						while((n = inStream.read(bA)) > 0) {
							fileOut.write(bA, 0, n);
							totalBytes += n;
						}
						fileOut.flush();
						fileOut.close();
						fileOut = null;

						inStream.close();
						inStream = null;

						if(kindExecutable) {
							invokeFileSetExecutable(file, true);	// JDK5 safe: file.setExecutable(true);
							targetExecutableList.add(file);
						}
						if(Main.isVerbose())
							System.err.println("fileWritten: " + file.getAbsolutePath() + " (" + totalBytes + " bytes)");
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

	/**
	 * QtJambi support JDK5, and this was only introduced in JDK6, so this
	 *  is forward looking implementation of {@link File#setExecutable(boolean)}
	 * @param file
	 * @param executable
	 * @return
	 * @see File#setExecutable(boolean)
	 */
	public static Boolean invokeFileSetExecutable(File file, boolean executable) {
		Boolean rv = null;
		if(rv == null) {
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
		}
		if(rv == null && new File("/bin/chmod").exists()) {
			try {
				Process process = Runtime.getRuntime().exec(new String[] { "/bin/chmod", "+x", file.getAbsolutePath() });
				try {
					process.getOutputStream().close();
				} catch(IOException eat) {
				}
				// FIXME: Should probably sink stdout/stderr
				try {
					process.getInputStream().close();
				} catch(IOException eat) {
				}
				try {
					process.getErrorStream().close();
				} catch(IOException eat) {
				}
				int exitValue = process.exitValue();
				if(exitValue == 0)
					rv = Boolean.TRUE;
				else
					rv = Boolean.FALSE;
			} catch (Exception e) {
			}
		}
		return rv;
	}

	/**
	 * QtJambi support JDK5, and this was only introduced in JDK6, so this
	 *  is forward looking implementation of {@link File#canExecutable()}
	 * @param file
	 * @return
	 * @see File#canExecute()
	 */
	public static Boolean invokeFileCanExecute(File file) {
		Boolean rv = null;
		if(rv == null) {
			try {
				// rv = file.canExecute();
				Method method = file.getClass().getMethod("canExecute", (Class<?> []) null);
				Object rvObj = method.invoke(file, (Object[]) null);
				if(rvObj instanceof Boolean) {
					rv = (Boolean) rvObj;
				}
			} catch (SecurityException e) {
			} catch (NoSuchMethodException e) {
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			} catch (InvocationTargetException e) {
			}
		}
		if(rv == null && new File("/bin/sh").exists()) {
			try {
				Process process = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", "test -x \"" + file.getAbsolutePath() + "\"" });
				try {
					process.getOutputStream().close();
				} catch(IOException eat) {
				}
				// FIXME: Should probably sink stdout/stderr
				try {
					process.getInputStream().close();
				} catch(IOException eat) {
				}
				try {
					process.getErrorStream().close();
				} catch(IOException eat) {
				}
				int exitValue = process.exitValue();
				if(exitValue == 0)
					rv = Boolean.TRUE;
				else
					rv = Boolean.FALSE;
			} catch (Exception e) {
			}
		}
		return rv;
	}

	public static boolean invokeFileCanExecuteDefault(File file, boolean defaultValue) {
		Boolean rv = invokeFileCanExecute(file);
		if(rv != null)
			return rv.booleanValue();
		return defaultValue;
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

	public static String[] stringArrayPrepend(String[] valueA, String str) {
		final int srcLength = valueA.length;
		String[] newValueA = new String[srcLength + 1];
		newValueA[0] = str;
		System.arraycopy(valueA, 0, newValueA, 1, srcLength);
		return newValueA;
	}

	public static String[] safeStringArrayPrepend(String[] valueA, String str) {
		if(valueA != null)
			return stringArrayPrepend(valueA, str);
		return new String[] { str };
	}

	public static String[] stringArraySplit(String value, String str) {
		List<String> list = new ArrayList<String>();
		int fromIndex = 0;
		int i;
		while((i = value.indexOf(str, fromIndex)) >= 0) {
			String oneValue = value.substring(fromIndex, i);
			list.add(oneValue);
			fromIndex = i + 1;
		}
		int rem = value.length() - fromIndex;
		if(rem > 0)
			list.add(value.substring(fromIndex));
		return list.toArray(new String[list.size()]);
	}

	public static String[] safeStringArraySplit(String value, String str) {
		if(value == null)
			return null;
		return stringArraySplit(value, str);
	}

	public static String stringConcat(String[] sA, String sep) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for(String s : sA) {
			if(first)
				first = false;
			else
				sb.append(sep);
			sb.append(s);
		}
		return sb.toString();
	}
}
