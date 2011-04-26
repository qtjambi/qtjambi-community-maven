package org.qtjambi.maven.plugins.utils.internal;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.logging.Log;

public class ProcessBuilder {
	private java.lang.ProcessBuilder processBuilder;

	private Log log;
	private String commandExe;

	public ProcessBuilder(Log log, List<String> command) {
		this.commandExe = command.get(0);
		this.processBuilder = new java.lang.ProcessBuilder(command);
		this.log = log;
	}
	public ProcessBuilder(Log log, String... command) {
		this.commandExe = command[0];
		this.processBuilder = new java.lang.ProcessBuilder(command);
		this.log = log;
	}
	public ProcessBuilder(Log log) {
		this.processBuilder = new java.lang.ProcessBuilder();
		this.log = log;
	}
	public ProcessBuilder(List<String> command) {
		this.processBuilder = new java.lang.ProcessBuilder(command);
	}
	public ProcessBuilder(String... command) {
		this.processBuilder = new java.lang.ProcessBuilder(command);
	}
	public ProcessBuilder() {
		this.processBuilder = new java.lang.ProcessBuilder();
	}

	public Log getLog() {
		return this.log;
	}
	public String getCommandExe() {
		return this.commandExe;
	}

	public int hashCode() {
		return processBuilder.hashCode();
	}

	public boolean equals(Object obj) {
		return processBuilder.equals(obj);
	}

	public java.lang.ProcessBuilder command(List<String> command) {
		commandExe = command.get(0);
		return processBuilder.command(command);
	}

	public java.lang.ProcessBuilder command(String... command) {
		commandExe = command[0];
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
		return processBuilder.start();
	}
}
