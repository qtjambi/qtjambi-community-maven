package org.qtjambi.maven.plugins.utils;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public class ExampleTask extends AbstractMojo {

	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info(ExampleTask.class.getName() + " says Hello World!\n");
	}

}
