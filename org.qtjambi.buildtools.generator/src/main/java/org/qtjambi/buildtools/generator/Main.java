package org.qtjambi.buildtools.generator;

public class Main {
	// src/main/qmake/wrapper.cpp:
	//   default_file("targets/qtjambi_masterinclude.h"),
	//   default_system("targets/build_all.xml"),
	//   pp_file(".preprocessed.tmp")
	// --no-suppress-warnings
	// --include-eclipse-warnings
	// --debug-level=[types|sparse|medium|full]
	// --dummy
	// --diff
	// --rebuild-only
	// --qt-include-directory
	// --ast-to-xml
	// --dump-object-tree
	// --help, -h or -?
	// --output-directory=[dir]
	// --include-paths=<path>[%c<path>%c...]
	// --print-stdout
	// --qt-include-directory=[dir]
	// header-file typesystem-file

	private String[] args;

	public Main(String[] args) {
		this.args = args;
	}


	public void run() {
		int i = 0;
		for(String s : args) {
			System.out.println("arg[" + i + "]: " + s);
			i++;
		}
		int status = 0;
		// Extract
		//NarManager.loadLibrary();
		// Run
		// Cleanup
		System.out.println("exitStatus=" + status);
		System.out.flush();
		System.exit(status);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Main main = null;
		try {
			main = new Main(args);
			main.run();
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}

}
