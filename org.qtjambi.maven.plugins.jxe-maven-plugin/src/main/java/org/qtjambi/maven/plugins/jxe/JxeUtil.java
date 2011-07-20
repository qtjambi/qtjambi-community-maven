package org.qtjambi.maven.plugins.jxe;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.qtjambi.maven.plugins.jxe.util.StreamCopyThread;
import org.qtjambi.maven.plugins.jxe.util.SystemInSelectable;

public abstract class JxeUtil {
	public static Integer exec(String targetExecutable, String[] args, JxeExecParam jxeExecParam) {
		return exec(new File(targetExecutable), args, jxeExecParam);
	}

	public static Integer exec(File targetExecutable, String[] args, JxeExecParam jxeExecParam) {
		List<String> command = new ArrayList<String>();
		command.add("java");
		command.add("-jar");
		command.add(targetExecutable.getAbsolutePath());
		for(String s : args)
			command.add(s);
		ProcessBuilder processBuilder = new ProcessBuilder(command);

		Map<String,String> envvars = processBuilder.environment();
		// NOOP we don't do anything with envvars  jxeExecParam

		if(jxeExecParam.getCurrentWorkingDirectory() != null)
			processBuilder.directory(jxeExecParam.getCurrentWorkingDirectory());

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

		System.err.flush();
		return null;
	}
}
