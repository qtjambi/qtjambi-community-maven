package org.qtjambi.maven.plugins.jxe;

import java.io.File;

public class JxeExecParam {
	private File currentWorkingDirectory;
	// envvar

	public File getCurrentWorkingDirectory() {
		return currentWorkingDirectory;
	}
	public void setCurrentWorkingDirectory(File currentWorkingDirectory) {
		this.currentWorkingDirectory = currentWorkingDirectory;
	}
}
