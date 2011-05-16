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
	private static boolean verbose;
	private static boolean noCleanup;


	public static void seenError() {
		synchronized(Main.class) {
			seenErrorFlag = true;
		}
	}
	public static boolean isVerbose() {
		synchronized(Main.class) {
			return verbose;
		}
	}
	public static boolean isNoCleanup() {
		synchronized(Main.class) {
			return noCleanup;
		}
	}

	public Main(String[] args) {
		this.args = args;
	}

	public void prepare() {
		// Extract
		exe = ExecutableUtils.extractFromClasspath(null);
	}

	public void run() throws Throwable {
		if(verbose) {
			int i = 0;
			for(String s : args) {
				System.out.println("arg[" + i + "]: " + s);
				i++;
			}
		}

		Integer exitStatus = null;
		// Run
		exitStatus = exe.run(args);
		if(exitStatus != null && exitStatus.intValue() != 0)
			doExit(exitStatus.intValue());
	}

	private void doExit(int exitStatus) {
		if(exe != null)
			exe.cleanup();
		exe = null;
		if(exitStatus != 0) {
			String exitStatusString;
			if(exitStatus < 0)	// high unsigned
				exitStatusString = String.format("0x%x", exitStatus);
			else
				exitStatusString = Integer.valueOf(exitStatus).toString();
			StringBuffer sb = new StringBuffer("exitStatus=" + exitStatusString);
			if(ExecutableUtils.getHostKind() == ExecutableUtils.HostKind.Windows) {
				// Windows error code diagnostics
				if(exitStatus == 0xc0000005)
					sb.append(" (Access Violation)");
				else if(exitStatus == 0xc0000135)
					sb.append(" (Unable to locate DLL)");
			}
			System.out.println(sb.toString());
		}
		System.out.flush();
		System.exit(exitStatus);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(System.getenv("MAVEN_EXE_VERBOSE") != null)
			verbose = true;
		if(System.getenv("MAVEN_EXE_NOCLEANUP") != null)
			noCleanup = true;

		Main main = null;
		try {
			main = new Main(args);
			main.prepare();
			if(main.exe == null || seenErrorFlag)
				main.doExit(255);
			main.run();
		} catch(Throwable t) {
			t.printStackTrace();
		} finally {
			if(main.exe != null)
				main.exe.cleanup();
			main.exe = null;
		}
	}
}
