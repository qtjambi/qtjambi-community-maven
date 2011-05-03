package org.qtjambi.maven.plugins.utils.internal;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.logging.Log;

public class ProcessBuilder {
	private java.lang.ProcessBuilder processBuilder;

	private Log log;
	private String[] commandA;
	private boolean initCommand;

	public ProcessBuilder(Log log, List<String> command) {
		this.processBuilder = new java.lang.ProcessBuilder();
		this.log = log;
		command(command);
	}
	public ProcessBuilder(Log log, String... command) {
		this.processBuilder = new java.lang.ProcessBuilder();
		this.log = log;
		command(command);
	}
	public ProcessBuilder(Log log) {
		this.processBuilder = new java.lang.ProcessBuilder();
		this.log = log;
	}
	public ProcessBuilder(List<String> command) {
		this.processBuilder = new java.lang.ProcessBuilder();
		command(command);
	}
	public ProcessBuilder(String... command) {
		this.processBuilder = new java.lang.ProcessBuilder();
		command(command);
	}
	public ProcessBuilder() {
		this.processBuilder = new java.lang.ProcessBuilder();
	}

	public Log getLog() {
		return this.log;
	}
	public String getCommandExe() {
		if(this.commandA == null || this.commandA.length == 0)
			return null;
		return this.commandA[0];
	}

	public int hashCode() {
		return processBuilder.hashCode();
	}

	public boolean equals(Object obj) {
		return processBuilder.equals(obj);
	}

	public java.lang.ProcessBuilder command(List<String> command) {
		final int len = command.size();
		commandA = command.toArray(new String[len]);
		initCommand = true;
		return processBuilder.command(command);
	}

	public java.lang.ProcessBuilder command(String... command) {
		final int len = command.length;
		commandA = new String[len];
		System.arraycopy(command, 0, this.commandA, 0, len);
		initCommand = true;
		return processBuilder.command(command);
	}

	public List<String> command() {
		return processBuilder.command();
	}

	public Map<String, String> environment() {
		return processBuilder.environment();
	}

	public String toString() {
		return processBuilder.toString();
	}

	public File directory() {
		return processBuilder.directory();
	}

	public java.lang.ProcessBuilder directory(File directory) {
		return processBuilder.directory(directory);
	}

	public boolean redirectErrorStream() {
		return processBuilder.redirectErrorStream();
	}

	public java.lang.ProcessBuilder redirectErrorStream(boolean redirectErrorStream) {
		return processBuilder.redirectErrorStream(redirectErrorStream);
	}

	public Process start() throws IOException {
		if(!initCommand && commandA != null)
			processBuilder.command(commandA);
		return processBuilder.start();
	}
}
