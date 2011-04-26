package org.qtjambi.buildtool.generator;

public class Main {
	public Main(String[] args) {
	}


	public void run() {
		int status = 0;
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
