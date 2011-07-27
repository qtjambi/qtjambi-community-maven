package org.qtjambi.maven.plugins.utils.shared;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.qtjambi.maven.plugins.utils.Toolchain;

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
		int rem = value.length() - fromIndex;
		if(rem > 0)
			list.add(value.substring(fromIndex));
		return list.toArray(new String[list.size()]);
	}

	public static String[] safeStringArraySplit(String value, String str) {
		if(value == null)
			return null;
		return stringArraySplit(value, str);
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

	public static String[] safeStringArrayPrepend(String[] valueA, String str) {
		if(valueA != null)
			return stringArrayPrepend(valueA, str);
		return new String[] { str };
	}

	public static String[] stringArrayAppend(String[] valueA, String str) {
		final int srcLength = valueA.length;
		String[] newValueA = Arrays.copyOf(valueA, srcLength + 1);
		return newValueA;
	}

	public static String[] safeStringArrayAppend(String[] valueA, String str) {
		if(valueA != null)
			return stringArrayAppend(valueA, str);
		return new String[] { str };
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
				} else {	// append always (like default)
					if(c == '+')
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

	public static String stringConcat(Object[] oA, String sep) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for(Object o : oA) {
			if(first)
				first = false;
			else
				sb.append(sep);
			sb.append(o.toString());
		}
		return sb.toString();
	}

	/**
	 * 
	 * @param path "C:\foo;D:\bar"
	 * @param separator "\"
	 * @return new String[] { "C:\foo", "D:\bar" }
	 */
	public static String[] stringSplit(final String path, final String separator) {
		// FIXME: Move to Util class
		List<String> list = new ArrayList<String>();
		int i = 0;
		int idx;
		while((idx = path.indexOf(separator, i)) >= 0) {
			String s = path.substring(i, idx);
			list.add(s);
			idx += separator.length();
			i = idx;
		}
		return list.toArray(new String[list.size()]);
	}

	/**
	 * Be careful using this.
	 * @param dir
	 * @return
	 */
	public static boolean deleteRecursive(File dir) {
		// PARANOID CHECK
		String pathAsString = dir.getAbsolutePath();
		if(pathAsString.length() == 0)
			return false;
		if(pathAsString.startsWith(File.separator) == false) {
			// FIXME: Do platform check, startWith() for linux/macosx, driver letter check as well for windows
			if(pathAsString.length() < 3 || (pathAsString.charAt(1) != ':' && pathAsString.charAt(2) != File.separatorChar))
				throw new RuntimeException("deleteRecursive(): " + dir.getAbsolutePath());
		}
		{
			int i, j;
			i = pathAsString.indexOf(File.separator);
			j = pathAsString.lastIndexOf(File.separator);
			if(i == j || (j - i) < 2)
				throw new RuntimeException("deleteRecursive(): " + dir.getAbsolutePath());
		}
		// PARANOID CHECK
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

	public static final String K_gcc 		= "gcc";
	public static final String K_mingw 		= "mingw";
	public static final String K_mingw_w64 	= "mingw_w64";	// preferred
	public static final String K_mingw__w64 = "mingw-w64";
	public static final String K_msvc 		= "msvc";

	public static Toolchain toolchainFromLabel(String s) {
		String mainString;
		String rest = null;
		int dotIndex = s.indexOf('.');
		if(dotIndex >= 0) {
			mainString = s.substring(0, dotIndex);
			if(s.length() > dotIndex)
				rest = s.substring(dotIndex + 1);
		} else {
			mainString = s;
		}
		if(mainString.equalsIgnoreCase(K_gcc))
			return Toolchain.gcc;
		if(mainString.equalsIgnoreCase(K_mingw))
			return Toolchain.mingw;
		if(mainString.equalsIgnoreCase(K_mingw_w64) || mainString.equalsIgnoreCase(K_mingw__w64))
			return Toolchain.mingw_w64;
		if(mainString.equalsIgnoreCase(K_msvc))
			return Toolchain.msvc;
		return null;
	}

	public static String toolchainKindFromLabel(String s) {
		String mainString;
		String rest = null;
		int dotIndex = s.indexOf('.');
		if(dotIndex >= 0) {
			mainString = s.substring(0, dotIndex);
			if(s.length() > dotIndex)
				rest = s.substring(dotIndex + 1);
		} else {
			mainString = s;
		}
		return rest;
	}

	public static String toolchainToLabel(Toolchain t) {
		switch(t) {
		case gcc:
			return K_gcc;
		case mingw:
			return K_mingw;
		case mingw_w64:
			return K_mingw_w64;
		case msvc:
			return K_msvc;
		}
		return null;
	}

	public static String debugStringArrayPretty(Object[] oA) {
		if(oA == null)
			return "null";
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		boolean first = true;
		for(Object o : oA) {
			if(first)
				first = false;
			else
				sb.append(", ");
			sb.append(o.toString());
		}
		sb.append("]");
		return sb.toString();
	}

	public static String debugStringMapPretty(Map map) {
		if(map == null)
			return "null";
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		boolean first = true;
		for(Map.Entry entry : ((Map<Object,Object>)map).entrySet()) {
			if(first)
				first = false;
			else
				sb.append(", ");

			Object key = entry.getKey();
			String keyString = "null";
			if(key != null)
				keyString = key.toString();

			Object value = entry.getValue();
			String valueString = "null";
			if(value != null)
				valueString = value.toString();

			sb.append(keyString + " => " + valueString);
		}
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Removes trailing (and excessive trailing) directory separators.
	 * @param path
	 * @return
	 */
	public static String pathCanonTrailing(String path) {
		final String fileSeparator = File.separator;
		final int fileSeparatorLen = fileSeparator.length();
		while(true) {
			int len = path.length();
			if(len >= fileSeparatorLen) {
				String s = path.substring(len - fileSeparatorLen);
				if(s.compareTo(fileSeparator) == 0) {
					path = path.substring(0, len - fileSeparatorLen);
					continue;		// try again
				}
			}
			break;
		}
		return path;
	}

	/**
	 * #stringReplaceShuffleChar("sub/dir/foo_bar__xyz.pro", '/', '_', 1) => "sub__dir__foo_bar___xyz.pro"
	 * #stringReplaceShuffleChar("sub/dir/_foo_bar__xyz.pro", '/', '_', 1) => "sub__dir___foo_bar___xyz.pro"
	 * #stringReplaceShuffleChar("sub/dir_/foo_bar__xyz.pro", '/', '_', 1) => "sub__dir___foo_bar___xyz.pro"
	 * #stringReplaceShuffleChar("sub/dir_/_foo_bar__xyz.pro", '/', '_', 1) => "sub__dir____foo_bar___xyz.pro"
	 * This is not designed to be reversible just one-way uniqueness.
	 * @param s
	 * @param rc		Replace character
	 * @param sc		Substitute character
	 * @param atPosition
	 * @return
	 */
	public static String stringReplaceShuffleChar(String s, final char rc, final char sc, final int atPosition) {
		final int sLen = s.length();
		StringBuffer sb = new StringBuffer();
		int contig = 0;
		for(int i = 0; i < sLen; i++) {
			char ch = s.charAt(i);
			if(contig > 0) {
				if(ch == sc) {
					if(atPosition == contig)
						sb.append(sc);
					contig++;
					sb.append(ch);
				} else if(ch == rc) {
					for(int j = 0; j < atPosition; j++)
						sb.append(sc);
					sb.append(sc);	// and one extra
					contig = 0;
					// drop 'ch'
				} else {
					contig = 0;
					sb.append(ch);
				}
			} else {
				if(ch == sc) {
					if(atPosition == contig)
						sb.append(sc);
					contig++;
					sb.append(ch);
				} else if(ch == rc) {
					for(int j = 0; j < atPosition; j++)
						sb.append(sc);
					sb.append(sc);	// and one extra
					contig = 0;
					// drop 'ch'
				} else {
					sb.append(ch);
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Search for an existing file that is executable in the envvarPath spec.
	 * @param envvarPath
	 * @param relExePath
	 * @return
	 */
	public static String searchPath(String envvarPath, String relExePath) {
		String[] envvarPathA = new String[0];
		if(envvarPath != null)
			envvarPathA = Utils.stringArraySplit(envvarPath, File.pathSeparator);
		for(String s : envvarPathA) {
			File file = new File(s, relExePath);
			if(file.exists() && file.isFile() && invokeFileCanExecuteDefault(file, true))
				return file.getAbsolutePath();
		}
		return null;
	}

	public static String safeStringPathAppend(String pathString, String s) {
		if(pathString != null) {
			String[] pathA = stringArraySplit(pathString, File.pathSeparator);
			pathA = stringArrayAppend(pathA, s);
			pathString = stringConcat(pathA, File.pathSeparator);
		} else {
			return s;
		}
		return pathString;
	}

	public static boolean pathStringIsAbsolute(String pathAsString) {
		if(File.separatorChar == '/') {		// linux && macosx
			if(pathAsString.startsWith(File.separator) == false)
				return true;
		}
		if(File.separatorChar == '\\') {	// windows
			// FIXME: Do platform check, startWith() for linux/macosx, driver letter check as well for windows
			if(pathAsString.length() >= 3) {
				char c = pathAsString.charAt(0);
				if((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
					if(pathAsString.charAt(1) == ':' && pathAsString.charAt(2) == File.separatorChar)
						return true;
				}
			}
		}
		return false;
	}

	/**
	 * QtJambi support JDK5, and this was only introduced in JDK6, so this
	 *  is forward looking implementation of {@link File#setExecutable(boolean)}
	 * @param file
	 * @param executable
	 * @return
	 * @see File#setExecutable(boolean)
	 */
	public static Boolean invokeFileSetExecutable(File file, boolean executable) {
		Boolean rv = null;
		if(rv == null) {
			try {
				// rv = file.setExecutable(executable);
				Method method = file.getClass().getMethod("setExecutable", boolean.class);
				Object rvObj = method.invoke(file, Boolean.valueOf(executable));
				if(rvObj instanceof Boolean) {
					rv = (Boolean) rvObj;
				}
			} catch (SecurityException e) {
			} catch (NoSuchMethodException e) {
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			} catch (InvocationTargetException e) {
			}
		}
		if(rv == null && new File("/bin/chmod").exists()) {
			try {
				Process process = Runtime.getRuntime().exec(new String[] { "/bin/chmod", "+x", file.getAbsolutePath() });
				try {
					process.getOutputStream().close();
				} catch(IOException eat) {
				}
				// FIXME: Should probably sink stdout/stderr
				try {
					process.getInputStream().close();
				} catch(IOException eat) {
				}
				try {
					process.getErrorStream().close();
				} catch(IOException eat) {
				}
				int exitValue = process.exitValue();
				if(exitValue == 0)
					rv = Boolean.TRUE;
				else
					rv = Boolean.FALSE;
			} catch (Exception e) {
			}
		}
		return rv;
	}

	/**
	 * QtJambi support JDK5, and this was only introduced in JDK6, so this
	 *  is forward looking implementation of {@link File#canExecutable()}
	 * @param file
	 * @return
	 * @see File#canExecute()
	 */
	public static Boolean invokeFileCanExecute(File file) {
		Boolean rv = null;
		if(rv == null) {
			try {
				// rv = file.canExecute();
				Method method = file.getClass().getMethod("canExecute", (Class<?> []) null);
				Object rvObj = method.invoke(file, (Object[]) null);
				if(rvObj instanceof Boolean) {
					rv = (Boolean) rvObj;
				}
			} catch (SecurityException e) {
			} catch (NoSuchMethodException e) {
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			} catch (InvocationTargetException e) {
			}
		}
		if(rv == null && new File("/bin/sh").exists()) {
			try {
				Process process = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", "test -x \"" + file.getAbsolutePath() + "\"" });
				try {
					process.getOutputStream().close();
				} catch(IOException eat) {
				}
				// FIXME: Should probably sink stdout/stderr
				try {
					process.getInputStream().close();
				} catch(IOException eat) {
				}
				try {
					process.getErrorStream().close();
				} catch(IOException eat) {
				}
				int exitValue = process.exitValue();
				if(exitValue == 0)
					rv = Boolean.TRUE;
				else
					rv = Boolean.FALSE;
			} catch (Exception e) {
			}
		}
		return rv;
	}

	public static boolean invokeFileCanExecuteDefault(File file, boolean defaultValue) {
		Boolean rv = invokeFileCanExecute(file);
		if(rv != null)
			return rv.booleanValue();
		return defaultValue;
	}
}
