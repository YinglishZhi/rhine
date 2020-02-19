package com.rhine.launch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.rhine.launch.IOUtils.close;

/**
 * 命令行执行
 *
 * @author LDZ
 * @date 2019-11-07 10:29
 */
public class ProcessUtils {


    public static void main(String[] args) {
//        System.out.println(listProcessByJps());

    }


    @SuppressWarnings("ThrowableNotThrown")
    static void process(List<String> processArgs) {

        List<String> command = new ArrayList<>(processArgs);
        ProcessBuilder pb = new ProcessBuilder(command);
        try {
            final Process proc = pb.start();
            Thread redirectStdout = new Thread(() -> {
                InputStream inputStream = proc.getInputStream();
                try {
                    IOUtils.copy(inputStream, System.out);
                } catch (IOException e) {
                    close(inputStream);
                }

            });

            Thread redirectStderr = new Thread(() -> {
                InputStream inputStream = proc.getErrorStream();
                try {
                    IOUtils.copy(inputStream, System.err);
                } catch (IOException e) {
                    close(inputStream);
                }

            });
            redirectStdout.start();
            redirectStderr.start();
            redirectStdout.join();
            redirectStderr.join();

            int exitValue = proc.exitValue();
            if (exitValue != 0) {
                System.exit(1);
            }
        } catch (Throwable e) {
            // ignore
        }
    }


    private static Map<Integer, String> listProcessByJps() {

        Map<Integer, String> result = new HashMap<>();

        String jps = "jps";

        String[] commond = new String[]{jps, "-l"};

        List<String> lines = runNative(commond);

        for (String line : lines) {
            String[] strings = line.trim().split("\\s+");

            if (strings.length < 1) {
                continue;
            }
            result.put(Integer.parseInt(strings[0]), line);
        }

        return result;
    }


    /**
     * Executes a command on the native command line and returns the result line by
     * line.
     *
     * @param cmdToRunWithArgs Command to run and args, in an array
     * @return A list of Strings representing the result of the command, or empty
     * string if the command failed
     */
    @SuppressWarnings("ThrowableNotThrown")
    private static List<String> runNative(String[] cmdToRunWithArgs) {
        Process p;
        try {
            p = Runtime.getRuntime().exec(cmdToRunWithArgs);
        } catch (SecurityException | IOException e) {
            return new ArrayList<>(0);
        }

        ArrayList<String> sa = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                sa.add(line);
            }
            p.waitFor();
        } catch (IOException e) {
            return new ArrayList<>(0);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } finally {
            close(reader);
        }
        return sa;
    }

}
