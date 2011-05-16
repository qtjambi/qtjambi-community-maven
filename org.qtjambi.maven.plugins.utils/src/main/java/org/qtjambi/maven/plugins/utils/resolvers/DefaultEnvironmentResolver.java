package org.qtjambi.maven.plugins.utils.resolvers;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.qtjambi.maven.plugins.utils.IEnvironmentResolver;
import org.qtjambi.maven.plugins.utils.Platform;
import org.qtjambi.maven.plugins.utils.envvar.EnvironmentEditor;
import org.qtjambi.maven.plugins.utils.envvar.EnvironmentPathEditor;
import org.qtjambi.maven.plugins.utils.envvar.OpPathAppend;
import org.qtjambi.maven.plugins.utils.envvar.OpSet;
import org.qtjambi.maven.plugins.utils.shared.Utils;

public class DefaultEnvironmentResolver implements IEnvironmentResolver {
	public static final String K_make = "make";

	public static final String K_PATH		 		= "PATH";
	public static final String K_LD_LIBRARY_PATH	= "LD_LIBRARY_PATH";
	public static final String K_DYLD_LIBRARY_PATH	= "DYLD_LIBRARY_PATH";
	public static final String K_CROSS_COMPILE		= "CROSS_COMPILE";

	private String commandMake;

	private EnvironmentPathEditor pathEditor;
	private EnvironmentEditor envvarEditor;

	protected Platform platform;

	public DefaultEnvironmentResolver(Platform platform) {
		this.platform = platform;
		commandMake = K_make;
		pathEditor = new EnvironmentPathEditor();
		envvarEditor = new EnvironmentEditor();
	}

	public void applyEnvironmentVariables(Map<String, String> envvar) {
		applyEnvironmentVariablesNoParentPrivate(envvar);
	}

	public void applyEnvironmentVariablesNoParent(Map<String, String> envvar) {
		applyEnvironmentVariablesNoParentPrivate(envvar);
	}

	private void applyEnvironmentVariablesNoParentPrivate(Map<String, String> envvar) {
		envvarEditor.apply(envvar);
		pathEditor.apply(envvar, K_PATH);
	}

	public String resolveCommand(File dir, String command) {
		if(command.indexOf(File.separator) >= 0 && dir != null && Utils.pathStringIsAbsolute(command) == false)
			command = dir.getAbsolutePath() + File.separator + command;

		return platform.makeExeFilename(command);
	}

	public String resolveCommandMake() {
		return commandMake;
	}

	public void setPathAppend(List<String> pathAppend) {
		if(pathAppend != null && pathAppend.size() > 0) {
			for(String s : pathAppend)
				pathEditor.add(new OpPathAppend(s));
		}
	}
	public void setLdLibraryPathAppend(List<String> ldLibraryPathAppend) {
		if(ldLibraryPathAppend != null && ldLibraryPathAppend.size() > 0) {
			for(String s : ldLibraryPathAppend)
				envvarEditor.add(K_LD_LIBRARY_PATH, new OpPathAppend(s));
		}
	}
	public void setDyldLibraryPathAppend(List<String> dyldLibraryPathAppend) {
		if(dyldLibraryPathAppend != null && dyldLibraryPathAppend.size() > 0) {
			for(String s : dyldLibraryPathAppend)
				envvarEditor.add(K_DYLD_LIBRARY_PATH, new OpPathAppend(s));
		}
	}
	public void setEnvvarMap(Map<String, String> envvarMap) {
		if(envvarMap != null && envvarMap.size() > 0) {
			for(Map.Entry<String,String> e : envvarMap.entrySet()) {
				String k = e.getKey();
				String v = e.getValue();
				if(k.equals(K_PATH) || k.equals(K_LD_LIBRARY_PATH) || k.equals(K_DYLD_LIBRARY_PATH))
					envvarEditor.add(k, new OpPathAppend(v));
				else
					envvarEditor.add(k, new OpSet(v));
			}
		}
	}

	public EnvironmentPathEditor getPathEditor() {
		return pathEditor;
	}
	public void setPathEditor(EnvironmentPathEditor pathEditor) {
		this.pathEditor = pathEditor;
	}

	public EnvironmentEditor getEnvvarEditor() {
		return envvarEditor;
	}
	public void setEnvvarEditor(EnvironmentEditor envvarEditor) {
		this.envvarEditor = envvarEditor;
	}
}
