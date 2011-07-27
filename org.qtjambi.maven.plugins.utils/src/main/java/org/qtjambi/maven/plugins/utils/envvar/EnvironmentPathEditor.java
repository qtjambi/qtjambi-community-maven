package org.qtjambi.maven.plugins.utils.envvar;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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

	/**
	 * 
	 * @param s
	 * @param defaultOpClazz	Maybe null for OpPathAppend.class default
	 */
	public void addWithModifier(String s, Class<? extends EnvironmentPathOperation> defaultOpClazz) {
		EnvironmentPathOperation op = parseWithModifier(s, defaultOpClazz);
		if(op != null)
			add(op);
	}

	/**
	 * 
	 * @param s
	 * @param defaultOpClazz	Maybe null for OpPathAppend.class default
	 * @return
	 */
	public static EnvironmentPathOperation parseWithModifier(String s, Class<? extends EnvironmentPathOperation> defaultOpClazz) {
		EnvironmentPathOperation op = null;
		// Do special keys:
		//  "path.global.-FOOBAR"		remove
		//  "path.global.=FOOBAR"	set (removing existing list)
		//  "path.global.:FOOBAR"		append if not present (with File.pathSeparator)
		//  "path.global.+FOOBAR"		append always [default] (with File.pathSeparator)
		if(s.length() > 0) {
			char c = s.charAt(0);
			if(c == '-') {				// remove
				s = s.substring(1);		// remove prefix character
				op = new OpPathRemove(s);
			//} else if(c == '=') {		// set
			//	s = s.substring(1);		// remove prefix character
			//	op = new OpSet(s);
			} else if(c == ':') {		// append if not present (with File.pathSeparator)
				s = s.substring(1);		// remove prefix character
				op = new OpPathAppendIfUnique(s);
			} else if(c == '<') {		// prepend if not present (with File.pathSeparator)
				s = s.substring(1);		// remove prefix character
				op = new OpPathPrependIfUnique(s);
			} else if(c == '[') {		// prepend always (with File.pathSeparator)
				s = s.substring(1);		// remove prefix character
				op = new OpPathPrepend(s);
			} else if(c == '+') {
				s = s.substring(1);		// append always
				op = new OpPathPrepend(s);
			} else {
				if(defaultOpClazz == null)
					op = new OpPathAppend(s);
				else
					op = newInstance(s, defaultOpClazz);
			}
		}
		return op;
	}

	public static EnvironmentPathOperation newInstance(String s, Class<? extends EnvironmentPathOperation> defaultOpClazz) {
		EnvironmentPathOperation op = null;
		try {
			Constructor<? extends EnvironmentPathOperation> ctor = defaultOpClazz.getConstructor(String.class);
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
