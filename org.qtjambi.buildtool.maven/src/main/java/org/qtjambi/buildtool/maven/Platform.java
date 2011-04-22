package org.qtjambi.buildtool.maven;

import org.apache.maven.plugin.logging.Log;

public class Platform {
	private boolean initDone;
	private String osName;
	private String osArch;
	private String osVersion;
	private String exeSuffix;
	private String batSuffix;
	private String shSuffix;

	private IEnvironmentResolver globalEnvironmentResolver;
	private IEnvironmentResolver gccEnvironmentResolver;
	private IEnvironmentResolver javaEnvironmentResolver;
	private IEnvironmentResolver mingwEnvironmentResolver;
	private IEnvironmentResolver mingwW64EnvironmentResolver;
	private IEnvironmentResolver msvcEnvironmentResolver;
	private IEnvironmentResolver qtEnvironmentResolver;

	public void detect(Log log) {
		init(log);
	}

	private void init(Log log) {
		if(initDone)
			return;
		osName = System.getProperty("os.name");
		if(log != null)
			log.debug(" system.property.os.name=" + osName);

		osArch = System.getProperty("os.arch");
		if(log != null)
			log.debug(" system.property.os.arch=" + osArch);

		osVersion = System.getProperty("os.version");
		if(log != null)
			log.debug(" system.property.os.version=" + osVersion);

		initStageTwo(log);
		initDone = true;
	}

	private void initStageTwo(Log log) {
		if(osName != null) {
			// Windows 2000
			// Windows XP
			// Windows Vista
			// Windows 7
			if(osName.startsWith("Windows ")) {
				exeSuffix = ".exe";
				if(log != null)
					log.debug(" setting exeSuffix=" + exeSuffix);

				batSuffix = ".bat";
				if(log != null)
					log.debug(" setting batSuffix=" + batSuffix);
			} else {
				shSuffix = ".sh";
				if(log != null)
					log.debug(" setting shSuffix=" + shSuffix);
			}
		}
	}

	public String makeScriptingFilename(String filename) {
		init(null);
		if(shSuffix != null)
			return filename + shSuffix;
		if(batSuffix != null)
			return filename + batSuffix;
		return filename;
	}

	public String makeExeFilename(String filename) {
		init(null);
		if(exeSuffix != null)
			return filename + exeSuffix;
		return filename;
	}

	public boolean isLinux(boolean andUnsure) {
		if(osName == null)
			return andUnsure;
		if(osName.startsWith("Linux"))
			return true;
		return false;
	}
	public boolean isLinux32(boolean andUnsure) {
		return true;		// FIXME
	}
	public boolean isLinux64(boolean andUnsure) {
		return true;		// FIXME
	}

	public boolean isWindows(boolean andUnsure) {
		if(osName == null)
			return andUnsure;
		if(osName.startsWith("Windows "))
			return true;
		return false;
	}
	public boolean isWindows32(boolean andUnsure) {
		if(osArch == null)
			return andUnsure;
		//if(osArch.equals("amd64"))
			return true;
		//return false;
	}
	public boolean isWindows64(boolean andUnsure) {
		if(osArch == null)
			return andUnsure;
		if(osArch.equals("amd64"))
			return true;
		return false;
	}

	public boolean isMacosx(boolean andUnsure) {
		if(osName == null)
			return andUnsure;
		if(osName.startsWith("Macosx"))	// CHECKME FIXME
			return true;
		return false;
	}
	public boolean isMacosxIntel(boolean andUnsure) {
		return true;		// FIXME
	}
	public boolean isMacosxPpc(boolean andUnsure) {
		return true;		// FIXME
	}

	public String getOsName() {
		return osName;
	}
	public String getOsArch() {
		return osArch;
	}
	public String getOsVersion() {
		return osVersion;
	}

	public IEnvironmentResolver getGlobalEnvironmentResolver() {
		return globalEnvironmentResolver;
	}
	public void setGlobalEnvironmentResolver(IEnvironmentResolver globalEnvironmentResolver) {
		this.globalEnvironmentResolver = globalEnvironmentResolver;
	}

	public IEnvironmentResolver getGccEnvironmentResolver() {
		return gccEnvironmentResolver;
	}
	public void setGccEnvironmentResolver(IEnvironmentResolver gccEnvironmentResolver) {
		this.gccEnvironmentResolver = gccEnvironmentResolver;
	}

	public IEnvironmentResolver getJavaEnvironmentResolver() {
		return javaEnvironmentResolver;
	}
	public void setJavaEnvironmentResolver(IEnvironmentResolver javaEnvironmentResolver) {
		this.javaEnvironmentResolver = javaEnvironmentResolver;
	}

	public IEnvironmentResolver getMingwEnvironmentResolver() {
		return mingwEnvironmentResolver;
	}
	public void setMingwEnvironmentResolver(IEnvironmentResolver mingwEnvironmentResolver) {
		this.mingwEnvironmentResolver = mingwEnvironmentResolver;
	}

	public IEnvironmentResolver getMingwW64EnvironmentResolver() {
		return mingwW64EnvironmentResolver;
	}
	public void setMingwW64EnvironmentResolver(IEnvironmentResolver mingwW64EnvironmentResolver) {
		this.mingwW64EnvironmentResolver = mingwW64EnvironmentResolver;
	}

	public IEnvironmentResolver getMsvcEnvironmentResolver() {
		return msvcEnvironmentResolver;
	}
	public void setMsvcEnvironmentResolver(IEnvironmentResolver msvcEnvironmentResolver) {
		this.msvcEnvironmentResolver = msvcEnvironmentResolver;
	}

	public IEnvironmentResolver getQtEnvironmentResolver() {
		return qtEnvironmentResolver;
	}
	public void setQtEnvironmentResolver(IEnvironmentResolver qtEnvironmentResolver) {
		this.qtEnvironmentResolver = qtEnvironmentResolver;
	}
}
