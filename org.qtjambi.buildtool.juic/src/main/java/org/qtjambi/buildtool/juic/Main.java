package org.qtjambi.buildtool.juic;

public class Main {
	// src/main/qmake/main.cpp:
	// -a
	// -i [include]
	// -e [exclude]
	// -v
	// -p package
	// -d out_dir
	// -cp process_directory
	// -pf prefix
	// --generate-main-method
	//  <UIfile> <UIfile...>

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
