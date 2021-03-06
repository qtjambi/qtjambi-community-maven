package com.trolltech.tools.ant;

import org.apache.tools.ant.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * @todo Rewrite. This kind of API is PITA to use and maintain.
 */
class Exec {

    /**
     * Executes the command specified by cmd and returns the printed output
     * from the process' stdout and stderr in the array on position 0 and 1 respectivly.
     * @param cmd The command to execute
     * @return An array of length 2, containing the [stdout, stderr] output.
     * @throws IOException If an error occurs
     * @throws InterruptedException If an error occurs...
     */
    public static String[] execute(String ... cmd) throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec(cmd);

        ByteArrayOutputStream outdata = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outdata);

        ByteArrayOutputStream errdata = new ByteArrayOutputStream();
        PrintStream err = new PrintStream(errdata);

        StreamConsumer stdoutReader = new StreamConsumer(p.getInputStream(), out);
        StreamConsumer stderrReader = new StreamConsumer(p.getErrorStream(), err);
        stdoutReader.start();
        stderrReader.start();
        p.waitFor();

        stdoutReader.join();
        stderrReader.join();
        out.close();
        err.close();

        return new String[] { outdata.toString(), errdata.toString() };
    }

    /**
     * Convenience method for exec(String, File).
     * @param command Command to be executed.
     */
    public static void exec(String command, Project project) {
        exec(command, null, project);
    }

    /**
     * Execute command command in directory dir.
     * @param command Command to be executed.
     * @param dir Directory where command should be executed.
     * @throws BuildException Thrown if process exit value is not zero or IOException has been occurred.
     */
    public static void exec(String command, File dir, Project project) throws BuildException {
        String directory = ((dir != null) ? "(" + Util.makeCanonical(dir) + ")" : "");
        System.out.println("Running : " + directory + " " + command);
        try {
            Process process = Runtime.getRuntime().exec(command, null, dir);
            Util.redirectOutput(process);
            if (process.exitValue() != 0) {
                throw new BuildException("Running: " + command + " failed with exit code: " + process.exitValue());
            }
        } catch (IOException e) {
            throw new BuildException("Running: " + command + " failed with error message: " + e.getMessage(), e);
        }
    }

    /**
     * TODO: this should be merged with above one, repeating code is not that wise.
     *
     * Executes process in more verbose manner.
     * @param cmd Array of command and its arguments to be executed.
     * @param dir Directory where should be executed.
     * @param verbose Whether to be verbose.
     * @throws BuildException Thrown if process exit value is not zero or IOException has been occurred.
     */
    public static void exec(String cmd[], File dir, Project project, boolean verbose) throws BuildException {
        if (verbose) {
            StringBuilder b = new StringBuilder();
            for (String s : cmd)
                b.append(s).append(' ');
            System.out.println("Running : " + ((dir!=null)? "(" + Util.makeCanonical(dir) + ")" : "") + " " + b);
        }

        try {
            Process process = Runtime.getRuntime().exec(cmd, null, dir);
            Util.redirectOutput(process);
            if (process.exitValue() != 0) {
                throw new BuildException("Running: '" + join(cmd) + "' failed.");
            }
        } catch (IOException e) {
            throw new BuildException("Running: '" + join(cmd) + "' failed.", e);
        }
    }

    public static void execute(List<String> command, File directory, Project project) throws BuildException {
        execute(command, directory, project, null, false);
    }

    public static void execute(List<String> command, File directory, Project project, boolean msyssupport) throws BuildException {
        execute(command, directory, project, null, msyssupport);
    }

    public static void execute(List<String> command, File directory, Project project, String ldpath) throws BuildException {
        execute(command, directory, project, ldpath, false);
    }

    public static void execute(List<String> command, File directory, Project project, String ldpath, boolean msyssupport) throws BuildException {
        String fullCommand = null;
        if(msyssupport == true) {
            Iterator<String> iter = command.iterator();
            String baseCommand = iter.next();
            StringBuilder builder = new StringBuilder();
            while(iter.hasNext()) {
                builder.append(iter.next() + " ");
            }

            fullCommand = baseCommand + " " + builder.toString();
        }

        ProcessBuilder builder;
        if(fullCommand == null) {
            System.out.println("Executing: " + command.toString() + " in directory " + directory.toString());
            builder = new ProcessBuilder(command);
        } else {
            System.out.println("Executing: " + fullCommand + " in directory " + directory.toString());
            builder = new ProcessBuilder(fullCommand);
        }

        // NOTE: this is most likely very linux-specific system. For Windows one would use PATH instead,
        // but it should not be needed there in first place... Only if you want to have same kind of building
        // environment one can have for Linux.
        // it shouldn't affect to Windows environment though.
        Map<String, String> env = builder.environment();
        if(ldpath != null)
            env.put("LD_LIBRARY_PATH", ldpath);

        PropertyHelper props = PropertyHelper.getPropertyHelper(project);
        String s = (String) props.getProperty("java.home.target");
        if(s != null)
            env.put("JAVA_HOME_TARGET", s);
        s = (String) props.getProperty("java.osarch.target");
        if(s != null)
            env.put("JAVA_OSARCH_TARGET", s);

        //something extra?
        s = (String) props.getProperty("qtjambi.phonon.includedir");
        if(s != null && s.length() > 0)
            env.put("PHONON_INCLUDEPATH", s);

        s = (String) props.getProperty("qtjambi.phonon.libdir");
        if(s != null && s.length() > 0)
            env.put("PHONON_LIBS", s);

        builder.directory(directory);
        try {
            Process process = builder.start();
            Util.redirectOutput(process);
            if (process.exitValue() != 0) {
                throw new BuildException("Running: '" + command.toString() + "' failed.  exitStatus=" + process.exitValue());
            }
        } catch (IOException e) {
            throw new BuildException("Running: '" + command.toString() + "' failed.", e);
        }
    }

    /**
     * Internal helper of Exec.
     * @param ar What to join
     * @return array joined together to form "foo1, foo2, .."
     */
    private static String join(String ar[]) {
        String s = "";
        for (int i = 0; i<ar.length; ++i) {
            s += ar[i];
            if (i < ar.length - 1)
                s += " ";
        }
        return s;
    }

}
