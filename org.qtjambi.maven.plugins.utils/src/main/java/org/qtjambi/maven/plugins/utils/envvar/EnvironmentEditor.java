package org.qtjambi.maven.plugins.utils.envvar;

import java.util.HashMap;
import java.util.Map;

public class EnvironmentEditor {
	private Map<String, EnvironmentOperation[]> operationMap;
	private int count;

	public EnvironmentEditor() {
		operationMap = new HashMap<String, EnvironmentOperation[]>();
	}

	public String edit(String key, String oldValue) {
		EnvironmentOperation[] opA = operationMap.get(key);
		if(opA == null)
			return oldValue;
		for(EnvironmentOperation op : opA)
			oldValue = op.operation(key, oldValue);
		return oldValue;
	}

	public void apply(Map<String, String> envvar, final String key) {
		if(operationMap.isEmpty())
			return;
		String oldValue = envvar.get(key);
		String newValue = edit(key, oldValue);
		if(newValue != null)
			envvar.put(key, newValue);
		else
			envvar.remove(key);
	}
	public void apply(Map<String, String> envvar) {
		if(operationMap.isEmpty())
			return;
		for(String key : operationMap.keySet())
			apply(envvar, key);
	}

	public void add(String key, EnvironmentOperation e) {
		EnvironmentOperation[] opA = operationMap.get(key);
		if(opA != null) {
			EnvironmentOperation[] newOpA = new EnvironmentOperation[opA.length + 1];
			System.arraycopy(opA, 0, newOpA, 0, opA.length);
			newOpA[opA.length] = e;
			opA = newOpA;
		} else {
			opA = new EnvironmentOperation[] { e };
		}
		operationMap.put(key, opA);
		count++;
	}
	public void clear(String key) {
		EnvironmentOperation[] opA = operationMap.remove(key);
		if(opA != null)
			count -= opA.length;
	}
	public void clear() {
		operationMap.clear();
		count = 0;
	}
	public int size() {
		return count;
	}
}
