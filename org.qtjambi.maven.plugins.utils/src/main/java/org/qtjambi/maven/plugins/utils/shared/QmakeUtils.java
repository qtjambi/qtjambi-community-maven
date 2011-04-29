package org.qtjambi.maven.plugins.utils.shared;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.qtjambi.maven.plugins.utils.Context;
import org.qtjambi.maven.plugins.utils.IEnvironmentResolver;
import org.qtjambi.maven.plugins.utils.internal.ProcessBuilder;
import org.qtjambi.maven.plugins.utils.shared.ProcessUtils.ProcessReturn;

public class QmakeUtils {
	private Context context;

	private boolean initDone;
	private String qmakePath;		// auto-filled by resolver "bin/qmake"
	private String qmakeVersion;
	private String qtDir;

	public QmakeUtils(Context context) {
		this.context = context;
	}

	private void init() {
		if(initDone)
			return;

		qmakePath = context.getPlatform().getQtEnvironmentResolver().resolveCommandMake();

		// Set from project.qtDir property
		//String mavenQtdirString = getProjectProperty("project.qtdir");
		//context.getLog().info("maven:project.qtdir=" + mavenQtdirString);
		// Set from $QTDIR environment variable
		String envvarQtdirString = System.getenv("QTDIR");
		context.getLog().info("envvar:QTDIR=" + envvarQtdirString);
		String syspropQtdirString = System.getProperty("QTDIR");
		context.getLog().info("sysprop:QTDIR=" + syspropQtdirString);
		// Check qmake exists on path, if so go with $PATH / system defaults
		String envvarPathString = System.getenv("PATH");
		String[] pathSplit = Utils.stringSplit(envvarPathString, File.pathSeparator);
		boolean first = true;
		for(String p : pathSplit) {
			if(first) {
				context.getLog().debug("PATH=" + p);
				first = false;
			} else {
				context.getLog().debug("    =" + p);
			}
				
		}

		if(qtDir == null) {
		} else {
			// Remove trailing directory separator
			qtDir = Utils.pathCanonTrailing(qtDir);
		}

		if(qtDir != null) {		// Check QTDIR exists (if set)
			File qtDirFile = new File(qtDir);
			if(qtDirFile.exists() == false) {
				context.getLog().warn("QTDIR=" + qtDir + ": does not exist");
			} else if(qtDirFile.isDirectory() == false) {
				context.getLog().warn("QTDIR=" + qtDir + ": is not a directory");
			}
		}

		// Check qmake exists to run, warn/error if not

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
				idx++;
			}
			if(sb.length() > 0)
				return sb.toString();
		}
		return null;
	}

	public boolean detectQmakeVersion() {
		boolean bf = false;

		IEnvironmentResolver environmentResolver = context.getPlatform().getQtEnvironmentResolver();

		// Run "qmake -query" parse output
		//  We know about "QMAKE_VERSION:2.01a"
		List<String> command = new ArrayList<String>();
		command.add(resolveQtDirPath(qmakePath));
		command.add("-query");

		ProcessBuilder processBuilder = new ProcessBuilder(command);

		File directory = null;
//		for(Object k : getPluginContext().keySet()) {
//			String kS = k.toString();
//			Object v = getPluginContext().get(k);
//			String vS = (String) v.toString();
//			getLog().info(kS + " => " + vS);
//		}

		{
			String directoryString = null; // getProjectProperty("project.basedir");
			if(directoryString != null) {
				directory = new File(directoryString);
				if(directory.exists() == false) {
					String msg = String.format("workingDirectory={0} does not exist", directoryString);
					context.getLog().warn(msg);
				} else {
					String msg = String.format("workingDirectory={0} does not exist", directoryString);
					context.getLog().info(msg);
				}
			}
		}
		if(directory != null)
			processBuilder.directory(directory);

		Map<String,String> envvar = processBuilder.environment();
		environmentResolver.applyEnvironmentVariables(envvar);

		ProcessUtils processUtils = new ProcessUtils();
		ProcessReturn processReturn = ProcessUtils.run(processBuilder, StreamReaderThread.DEFAULT_SAVED_VALUE);

		String newQmakeVersion = null;

		StringReader sr = null;
		BufferedReader br = null;
		try {
			sr = new StringReader(processReturn.stdoutString);
			br = new BufferedReader(sr);
			String s;
			while((s = br.readLine()) != null) {
				final String K_QMAKE_VERSION_COLON = "QMAKE_VERSION:";

				String x;
				if((x = qmakeQueryTryMatch(s, K_QMAKE_VERSION_COLON)) != null)
					newQmakeVersion = x;

				context.getLog().info(s);
			}
			br.close();
			br = null;
			sr.close();
			sr = null;
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			if(br != null) {
				try {
					br.close();
				} catch(IOException eat) {
				}
				br = null;
			}
			if(sr != null) {
				sr.close();
				sr = null;
			}
		}

		if(processReturn.exitStatus != null && processReturn.exitStatus.intValue() == 0) {
			if(newQmakeVersion != null)
				qmakeVersion = newQmakeVersion;
			bf = true;
		}
		return bf;
	}

	public String getQmakeVersion() {
		return qmakeVersion;
	}
}
