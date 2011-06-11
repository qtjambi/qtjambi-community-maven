/****************************************************************************
**
** Copyright (C) 1992-2009 Nokia. All rights reserved.
** Copyright (C) 2011 Darryl L. Miles.
**
** This file is part of Qt Jambi.
**
** ** $BEGIN_LICENSE$
** GNU Lesser General Public License Usage
** Alternatively, this file may be used under the terms of the GNU Lesser
** General Public License version 2.1 as published by the Free Software
** Foundation and appearing in the file LICENSE.LGPL included in the
** packaging of this file.  Please review the following information to
** ensure the GNU Lesser General Public License version 2.1 requirements
** will be met: http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html.
** 
** In addition, as a special exception, Nokia gives you certain
** additional rights. These rights are described in the Nokia Qt LGPL
** Exception version 1.0, included in the file LGPL_EXCEPTION.txt in this
** package.
** 
** GNU General Public License Usage
** Alternatively, this file may be used under the terms of the GNU
** General Public License version 3.0 as published by the Free Software
** Foundation and appearing in the file LICENSE.GPL included in the
** packaging of this file.  Please review the following information to
** ensure the GNU General Public License version 3.0 requirements will be
** met: http://www.gnu.org/copyleft/gpl.html.
** 
** If you are unsure which license is appropriate for your use, please
** contact the sales department at qt-sales@nokia.com.
** $END_LICENSE$

**
** This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
** WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
**
****************************************************************************/

package org.qtjambi.maven.plugins.generator;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoFailureException;
import org.qtjambi.maven.plugins.utils.envvar.EnvironmentEditor;
import org.qtjambi.maven.plugins.utils.envvar.EnvironmentPathEditor;
import org.qtjambi.maven.plugins.utils.envvar.OpPathPrepend;
import org.qtjambi.maven.plugins.utils.shared.Utils;

public class GeneratorTask {
	// Mandatory arguments
	private File headerFile;
	private File typesystemFile;

	// Optional arguments
	private File phononInclude;
	private File qtIncludeDirectory;
	private File[] includePaths;
	private File inputDirectory;
	private File outputDirectory;
	private File cppOutputDirectory;
	private File javaOutputDirectory;

	private File qtLibDirectory;		// This is more for LD_LIBRARY_PATH editing

	private String[] options;

	private List<String> commandList = new ArrayList<String>();

	private File workingDirectory;
	// Windows: { "generator\\release", "generator\\debug" }
	// else: { "./generator" }
	private List<String> jxeSearchPath = new ArrayList<String>();

	public void setOptions(String[] options) {
		this.options = options;
	}
	public String[] getOptions() {
		return options;
	}

	private void checkMandatoryArguments(List<String> commandList) throws MojoFailureException {
		if(headerFile == null || headerFile.exists()) {
			throw new MojoFailureException("Header file '" + headerFile + "' does not exist.");
		}

		if(typesystemFile == null || typesystemFile.exists()) {
			throw new MojoFailureException("Typesystem file '" + typesystemFile + "' does not exist.");
		}

		commandList.add(headerFile.getAbsolutePath());
		commandList.add(typesystemFile.getAbsolutePath());
	}

	private boolean parseArguments() throws MojoFailureException {
		if(phononInclude != null) {
			commandList.add("--phonon-include=" + phononInclude);
		}

		if(qtIncludeDirectory != null) {
			commandList.add("--qt-include-directory=" + qtIncludeDirectory);
		}

		if(qtLibDirectory != null) {
			commandList.add("--qt-lib-directory=" + qtLibDirectory);
		}

		if(includePaths != null) {
			String s = Utils.stringConcat(includePaths, File.separator);
			commandList.add("--include-paths=" + s);
		}

		if(inputDirectory != null) {
			if(!inputDirectory.exists())
				throw new MojoFailureException("Input directory '" + inputDirectory + "' does not exist.");
			commandList.add("--input-directory=" + inputDirectory.getAbsolutePath());
		}

		if(outputDirectory != null) {
			if(!outputDirectory.exists())
				throw new MojoFailureException("Output directory '" + outputDirectory + "' does not exist.");
			commandList.add("--output-directory=" + outputDirectory.getAbsolutePath());
		}

		if(cppOutputDirectory != null) {
			if(!cppOutputDirectory.exists())
				throw new MojoFailureException("CPP Output directory '" + cppOutputDirectory + "' does not exist.");
			commandList.add("--cpp-output-directory=" + cppOutputDirectory.getAbsolutePath());
		}

		if(javaOutputDirectory != null) {
			if(!javaOutputDirectory.exists())
				throw new MojoFailureException("Java Output directory '" + javaOutputDirectory + "' does not exist.");
			commandList.add("--java-output-directory=" + javaOutputDirectory.getAbsolutePath());
		}

		checkMandatoryArguments(commandList);

		if(options != null) {
			for(String s : options)
				commandList.add(s);
		}

		return true;
	}

	public static final String K_LD_LIBRARY_PATH = "LD_LIBRARY_PATH";
	public void execute() throws MojoFailureException, MojoFailureException {
		commandList.clear();
		parseArguments();
		//String generator = generatorExecutable();
		List<String> thisCommandList = new ArrayList<String>();
		//thisCommandList.add(generator);
		thisCommandList.addAll(commandList);
		System.out.println(thisCommandList.toString());

		EnvironmentEditor envvarEditor = new EnvironmentEditor();
		if(qtLibDirectory != null)
			envvarEditor.add(K_LD_LIBRARY_PATH, new OpPathPrepend(qtLibDirectory.getAbsolutePath()));

		//Exec.execute(thisCommandList, workingDirectory, qtLibDirectory);
	}

	public void setHeaderFile(File headerFile) {
		this.headerFile = headerFile;
	}

	public void setTypesystem(File typesystemFile) {
		this.typesystemFile = typesystemFile;
	}

	public void setPhononpath(File path) {
		this.phononInclude = path;
	}

	public void setQtIncludeDirectory(File qtIncludeDirectory) {
		this.qtIncludeDirectory = qtIncludeDirectory;
	}

	public void setQtLibDirectory(File qtLibDirectory) {
		this.qtLibDirectory = qtLibDirectory;
	}

	public void setInputDirectory(File inputDirectory) {
		this.inputDirectory = inputDirectory;
	}

	public void setOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	public void setCppOutputDirectory(File cppOutputDirectory) {
		this.cppOutputDirectory = cppOutputDirectory;
	}

	public void setJavaOutputDirectory(File javaOutputDirectory) {
		this.javaOutputDirectory = javaOutputDirectory;
	}

	public void setWorkingDirectory(File workingDirectory) {
		this.workingDirectory = workingDirectory;
	}
}
