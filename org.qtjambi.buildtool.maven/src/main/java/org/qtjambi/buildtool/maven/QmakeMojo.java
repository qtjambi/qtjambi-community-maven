package org.qtjambi.buildtool.maven;

import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public class QmakeMojo extends AbstractMojo {
	/**
	 * @param
	 */
	private String qtDir;

	/**
	 * @param
	 */
	private Map<String,String> environmentVariables;

	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info(QmakeMojo.class.getName() + " QMAKE\n");
	}

	private void detectQmakeVersion() {
		// Run "qmake -query" parse output
		//  We know about "QMAKE_VERSION:2.01a"
	}
}
