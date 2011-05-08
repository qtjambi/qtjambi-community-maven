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
	private Executable exe;
	private static boolean seenErrorFlag;

	public static void seenError() {
		synchronized(Main.class) {
			seenErrorFlag = true;
		}
	}

	public Main(String[] args) {
		this.args = args;
	}

	public void prepare() {
		exe = ExecutableUtils.extractFromClasspath(null);
	}

	public void run() {
		int i = 0;
		for(String s : args) {
			System.out.println("arg[" + i + "]: " + s);
			i++;
		}
		Integer exitStatus = null;
		// Extract
		//NarManager.loadLibrary();
		// Run
		try {
			exitStatus = exe.run(args);
		} catch(Throwable t) {
			// Cleanup
			exe.cleanup();
		}
		if(exitStatus != null)
			doExit(exitStatus.intValue());
	}

	private void doExit(int exitStatus) {
		System.out.println("exitStatus=" + exitStatus);
		System.out.flush();
		System.exit(exitStatus);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Main main = null;
		try {
			main = new Main(args);
			main.prepare();
			if(seenErrorFlag)
				main.doExit(255);
			main.run();
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}
}
