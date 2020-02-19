package com.rhine.core;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

import java.io.IOException;

/**
 * @author LDZ
 * @date 2019-11-01 17:14
 */
public class EvilRoot {

    private static final String HECK_JAR_PATH = "/Users/zhiyinglish/dev/rhine/heck/target/heck-1.0-SNAPSHOT.jar";


    public static void main(String[] args) {
        // server
        try {
            attachAgent("69290");
        } catch (IOException | AttachNotSupportedException | AgentLoadException | AgentInitializationException e) {
            e.printStackTrace();
        }
    }

    private static void attachAgent(String pid) throws IOException, AttachNotSupportedException, AgentLoadException, AgentInitializationException {
        VirtualMachine vm = VirtualMachine.attach(pid);
        vm.loadAgent(HECK_JAR_PATH, "/Users/zhiyinglish/dev/rhine/terminal/target/terminal-jar-with-dependencies.jar");
        vm.detach();
    }

    private static String parse(String[] args) {
        return args[0];
    }

}
