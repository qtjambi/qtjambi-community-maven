package org.qtjambi.buildtools.generator;

import java.io.File;

public class Executable {
	private File extractDir;
	private File targetExecutable;

	public boolean cleanup() {
		boolean bf = false;
		if(extractDir != null) {
			bf = ExecutableUtils.deleteRecursive(extractDir);
			if(extractDir.delete() == false)
				bf = false;
		}
		extractDir = null;
		targetExecutable = null;
		return bf;
	}

	public Integer run(String[] args) {
		return null;
	}

	public File getExtractDir() {
		return extractDir;
	}
	public void setExtractDir(File extractDir) {
		this.extractDir = extractDir;
	}
	public File getTargetExecutable() {
		return targetExecutable;
	}
	public void setTargetExecutable(File targetExecutable) {
		this.targetExecutable = targetExecutable;
	}
}
