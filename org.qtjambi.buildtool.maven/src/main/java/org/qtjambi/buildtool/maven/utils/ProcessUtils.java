package org.qtjambi.buildtool.maven.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.qtjambi.buildtool.maven.internal.ProcessBuilder;

public class ProcessUtils {
	public static Integer run(ProcessBuilder processBuilder) {
		OutputStream stdinStream = null;
		InputStream stdoutStream = null;
		InputStream stderrStream = null;
		Process process = null;
		Integer exitStatus = null;
		StreamReaderThread stderrReader = null;
		StreamReaderThread stdoutReader = null;
		Thread stderrThread = null;
		Thread stdoutThread = null;
		try {
			process = processBuilder.start();

			stdinStream = process.getOutputStream();
			stdinStream.close();
			stdinStream = null;

			{
				stderrStream = process.getErrorStream();
				stderrReader = new StreamReaderThread(stderrStream);

				StringBuilder sb = new StringBuilder();
				sb.append(processBuilder.getCommandExe());
				sb.append(" >E> ");
				String stderrPrefix = sb.toString();
				stderrReader.setPrefix(stderrPrefix);

				stderrReader.setLog(processBuilder.getLog());
			}

			{
				stdoutStream = process.getInputStream();
				stdoutReader = new StreamReaderThread(stdoutStream);

				StringBuilder sb = new StringBuilder();
				sb.append(processBuilder.getCommandExe());
				sb.append(" >>> ");
				String stdoutPrefix = sb.toString();
				stdoutReader.setPrefix(stdoutPrefix);

				stdoutReader.setLog(processBuilder.getLog());
			}

			stdoutThread = new Thread(stdoutReader);
			stdoutThread.start();
			stderrThread = new Thread(stderrReader);
			stderrThread.start();

			exitStatus = process.waitFor();
			if(exitStatus.intValue() != 0 && processBuilder.getLog() != null)
				processBuilder.getLog().warn(processBuilder.getCommandExe() + " === exitStatus=" + exitStatus);

			stderrThread.join();
			stdoutThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(stderrThread != null) {
				while(stderrThread.isAlive()) {
					try {
						stderrThread.join();
					} catch(InterruptedException eat) {
						eat.printStackTrace();
					}
				}
			}
			if(stdoutThread != null) {
				while(stdoutThread.isAlive()) {
					try {
						stdoutThread.join();
					} catch(InterruptedException eat) {
						eat.printStackTrace();
					}
				}
			}
			if(stderrReader != null) {
				stderrReader.close();
				stderrReader = null;
			}
			if(stdoutReader != null) {
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

		return null;
	}
}
