package com.rhine.core;

import com.rhine.core.config.Configure;
import com.rhine.util.CLIConfigurator;
import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;
import sun.security.krb5.Config;

import java.io.IOException;
import java.util.Optional;

/**
 * @author LDZ
 * @date 2019-11-01 17:14
 */
@Slf4j
public class EvilRoot {

    private static final String HECK_JAR_PATH = "/Users/zhiyinglish/code/DEV/rhine/heck/target/heck-1.0-SNAPSHOT.jar";


    public static void main(String[] args) {
        // server
        try {
            attachAgent(parse(args));
        } catch (IOException | AttachNotSupportedException | AgentLoadException | AgentInitializationException e) {
            e.printStackTrace();
        }
    }

    private static void attachAgent(Configure configure) throws IOException, AttachNotSupportedException, AgentLoadException, AgentInitializationException {

        String pid = Optional.ofNullable(configure).map(Configure::getJavaPid).map(String::valueOf).orElseThrow(() -> new IllegalArgumentException("configure is null"));

        VirtualMachine vm = VirtualMachine.attach(pid);

        vm.loadAgent(HECK_JAR_PATH, "/Users/zhiyinglish/code/DEV/rhine/terminal/target/terminal-jar-with-dependencies.jar");
        vm.detach();
    }

    private static Configure parse(String[] args) {

        Options options = CLIConfigurator.define(Configure.class);

        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cli = parser.parse(options, args);
            Configure configure = new Configure();
            try {
                CLIConfigurator.inject(cli, configure);
            } catch (Exception e) {
                // ignore
                log.error("error");
            }
            return configure;
        } catch (ParseException e) {
            // ignore
        }
        return null;
    }

}
