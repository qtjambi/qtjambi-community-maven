package org.qtjambi.buildtool.maven.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class Utils {
	public static String[] stringArraySplit(String value, String str) {
		List<String> list = new ArrayList<String>();
		int fromIndex = 0;
		int i;
		while((i = value.indexOf(str, fromIndex)) >= 0) {
			String oneValue = value.substring(fromIndex, i);
			list.add(oneValue);
			fromIndex = i + 1;
		}
		return list.toArray(new String[list.size()]);
	}

	public static String[] stringArrayRemove(String[] valueA, String str) {
		final int srcLength = valueA.length;
		String[] newValueA = new String[srcLength];
		int destI = 0;
		int srcI;
		for(srcI = 0; srcI < srcLength; srcI++) {
			String v = valueA[srcI];
			if(!v.equals(str))
				newValueA[destI++] = v;
		}
		if(srcI != destI) {
			valueA = new String[destI];
			System.arraycopy(newValueA, 0, valueA, 0, destI);
			return valueA;
		}
		return valueA;
	}

	public static boolean stringArrayContains(String[] valueA, String str) {
		for(String v : valueA) {
			if(v.equals(str))
				return true;
		}
		return false;
	}

	public static String[] stringArrayPrepend(String[] valueA, String str) {
		final int srcLength = valueA.length;
		String[] newValueA = new String[srcLength + 1];
		newValueA[0] = str;
		System.arraycopy(valueA, 0, newValueA, 1, srcLength);
		return newValueA;
	}

	public static String[] stringArrayAppend(String[] valueA, String str) {
		final int srcLength = valueA.length;
		String[] newValueA = new String[srcLength + 1];
		System.arraycopy(valueA, 0, newValueA, 0, srcLength);
		newValueA[srcLength] = str;
		return newValueA;
	}

	public static int countLeadingDigits(String s) {
		int count = 0;
		final int left = s.length();
		int i;
		for(i = 0; i < left; i++) {
			char c = s.charAt(i);
			if(c >= '0' && c <= '9')
				count++;
			else
				break;
		}
		return count;
	}

	public static void applyEnvVarMap(Map<String,String> dest, Map<String,String> overwriteMap) {
		for(Map.Entry<String, String> e : overwriteMap.entrySet()) {
			String k = e.getKey();
			String v = e.getValue();

			// Do special keys:
			//  "envvar.global.-FOOBAR"		unset
			//  "envvar.global.+FOOBAR"		append
			//  "envvar.global.:FOOBAR"		append with File.pathSeparator
			//  "envvar.global._FOOBAR"		append with space
			//  "envvar.global.=FOOBAR=FOO"	set (and default, "=" is not needed)
			if(k.length() > 0) {
				char c = k.charAt(0);
				if(c == '-') {				// unset
					k = k.substring(1);		// remove prefix character
					dest.remove(k);
				} else if(c == '+') {		// append
					k = k.substring(1);		// remove prefix character
					String value = dest.get(k);
					if(value != null)
						value += v;
					else
						value = v;
					dest.put(k, value);
				} else if(c == ':') {		// append with File.pathSeparator
					k = k.substring(1);		// remove prefix character
					String value = dest.get(k);
					if(value != null)
						value += File.pathSeparator + v;
					else
						value = v;
					dest.put(k, value);
				} else if(c == '_') {		// append with space
					k = k.substring(1);		// remove prefix character
					String value = dest.get(k);
					if(value != null)
						value += " " + v;
					else
						value = v;
					dest.put(k, value);
				} else /*if(c == '=')*/ {		// set (like default)
					dest.put(k, v);
				}
			} else {
				dest.put(k, v);
			}

		}
	}

	public static void applyEnvVarPath(Map<String,String> dest, String name, List<String> overwriteList) {
		String value = dest.get(name);
		String[] valueA = new String[0];
		if(value != null)
			valueA = Utils.stringArraySplit(value, File.pathSeparator);
		for(String s : overwriteList) {
			// Do special keys:
			//  "path.global.-FOOBAR"		unset
			//  "path.global.=FOOBAR"	set (removing existing list)
			//  "path.global.:FOOBAR"		append if not present (with File.pathSeparator)
			//  "path.global.+FOOBAR"		append always [default] (with File.pathSeparator)
			if(s.length() > 0) {
				char c = s.charAt(0);
				if(c == '-') {				// unset
					s = s.substring(1);		// remove prefix character
					valueA = Utils.stringArrayRemove(valueA, s);
				} else if(c == '=') {		// set
					valueA = new String[1];
					valueA[0] = s;
				} else if(c == ':') {		// append if not present (with File.pathSeparator)
					s = s.substring(1);		// remove prefix character
					if(Utils.stringArrayContains(valueA, s) == false)
						valueA = Utils.stringArrayAppend(valueA, s);
				} else if(c == '<') {		// prepend if not present (with File.pathSeparator)
					s = s.substring(1);		// remove prefix character
					if(Utils.stringArrayContains(valueA, s) == false)
						valueA = Utils.stringArrayPrepend(valueA, s);
				} else if(c == '[') {		// prepend always (with File.pathSeparator)
					s = s.substring(1);		// remove prefix character
					valueA = Utils.stringArrayPrepend(valueA, s);
				} else /*if(c == '+')*/ {	// append always (like default)
					s = s.substring(1);		// remove prefix character
					valueA = Utils.stringArrayAppend(valueA, s);
				}
			} else {
				valueA = Utils.stringArrayAppend(valueA, s);
			}
		}
		if(valueA != null)
			value = Utils.resolveFilePathSeparator(valueA);
		else
			value = null;
		if(value != null)
			dest.put(name, value);
		else
			dest.remove(name);
	}

	public static String resolveFileSeparator(String[] sA) {
		return stringConcat(sA, File.separator);
	}

	public static String resolveFilePathSeparator(String[] sA) {
		return stringConcat(sA, File.pathSeparator);
	}

	public static String stringConcat(String[] sA, String sep) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for(String s : sA) {
			if(first)
				first = false;
			else
				sb.append(sep);
			sb.append(s);
		}
		return sb.toString();
	}


	/**
	 * Be careful using this.
	 * @param dir
	 * @return
	 */
	public static boolean deleteRecursive(File dir) {
		boolean bf = true;
		File[] fileA = dir.listFiles();
		for(File f : fileA) {
			if(f.isDirectory()) {
				if(deleteRecursive(f) == false)
					bf = false;
			}
			try {
				System.out.println("delete: " + f.getAbsolutePath());
				if(f.delete() == false)
					bf = false;
			} catch (SecurityException e) {
				bf = false;
			}
		}
		return bf;
	}

	public static List<String> safeListStringAppend(List<String> list, String s) {
		if(list == null)
			list = new ArrayList<String>();
		list.add(s);
		return list;
	}
}
