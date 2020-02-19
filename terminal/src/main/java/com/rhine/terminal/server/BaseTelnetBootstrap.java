package com.rhine.terminal.server;

import java.util.function.Supplier;

/**
 * telnet bootstrap 抽象类
 *
 * @author LDZ
 * @date 2019-11-01 15:15
 */
public abstract class BaseTelnetBootstrap {

    private String host = "localhost";
    private int port = 3385;

    public String getHost() {
        return host;
    }

    public BaseTelnetBootstrap setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public BaseTelnetBootstrap setPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * open telnet server
     *
     * @param factory the telnet handler factory
     */
    public abstract void open(Supplier<TelnetHandler> factory);

    public abstract void close();
}
