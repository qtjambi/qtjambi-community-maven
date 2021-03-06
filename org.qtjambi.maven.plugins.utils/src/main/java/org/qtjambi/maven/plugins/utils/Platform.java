package org.qtjambi.maven.plugins.utils;

import org.apache.maven.plugin.logging.Log;

public class Platform {
	private boolean initDone;
	private String osName;
	private String osNameCanon;
	private String osArch;
	private String osArchCanon;
	private String osVersion;
	private String exeSuffix;
	private String batSuffix;
	private String shSuffix;
	private Toolchain toolchain;

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
		if(osName != null)
			osNameCanon = osName.toLowerCase();
		else
			osNameCanon = null;

		osArch = System.getProperty("os.arch");
		if(log != null)
			log.debug(" system.property.os.arch=" + osArch);
		if(osArch != null)
			osArchCanon = osArch.toLowerCase();
		else
			osArchCanon = null;

		osVersion = System.getProperty("os.version");
		if(log != null)
			log.debug(" system.property.os.version=" + osVersion);

		initStageTwo(log);
		initDone = true;
	}

	private void initStageTwo(Log log) {
		if(osNameCanon != null) {
			// Windows 2000
			// Windows XP
			// Windows Vista
			// Windows 7
			if(osNameCanon.startsWith("windows")) {
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
		// Trying not to check for it already (rather fix the source of the problem)
		if(exeSuffix != null /*&& filename.endsWith(exeSuffix) == false*/)
			return filename + exeSuffix;
		return filename;
	}

	public IEnvironmentResolver environmentResolverWithToolchain() {
		switch(toolchain) {
		case gcc:
			return getGccEnvironmentResolver();
		case mingw:
			return getMingwEnvironmentResolver();
		case mingw_w64:
			return getMingwW64EnvironmentResolver();
		case msvc:
			return getMsvcEnvironmentResolver();
		}
		return null;
	}

	public boolean isLinux(boolean andUnsure) {
		if(osNameCanon == null)
			return andUnsure;
		if(osNameCanon.startsWith("linux"))
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
		if(osNameCanon == null)
			return andUnsure;
		if(osNameCanon.startsWith("windows"))
			return true;
		return false;
	}
	public boolean isWindows32(boolean andUnsure) {
		if(osArchCanon == null)
			return andUnsure;
		//if(osArch.equals("amd64"))
			return true;
		//return false;
	}
	public boolean isWindows64(boolean andUnsure) {
		if(osArchCanon == null)
			return andUnsure;
		if(osArchCanon.equals("amd64"))
			return true;
		return false;
	}

	public boolean isMacosx(boolean andUnsure) {
		if(osNameCanon == null)
			return andUnsure;
		if(osNameCanon.startsWith("mac os x") || osName.startsWith("macosx"))	// CHECKME FIXME
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

	public Toolchain getToolchain() {
		return toolchain;
	}
	public void setToolchain(Toolchain toolchain) {
		this.toolchain = toolchain;
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
