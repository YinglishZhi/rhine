package com.rhine.heck;


import org.junit.Test;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.net.MalformedURLException;
import java.net.URL;

public class AgentLauncherTest {
    private static final String CORE_JAR_PATH = "/Users/zhiyinglish/code/DEV/rhine/core/target/core-jar-with-dependencies.jar";

    @Test
    public void main() {



        try {

            ClassLoader agentClassLoader = loadOrDefineClassLoader(CORE_JAR_PATH);

            final Class<?> server = agentClassLoader.loadClass("com.rhine.core.server.RhineServiceBootstrap");

            Object instance = server.getMethod("getInstance", Instrumentation.class).invoke(null, null);

            server.getMethod("bind").invoke(instance);

        } catch (Exception e) {
            // ignore
        }


    }
    private static volatile ClassLoader agentClassLoader;

    private static ClassLoader loadOrDefineClassLoader(String agentJar) throws MalformedURLException {
        if (null == agentClassLoader) {
            agentClassLoader = new AgentClassLoader(new URL[]{new File(agentJar).toURI().toURL()});
        }
        return agentClassLoader;
    }}
