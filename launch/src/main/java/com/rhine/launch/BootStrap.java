package com.rhine.launch;


import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.rhine.launch.ProcessUtils.process;

/**
 * @author LDZ
 * @date 2019-11-07 10:49
 */
public class BootStrap {


    private static final String CLIENT_PATH = "/Users/zhiyinglish/code/DEV/rhine/client/target";
    private static final String CLIENT_NAME = "client-jar-with-dependencies.jar";

    private static final String CORE_PATH = "/Users/zhiyinglish/code/DEV/rhine/core/target";
    private static final String CORE_NAME = "core-jar-with-dependencies.jar";

    private static final String[] JAVA_PATHS = {"bin/java", "bin/java.exe", "../bin/java", "../bin/java.exe"};
    private static final String[] JPS_PATHS = {"bin/jps", "bin/jps.exe", "../bin/jps", "../bin/jps.exe"};

    private static Path path = getThePath();

    public static void main(String[] args) {

        startCore();

        startClient();

    }


    private static void startCore() {

        // get pid
        int pid = ProcessUtils.select();

        List<String> command = new ArrayList<>();
        // $JAVA_HOME/jre/bin/java
        // // 指定 tool.jar 否则会报错
        // -Xbootclasspath/a:/Library/Java/JavaVirtualMachines/jdk1.8.0_151.jdk/Contents/Home/jre/../lib/tools.jar
        // -jar
        // core-jar-with-dependencies.jar
        // 15277
        command.add(path.getJavaPath());
        command.add("-Xbootclasspath/a:" + path.getToolsJar());
        command.add("-jar");
        command.add(CORE_PATH + File.separator + CORE_NAME);
        command.add("-p" + pid);
        process(command);
    }

    private static void startClient() {
        try {
            URLClassLoader classLoader = new URLClassLoader(new URL[]{new File(CLIENT_PATH + File.separator + CLIENT_NAME).toURI().toURL()});
            Class<?> telnetConsoleClazz = classLoader.loadClass("com.rhine.client.TelnetConsole");
            Method mainMethod = telnetConsoleClazz.getMethod("main", String[].class);

            List<String> telnetArgs = new ArrayList<>();

            telnetArgs.add("-target-ip");
            telnetArgs.add("localhost");
            telnetArgs.add("-port");
            telnetArgs.add("1234");

            mainMethod.invoke(null, new Object[]{telnetArgs.toArray(new String[0])});
            System.out.println(1);
        } catch (NoSuchMethodException | MalformedURLException | ClassNotFoundException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }


    // 暂且不考虑java9的模块化
    private static Path getThePath() {

        if (null != path && path.exist()) {
            return path;
        }
        // 在 java.home 下找
        String javaHome = System.getProperty("java.home");

        File toolsJar = new File(javaHome, "lib/tools.jar");
        if (!toolsJar.exists()) {
            toolsJar = new File(javaHome, "../lib/tools.jar");
            if (!toolsJar.exists()) {
                toolsJar = new File(javaHome, "../../lib/tools.jar");
            }
        }

        if (!toolsJar.exists()) {
            String javaHomeEnv = System.getenv("JAVA_HOME");
            if (null != javaHomeEnv && !javaHomeEnv.isEmpty()) {
                toolsJar = new File(javaHomeEnv, "lib/tools.jar");
                if (!toolsJar.exists()) {
                    toolsJar = new File(javaHomeEnv, "../lib/tools.jar");
                    if (toolsJar.exists()) {
                        javaHome = javaHomeEnv;
                    }
                }
            }
        }
        String javaPath = Optional.ofNullable(findPath(javaHome, JAVA_PATHS)).orElseThrow(() -> new IllegalArgumentException("java not exist"));
        String jpsPath = Optional.ofNullable(findPath(javaHome, JPS_PATHS)).orElse("jps");
        if (toolsJar.exists()) {
            path = new Path();
            path.setJavaHome(javaHome);
            path.setToolsJar(toolsJar.getAbsolutePath());
            path.setJpsPath(jpsPath);
            path.setJavaPath(javaPath);
            return BootStrap.path;
        } else {
            throw new IllegalArgumentException("not found");
        }
    }


    private static String findPath(String javaHome, String[] paths) {

        File target = null;
        for (String path : paths) {
            File tmpFile = new File(javaHome, path);
            if (tmpFile.exists()) {
                if (null == target) {
                    target = tmpFile;
                } else {
                    try {
                        target = target.getCanonicalPath().length() < tmpFile.getCanonicalPath().length() ? target : tmpFile;
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }
        }
        return Optional.ofNullable(target).map(File::getAbsolutePath).orElse(null);
    }

    @Data
    private static class Path {
        private String javaHome;

        private String toolsJar;

        private String javaPath;

        private String jpsPath;

        private boolean exist() {
            return javaHome != null && toolsJar != null && javaPath != null && jpsPath != null;
        }
    }
}
