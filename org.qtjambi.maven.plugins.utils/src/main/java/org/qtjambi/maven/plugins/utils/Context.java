package org.qtjambi.maven.plugins.utils;

import org.apache.maven.plugin.logging.Log;
import org.qtjambi.maven.plugins.utils.internal.Arguments;

public class Context {
	private Platform platform;
	private Arguments arguments;
	private Log log;

	public Context(Platform platform, Arguments arguments, Log log) {
		setPlatform(platform);
		setArguments(arguments);
		setLog(log);
	}

	public Platform getPlatform() {
		return platform;
	}
	public void setPlatform(Platform platform) {
		this.platform = platform;
	}

	//public Arguments getArguments() {
	//	return arguments;
	//}
	public void setArguments(Arguments arguments) {
		this.arguments = arguments;
	}

	public Log getLog() {
		return log;
	}
	public void setLog(Log log) {
		this.log = log;
	}
}
