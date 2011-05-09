package org.qtjambi.maven.plugins.jxe.datum;

import org.qtjambi.maven.plugins.utils.shared.Utils;

public class TargetExecutable implements IFileSet {
	private String[] includes;
	private String[] excludes;

	public TargetExecutable() {
		includes = new String[0];
		excludes = new String[0];
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

	public final String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("includes=[");
		sb.append(Utils.debugStringArrayPretty(includes));
		sb.append("], excludes=[");
		sb.append(Utils.debugStringArrayPretty(excludes));
		sb.append("]");
		return sb.toString();
	}
}
