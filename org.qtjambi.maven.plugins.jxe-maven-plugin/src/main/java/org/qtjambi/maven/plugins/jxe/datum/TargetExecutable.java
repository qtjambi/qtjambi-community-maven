package org.qtjambi.maven.plugins.jxe.datum;

public class TargetExecutable {
	private String[] includes;
	private String[] excludes;

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
}
