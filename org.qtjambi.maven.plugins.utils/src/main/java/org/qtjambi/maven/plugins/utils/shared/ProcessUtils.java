package org.qtjambi.maven.plugins.utils.shared;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.qtjambi.maven.plugins.utils.internal.ProcessBuilder;

public class ProcessUtils {
	public static Integer run(ProcessBuilder processBuilder) {
		if(processBuilder.getLog().isDebugEnabled()) {
			List<String> command = processBuilder.command();
			String[] commandA = command.toArray(new String[command.size()]);
			String cmd = Utils.stringConcat(commandA, " ");
			processBuilder.getLog().debug("  EXEC: " + cmd);

			if(processBuilder.directory() != null)
				processBuilder.getLog().debug("   CWD: " + processBuilder.directory().getAbsolutePath());

			boolean first = true;
			Map<String,String> envMap = processBuilder.environment();
			TreeMap<String,String>envSortedMap = new TreeMap<String,String>(envMap);	// sort it
			for(Map.Entry<String,String> e : envSortedMap.entrySet()) {
				// FIXME: Do special pretty output for PATH like variable
				if(first) {
					first = false;
					processBuilder.getLog().debug("ENVVAR: " + e.getKey() + "=" + e.getValue());
				} else {
					processBuilder.getLog().debug("        " + e.getKey() + "=" + e.getValue());
				}
			}
		}

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

			// What are we doing here, well we are creating a couple of threads to
			//  asynchronously handle stdout/stderr streams and emit them on the
			//  logger (or sink them).
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

				if(processBuilder.getLog().isDebugEnabled())
					stdoutReader.setLog(processBuilder.getLog());
			}

			stdoutThread = new Thread(stdoutReader);
			stdoutThread.start();
			stderrThread = new Thread(stderrReader);
			stderrThread.start();

			exitStatus = process.waitFor();
			if(exitStatus.intValue() != 0 && processBuilder.getLog() != null)
				processBuilder.getLog().warn(processBuilder.getCommandExe() + " === exitStatus=" + exitStatus);
			else if(processBuilder.getLog().isDebugEnabled())
				processBuilder.getLog().debug("  EXEC COMPLETE === exitStatus=" + exitStatus + "; " + ((exitStatus.intValue() == 0) ? "SUCCESS" : "ERROR"));

			stderrThread.join();
			stdoutThread.join();

			return exitStatus;
		} catch(InterruptedException e) {
			processBuilder.getLog().debug("  EXEC FAILURE EXCEPTION ", e);
		} catch(IOException e) {
			processBuilder.getLog().debug("  EXEC FAILURE EXCEPTION ", e);
		} finally {
			if(stderrThread != null) {
				while(stderrThread.isAlive()) {
					try {
						stderrThread.join();
						break;		// PARANOIA
					} catch(InterruptedException eat) {
						eat.printStackTrace();
					}
				}
			}
			if(stdoutThread != null) {
				while(stdoutThread.isAlive()) {
					try {
						stdoutThread.join();
						break;		// PARANOIA
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
