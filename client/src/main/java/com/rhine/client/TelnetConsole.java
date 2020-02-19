package com.rhine.client;

import com.rhine.client.util.IOUtil;
import com.taobao.middleware.cli.CLI;
import com.taobao.middleware.cli.CommandLine;
import com.taobao.middleware.cli.annotations.*;
import jline.TerminalSupport;
import jline.console.ConsoleReader;
import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.TelnetOptionHandler;
import org.apache.commons.net.telnet.WindowSizeOptionHandler;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author LDZ
 * @date 2019-11-05 18:28
 */
@Name("client")
@Summary("telnet client")
public class TelnetConsole {

    private boolean help = false;

    private String targetIp = "127.0.0.1";
    private int port = 3385;


    @Option(longName = "help", flag = true)
    @Description("Print usage")
    public void setHelp(boolean help) {
        this.help = help;
    }

    @Option(longName = "target-ip", required = false)
    @Description("Target Ip")
    public void setTargetIp(String targetIp) {
        this.targetIp = targetIp;
    }

    @Option(longName = "port", required = false)
    @Description("port")
    public void setPort(int port) {
        this.port = port;
    }

    private static final TelnetClient telnetClient = new TelnetClient();

    private static ConsoleReader consoleReader = null;

    public static void main(String[] args) {

        try {

            TelnetConsole telnetConsole = new TelnetConsole();
            CLI cli = CLIConfigurator.define(TelnetConsole.class);
            CommandLine commandLine = cli.parse(Arrays.asList(args));
            CLIConfigurator.inject(commandLine, telnetConsole);

            if (telnetConsole.isHelp()) {
                System.out.println("this is help");
                System.exit(0);
            }

            initConsoleReader();

            connect(telnetConsole);

            IOUtil.readWrite(telnetClient.getInputStream(), telnetClient.getOutputStream(), System.in, consoleReader.getOutput());

        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(1);
        }


    }

    /**
     * 连接 telnet
     *
     * @throws IOException 连接异常
     */
    private static void connect(TelnetConsole telnetConsole) throws IOException {
        telnetClient.setConnectTimeout(5000);
        int width = TerminalSupport.DEFAULT_WIDTH;
        int height = TerminalSupport.DEFAULT_HEIGHT;
        TelnetOptionHandler sizeOpt = new WindowSizeOptionHandler(width, height, true, true, false, false);
        try {
            telnetClient.addOptionHandler(sizeOpt);
        } catch (InvalidTelnetOptionException e) {
            // ignore
        }
        telnetClient.connect(telnetConsole.getTargetIp(), telnetConsole.getPort());
    }

    /**
     * 初始化 console
     *
     * @throws IOException 初始化异常
     */
    private static void initConsoleReader() throws IOException {

        consoleReader = new ConsoleReader(System.in, System.out);

        consoleReader.setHandleUserInterrupt(true);

//        Terminal terminal = consoleReader.getTerminal();

//        if (terminal instanceof TerminalSupport) {
//            terminal.disableInterruptCharacter();
//        }
//        // support catch ctrl+c event
//        if (terminal instanceof UnixTerminal) {
//            ((UnixTerminal) terminal).disableLitteralNextCharacter();
//        }
    }


    private boolean isHelp() {
        return help;
    }

    private String getTargetIp() {
        return targetIp;
    }

    private int getPort() {
        return port;
    }
}
