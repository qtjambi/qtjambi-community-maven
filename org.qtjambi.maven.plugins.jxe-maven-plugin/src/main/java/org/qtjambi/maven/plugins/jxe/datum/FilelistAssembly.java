package org.qtjambi.maven.plugins.jxe.datum;


public class FilelistAssembly {
	private String[] includes;
	private String[] excludes;
	private TargetExecutable[] targetExecutables;

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
	public TargetExecutable[] getTargetExecutables() {
		return targetExecutables;
	}
	public void setTargetExecutables(TargetExecutable[] targetExecutables) {
		this.targetExecutables = targetExecutables;
	}
}
