package org.qtjambi.maven.plugins.jxe;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

/**
 * The purpose of this descriptor is to allow any JXE item to be consumed by Maven.
 * @author <a href="mailto:darryl.miles@darrylmiles.org">Darryl L. Miles</a>
 */
public class JxeInfo {
	public static final String JXE_PROPERTIES = "jxe.properties";

	private String groupId, artifactId, version;

	private Properties info;

	private Log log;

	public JxeInfo(String groupId, String artifactId, String version, Log log) throws MojoExecutionException {
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		this.log = log;
		info = new Properties();

		// Fill with general properties.jxe file
		File propertiesDir = new File("src/main/resources/META-INF/jxe/" + groupId + "/" + artifactId);
		File propertiesFile = new File(propertiesDir, JxeInfo.JXE_PROPERTIES);
		try {
			info.load(new FileInputStream(propertiesFile));
		} catch (FileNotFoundException e) {
			// ignored
		} catch (IOException e) {
			throw new MojoExecutionException("Problem loading " + propertiesFile, e);
		}
	}

	@SuppressWarnings("rawtypes")
	public String toString() {
		StringBuffer s = new StringBuffer("JxeInfo for ");
		s.append(groupId);
		s.append(":");
		s.append(artifactId);
		s.append("-");
		s.append(version);
		s.append(" {\n");

		for (Iterator i = info.keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			s.append("   ");
			s.append(key);
			s.append("='");
			s.append(info.getProperty(key, "<null>"));
			s.append("'\n");
		}

		s.append("}\n");
		return s.toString();
	}

	public boolean exists(JarFile jar) {
		return getJxePropertiesEntry(jar) != null;
	}

	public void read(JarFile jar) throws IOException {
		info.load(jar.getInputStream(getJxePropertiesEntry(jar)));
	}

	private JarEntry getJxePropertiesEntry(JarFile jar) {
		return jar.getJarEntry("META-INF/jxe/" + groupId + "/" + artifactId + "/" + JXE_PROPERTIES);
	}

	public void writeToFile(File file) throws IOException {
		info.store(new FileOutputStream((file)), "JXE Properties for " + groupId + "." + artifactId + "-" + version);
	}

	public void setProperty(String key, String value) {
		info.setProperty(key, value);
	}

	public String getProperty(String key, String defaultValue) {
		if(key == null)
			return defaultValue;
		String value = info.getProperty(key, defaultValue);
		log.debug("getProperty(" + key + ", " + defaultValue + ") = " + value);
		return value;
	}
}
