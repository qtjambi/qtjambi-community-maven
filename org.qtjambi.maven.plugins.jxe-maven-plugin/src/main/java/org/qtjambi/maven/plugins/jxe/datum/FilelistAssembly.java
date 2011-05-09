package org.qtjambi.maven.plugins.jxe.datum;

import java.io.File;

import org.qtjambi.maven.plugins.utils.shared.Utils;

public class FilelistAssembly implements IFileSet {
	private File directory;
	private String[] includes;
	private String[] excludes;
	private TargetExecutable/*[]*/ targetExecutable;

	public FilelistAssembly() {
		includes = new String[0];
		excludes = new String[0];
		targetExecutable = new TargetExecutable/*[0]*/();
	}

	public File getDirectory() {
		return directory;
	}
	public void setDirectory(File directory) {
		this.directory = directory;
	}
	public String[] getIncludes() {
		return includes;
	}
	public void setIncludes(String[] includes) {
		this.includes = includes;
	}
	public String[] getExcludes() {
		return excludes;
	}
	public void setExcludes(String[] excludes) {
		this.excludes = excludes;
	}
	public TargetExecutable/*[]*/ getTargetExecutable() {
		return targetExecutable;
	}
	public void setTargetExecutable(TargetExecutable/*[]*/ targetExecutable) {
		this.targetExecutable = targetExecutable;
	}

	public final String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("directory=");
		sb.append(directory.toString());
		sb.append(", includes=");
		sb.append(Utils.debugStringArrayPretty(includes));
		sb.append(", excludes=");
		sb.append(Utils.debugStringArrayPretty(excludes));
		sb.append(", targetExecutable=");
		if(targetExecutable != null)
			sb.append(targetExecutable.toString());
		else
			sb.append(targetExecutable);

		return sb.toString();
	}
}
