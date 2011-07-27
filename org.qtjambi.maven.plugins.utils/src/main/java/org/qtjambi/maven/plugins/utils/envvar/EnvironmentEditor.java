package org.qtjambi.maven.plugins.utils.envvar;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class EnvironmentEditor {
	private Map<String, EnvironmentOperation[]> operationMap;
	private int count;

	// FIXME: Audit the code for handling Windows case-insensitive envvar names
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
	public void addPathEditorWithModifier(String key, EnvironmentPathOperation e) {
		add(key, (EnvironmentOperation)e);
	}
	public void addPathEditorWithModifier(String key, String s, Class<? extends EnvironmentPathOperation> defaultOpClazz) {
		EnvironmentPathOperation e = EnvironmentPathEditor.parseWithModifier(s, defaultOpClazz);
		if(e instanceof EnvironmentOperation)	// FIXME: Clean this up OOP!
			add(key, (EnvironmentOperation)e);
	}
	public void addPathEditorWithModifier(String key, String s) {
		addPathEditorWithModifier(key, s, null);
	}
	public void remove(String key) {
		operationMap.remove(key);
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

	/**
	 * 
	 * @param key
	 * @param s
	 * @param defaultOpClazz	Maybe null for OpAppend.class default
	 * @return
	 * @see #addPathEditorWithModifier(String, String)
	 */
	public void addWithModifier(String key, String s, Class<? extends EnvironmentOperation> defaultOpClazz) {
		EnvironmentOperation e = parseWithModifier(s, defaultOpClazz);
		if(e != null)
			add(key, e);
	}

	/**
	 * 
	 * @param key
	 * @param s
	 * @see #addWithModifier(String, String, Class)
	 */
	public void addWithModifier(String key, String s) {
		addWithModifier(key, s, null);
	}

	/**
	 * 
	 * @param s
	 * @param defaultOpClazz	Maybe null for OpAppend.class default
	 * @return
	 */
	public static EnvironmentOperation parseWithModifier(String s, Class<? extends EnvironmentOperation> defaultOpClazz) {
		EnvironmentOperation op = null;
		// Do special keys:
		//  "envvar.global.-FOOBAR"		remove
		//  "envvar.global.:FOOBAR"		append
		//  "envvar.global.:FOOBAR"		set if not set
		//  "envvar.global.=FOOBAR"		set always [default]
		if(s.length() > 0) {
			char c = s.charAt(0);
			if(c == '-') {				// remove
				s = s.substring(1);		// remove prefix character
				op = new OpUnset(s);
			} else if(c == '+') {		// set
				s = s.substring(1);		// remove prefix character
				op = new OpAppend(s);
			} else if(c == ':') {		// append if not present (with File.pathSeparator)
				s = s.substring(1);		// remove prefix character
				op = new OpSetIfUnset(s);
			} else if(c == '=') {
				s = s.substring(1);		// remove prefix character
				op = new OpSet(s);
			} else {	// append always (like default)
				if(defaultOpClazz == null)
					op = new OpSet(s);
				else
					op = newInstance(s, defaultOpClazz);
			}
		}
		return op;
	}

	public static EnvironmentOperation newInstance(String s, Class<? extends EnvironmentOperation> defaultOpClazz) {
		EnvironmentOperation op = null;
		try {
			Constructor<? extends EnvironmentOperation> ctor = defaultOpClazz.getConstructor(String.class);
			op = ctor.newInstance(s);
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		} catch (IllegalArgumentException e) {
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		}
		return op;
	}
}
