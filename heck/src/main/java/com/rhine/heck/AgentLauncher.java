package com.rhine.heck;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * agent launcher
 *
 * @author LDZ
 * @date 2019-11-01 16:43
 */
public class AgentLauncher {


    private static final String RHINE_SERVER = "com.rhine.terminal.RhineServer";


    /**
     * agent class loader
     */
    private static volatile ClassLoader agentClassLoader;

    public static void premain(String args, Instrumentation instrumentation) {
        System.out.println("execute premain");
        main(args, instrumentation);
    }

    public static void agentmain(String args, Instrumentation instrumentation) {
        System.out.println("execute agent");
        main(args, instrumentation);
    }

    private static void main(String args, Instrumentation instrumentation) {

        try {

            ClassLoader agentClassLoader = loadOrDefineClassLoader(args);

            final Class<?> server = agentClassLoader.loadClass(RHINE_SERVER);

            Object instance = server.getMethod("getInstance", Instrumentation.class).invoke(null, instrumentation);

            server.getMethod("open").invoke(instance);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static ClassLoader loadOrDefineClassLoader(String agentJar) throws MalformedURLException {
        if (null == agentClassLoader) {
            agentClassLoader = new AgentClassLoader(new URL[]{new File(agentJar).toURI().toURL()});
        }
        return agentClassLoader;
    }
}
