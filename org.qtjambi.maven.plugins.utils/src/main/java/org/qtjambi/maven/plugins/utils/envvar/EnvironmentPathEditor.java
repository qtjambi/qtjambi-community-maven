package org.qtjambi.maven.plugins.utils.envvar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.qtjambi.maven.plugins.utils.shared.Utils;

public class EnvironmentPathEditor {
	private List<EnvironmentPathOperation> operationList;

	public EnvironmentPathEditor() {
		operationList = new ArrayList<EnvironmentPathOperation>();
	}

	public String edit(String key, String oldValue) {
		String[] oldValueA = Utils.safeStringArraySplit(oldValue, File.pathSeparator);
		for(EnvironmentPathOperation op : operationList)
			oldValueA = op.operationPath(key, oldValueA);
		if(oldValueA != null)
			return Utils.stringConcat(oldValueA, File.pathSeparator);
		return oldValue;
	}

	public void apply(Map<String, String> envvar, final String key) {
		if(operationList.isEmpty())
			return;
		String oldValue = envvar.get(key);
		String newValue = edit(key, oldValue);
		if(newValue != null)
			envvar.put(key, newValue);
		else
			envvar.remove(key);
	}

	public void add(EnvironmentPathOperation e) {
		operationList.add(e);
	}
	public void clear() {
		operationList.clear();
	}
	public int size() {
		return operationList.size();
	}
}
