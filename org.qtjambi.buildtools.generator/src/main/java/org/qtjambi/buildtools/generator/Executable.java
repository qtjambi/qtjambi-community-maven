package org.qtjambi.buildtools.generator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Executable {
	private static final String K_PATH				= "PATH";
	private static final String K_LD_LIBRARY_PATH	= "LD_LIBRARY_PATH";
	private static final String K_DYLD_LIBRARY_PATH	= "DYLD_LIBRARY_PATH";

	private File extractDir;
	private File targetExecutable;

	public boolean cleanup() {
		boolean bf = false;
		if(extractDir != null) {
			if(Main.isVerbose())
				System.err.println("cleanup()");
			if(Main.isNoCleanup()) {
				System.err.println("noCleanup: " + extractDir.getAbsolutePath());
			} else {
				bf = ExecutableUtils.deleteRecursive(extractDir);
				if(extractDir.delete() == false)
					bf = false;
			}
		}
		extractDir = null;
		targetExecutable = null;
		return bf;
	}

	private String findEnvVarKey(Map<String,String> envvars, String defaultKey) {
		if(envvars.containsKey(defaultKey))
			return defaultKey;
		// On windows this can be mixed case
		for(Map.Entry<String, String> e : envvars.entrySet()) {
			String k = e.getKey();
			if(k.equalsIgnoreCase(defaultKey))
				return k;
		}
		return defaultKey;
	}

	public Integer run(String[] args) {
		List<String> command = new ArrayList<String>();
		command.add(targetExecutable.getAbsolutePath());
		for(String s : args)
			command.add(s);
		ProcessBuilder processBuilder = new ProcessBuilder(command);

		Map<String,String> envvars = processBuilder.environment();

		File libDir = new File(extractDir, "lib");
		String libPathItem = null;
		if(libDir.exists() && libDir.isDirectory())
			libPathItem = libDir.getAbsolutePath();
		else
			libPathItem = extractDir.getAbsolutePath();	// fallback
		ExecutableUtils.HostKind hostKind = ExecutableUtils.getHostKind();
		if(libPathItem != null) {
			String key = null;
			if(hostKind == ExecutableUtils.HostKind.Windows) {
				key = findEnvVarKey(envvars, K_PATH);
				String pathString = envvars.get(key);
				String[] pathA = ExecutableUtils.safeStringArraySplit(pathString, File.pathSeparator);
				pathA = ExecutableUtils.safeStringArrayPrepend(pathA, libPathItem);
				pathString = ExecutableUtils.stringConcat(pathA, File.pathSeparator);
				envvars.put(key, pathString);
			} else if(hostKind == ExecutableUtils.HostKind.Linux) {
				key = K_LD_LIBRARY_PATH;
				String pathString = envvars.get(key);
				String[] pathA = ExecutableUtils.safeStringArraySplit(pathString, File.pathSeparator);
				pathA = ExecutableUtils.safeStringArrayPrepend(pathA, libPathItem);
				pathString = ExecutableUtils.stringConcat(pathA, File.pathSeparator);
				envvars.put(key, pathString);
			} else if(hostKind == ExecutableUtils.HostKind.Macosx) {
				key = K_DYLD_LIBRARY_PATH;
				String pathString = envvars.get(key);
				String[] pathA = ExecutableUtils.safeStringArraySplit(pathString, File.pathSeparator);
				pathA = ExecutableUtils.safeStringArrayPrepend(pathA, libPathItem);
				pathString = ExecutableUtils.stringConcat(pathA, File.pathSeparator);
				envvars.put(key, pathString);
			}
			if(Main.isVerbose() && key != null)
				System.err.println(key + "+=" + libPathItem);
		}

		OutputStream stdinStream = null;
		InputStream stdoutStream = null;
		InputStream stderrStream = null;
		Process process = null;
		Integer exitStatus = null;
		StreamCopyThread stdinWriter = null;
		StreamCopyThread stderrReader = null;
		StreamCopyThread stdoutReader = null;
		try {
			process = processBuilder.start();

			// What are we doing here, well we are creating a couple of threads to
			//  asynchronously handle stdout/stderr streams and emit them on the
			//  logger (or sink them).

			{
				stdinStream = process.getOutputStream();
				SystemInSelectable stdinSelectable = new SystemInSelectable(System.in);
				stdinWriter = new StreamCopyThread(stdinSelectable.getStdinChannel(), System.in, stdinStream);
			}

			{
				stderrStream = process.getErrorStream();
				stderrReader = new StreamCopyThread(stderrStream, System.err);
			}

			{
				stdoutStream = process.getInputStream();
				stdoutReader = new StreamCopyThread(stdoutStream, System.out);
			}

			stdinWriter.setName("stdinWriter");
			stdinWriter.setDaemon(true);
			stdinWriter.start();
			stdoutReader.setName("stdoutReader");
			stdoutReader.setDaemon(true);
			stdoutReader.start();
			stderrReader.setName("stderrReader");
			stderrReader.setDaemon(true);
			stderrReader.start();

			exitStatus = process.waitFor();

			// Do the thread joins in the finally part

			return exitStatus;
		} catch(InterruptedException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			if(stdinWriter != null) {
				while(stdinWriter.isAlive()) {
					try {
						stdinWriter.shutdown();
						if(stdinWriter.isUsingChannel())
							stdinWriter.join();
						// We try our best to reap this
						break;		// PARANOID
					} catch(InterruptedException eat) {
						eat.printStackTrace();
					}
				}
				stdinWriter.close();
				stdinWriter = null;
			}
			if(stderrReader != null) {
				while(stderrReader.isAlive()) {
					try {
						stderrReader.shutdown();
						stderrReader.join();
						break;		// PARANOIA
					} catch(InterruptedException eat) {
						eat.printStackTrace();
					}
				}
				stderrReader.close();
				stderrReader = null;
			}
			if(stdoutReader != null) {
				while(stdoutReader.isAlive()) {
					try {
						stdoutReader.shutdown();
						stdoutReader.join();
						break;		// PARANOIA
					} catch(InterruptedException eat) {
						eat.printStackTrace();
					}
				}
				stdoutReader.close();
				stdoutReader = null;
			}
			if(stdinStream != null) {
				try {
					stdinStream.close();
				} catch (IOException eat) {
				}
				stdinStream = null;
			}
			if(stdoutStream != null) {
				try {
					stdoutStream.close();
				} catch (IOException eat) {
				}
				stdoutStream = null;
			}
			if(stderrStream != null) {
				try {
					stderrStream.close();
				} catch (IOException eat) {
				}
				stderrStream = null;
			}
			if(process != null) {
				process.destroy();
				process = null;
			}
		}

		if(Main.isVerbose())
			System.err.flush();
		return null;
	}

	public File getExtractDir() {
		return extractDir;
	}
	public void setExtractDir(File extractDir) {
		this.extractDir = extractDir;
	}
	public File getTargetExecutable() {
		return targetExecutable;
	}
	public void setTargetExecutable(File targetExecutable) {
		this.targetExecutable = targetExecutable;
	}
}
