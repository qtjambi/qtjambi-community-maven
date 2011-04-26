package org.qtjambi.maven.plugins.qmake;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.qtjambi.maven.plugins.utils.internal.ProcessBuilder;

/**
 * 
 * @goal initialize
 * @author <a href="mailto:darryl.miles@darrylmiles.org">Darryl L. Miles</a>
 *
 */
public class InitializeMojo extends AbstractMojo {
	/**
	 * @parameter
	 */
	private String testString;

	/**
	 * @parameter
	 */
	private String qtDir;

	/**
	 * @parameter
	 */
	private Map<String,String> environmentVariables;

	private boolean initDone;

	private String execSuffix = "";
	private String K_bin_qmake;		// auto-filled by platform "bin/qmake"
	private String qmakeVersion;
	private static Object qmakeSettings;

	/**
	 * @execute
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		init();
		getLog().info(InitializeMojo.class.getName() + " QMAKE");
		detectQmakeVersion();
		getLog().info(InitializeMojo.class.getName() + " QMAKE_VERSION=" + qmakeVersion);
	}

	private void init() {
		if(initDone)
			return;

		if(File.separator.compareTo("\\") == 0)		// FIXME so proper windows detection
			execSuffix = ".exe";

		if(K_bin_qmake == null)
			K_bin_qmake = "bin" + File.separator + "qmake" + execSuffix;

		if(qtDir == null) {
			// Set from project.qtDir property
			// Set from $QTDIR environment variable
			// Check qmake exists on path, if so go with $PATH / system defaults
		} else {
			// Remove trailing directory separator
			final String fileSeparator = File.separator;
			final int fileSeparatorLen = fileSeparator.length();
			while(true) {
				int len = qtDir.length();
				if(len >= fileSeparatorLen) {
					String s = qtDir.substring(len - fileSeparatorLen, fileSeparatorLen);
					if(s.compareTo(fileSeparator) == 0) {
						qtDir = qtDir.substring(0, len - fileSeparatorLen);
						continue;		// try again
					}
				}
				break;
			}
		}

		if(qtDir != null) {		// Check QTDIR exists (if set)
			File qtDirFile = new File(qtDir);
			if(qtDirFile.exists() == false) {
				getLog().warn("QTDIR=" + qtDir + ": does not exist");
			} else if(qtDirFile.isDirectory() == false) {
				getLog().warn("QTDIR=" + qtDir + ": is not a directory");
			}
		}

		// Check qmake exists to run, warn/error if not

		synchronized(InitializeMojo.class) {	
			qmakeSettings = new Object();
		}

		initDone = true;
	}

	private String resolveQtDirPath(String p) {
		StringBuffer sb = new StringBuffer();
		if(qtDir != null) {
			sb.append(qtDir);
			sb.append(File.separator);
		}
		sb.append(p);	// should not have a leading slash
		return sb.toString();
	}

	/**
	 * 
	 * @param line	The full one-line of text to process from "qmake -query" output
	 * @param propName	The name of the property we are looking for
	 * @return Returns null upon no match (or empty string match)
	 */
	private String qmakeQueryTryMatch(final String line, final String propName) {
		int idx;
		if((idx = line.indexOf(propName)) >= 0) {
			final int lineLen = line.length();
			StringBuilder sb = new StringBuilder();
			idx += propName.length();
			while(idx < lineLen) {
				char c = line.charAt(idx);
				// Allow other whitespace to exist in the line
				if(c == '\r' || c == '\n')
					break;
				sb.append(c);
			}
			if(sb.length() > 0)
				sb.toString();
		}
		return null;
	}

	private boolean detectQmakeVersion() {
		boolean bf = false;

		// Run "qmake -query" parse output
		//  We know about "QMAKE_VERSION:2.01a"
		List<String> command = new ArrayList<String>();
		command.add(resolveQtDirPath(K_bin_qmake));
		command.add("-query");

		ProcessBuilder processBuilder = new ProcessBuilder(command);

		File directory = null;
		if(getPluginContext().containsKey("basedir")) {
			String directoryString = (String) getPluginContext().get("basedir");
			directory = new File(directoryString);
			if(directory.exists() == false) {
				String msg = String.format("workingDirectory={0} does not exist", directoryString);
				getLog().warn(msg);
			}
		}
		if(directory != null)
			processBuilder.directory(directory);

		Map<String,String> env = processBuilder.environment();

		String newQmakeVersion = null;

		OutputStream stdinStream = null;
		InputStream stdoutStream = null;
		InputStream stderrStream = null;
		Process process = null;
		Integer exitStatus = null;
		try {
			process = processBuilder.start();

			stdinStream = process.getOutputStream();
			stdinStream.close();
			stdinStream = null;

			stderrStream = process.getErrorStream();
			stderrStream.close();
			stderrStream = null;

			stdoutStream = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(stdoutStream);
			BufferedReader br = new BufferedReader(isr);
			String s;
			while((s = br.readLine()) != null) {
				final String K_QMAKE_VERSION_COLON = "QMAKE_VERSION:";

				String x;
				if((x = qmakeQueryTryMatch(s, K_QMAKE_VERSION_COLON)) != null)
					newQmakeVersion = x;


				getLog().info(s);
			}
			br.close();
			br = null;
			isr.close();
			isr = null;
			stdoutStream.close();
			stdoutStream = null;

			exitStatus = process.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
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

		if(exitStatus != null && exitStatus.intValue() == 0) {
			qmakeVersion = newQmakeVersion;
			bf = true;
		}
		return bf;
	}

	public static Object testStatic() {
		return qmakeSettings;
	}
}
