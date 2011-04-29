package org.qtjambi.maven.plugins.utils.shared;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public class MojoExceptionHelper<T extends Exception> {
	/**
	 * Rethrow Mojo related exceptions as-is or raise one based on the generics type.
	 *  throw new MojoExceptionHelper<MojoFoobarException>().rethrow(e);
	 * If 'e' is already a Mojo exception we rethrow it otherwise we wrap it in one.
	 * @param e
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 */
	public T rethrow(Exception e) throws MojoExecutionException, MojoFailureException {
		tryToRethrow(e);
		Class<T> clazz = getGenericClass();

		T ne = newInstance(clazz, e, e.getMessage(), e.getMessage());
		if(ne != null)
			return ne;

		ne = newInstance(clazz);
		return ne;
	}

	/**
	 * Rethrow if it is already known exception type, otherwise return control to caller.
	 * @param e
	 * @throws MojoExecutionException
	 * @throws MojoFailureException
	 */
	public static void tryToRethrow(Exception e) throws MojoExecutionException, MojoFailureException {
		if(e instanceof MojoExecutionException) {
			MojoExecutionException mee = (MojoExecutionException)e;
			throw mee;
		}
		if(e instanceof MojoFailureException) {
			MojoFailureException mfe = (MojoFailureException)e;
			throw mfe;
		}
	}

	@SuppressWarnings("unchecked")
	private Class<T> getGenericClass() {
		Type type = getClass().getGenericSuperclass();
		ParameterizedType paramType = (ParameterizedType)type;
		return (Class<T>) paramType.getActualTypeArguments()[0];
	}

	private T newInstance(Class<T> clazz, Object source, String shortMessage, String longMessage) {
		try {
			Constructor<T> ctor = clazz.getConstructor(new Class[] { Object.class, String.class, String.class });
			return ctor.newInstance(new Object[] { source, shortMessage, longMessage });
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		} catch (IllegalArgumentException e) {
		} catch (InvocationTargetException e) {
		}
		return null;
	}

	private T newInstance(Class<T> clazz) {
		try {
			return clazz.newInstance();
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
		}
		return null;
	}
}
