package com.rhine.terminal;

import com.rhine.terminal.api.TtyConnection;
import com.rhine.terminal.server.netty.RhineTelnetConnection;
import com.rhine.terminal.server.netty.TelnetBootstrap;
import lombok.extern.slf4j.Slf4j;

import java.lang.instrument.Instrumentation;
import java.nio.charset.Charset;
import java.util.function.Consumer;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Netty Telnet Bootstrap
 * server 的总入口
 *
 * @author LDZ
 * @date 2019-11-07 19:29
 */
@Slf4j
public class RhineServer {

    /**
     * telnet 启动类
     */
    private final TelnetBootstrap telnet;

    /**
     * Enable or disable the TELNET BINARY option on output.
     */
    private boolean outBinary;

    /**
     * Enable or disable the TELNET BINARY option on input.
     */
    private boolean inBinary;

    /**
     * charset
     */
    private Charset charset = UTF_8;


    public RhineServer() {
        telnet = new TelnetBootstrap();
    }

    /**
     * 启动netty
     *
     * @param consumerFactory 终端链接句柄工厂，用于构造各种处理业务的类
     */
    public void open(Consumer<TtyConnection> consumerFactory) {
        log.info("host = {}, port = {}", telnet.getHost(), telnet.getPort());
        telnet.open(() -> new RhineTelnetConnection(inBinary, outBinary, charset, consumerFactory));
    }

    /**
     * 停止netty
     */
    public void stop() {
        telnet.close();
    }

    // ============================== get or set ==============================

    public String getHost() {
        return telnet.getHost();
    }

    public RhineServer setHost(String host) {
        telnet.setHost(host);
        return this;
    }

    public int getPort() {
        return telnet.getPort();
    }

    public RhineServer setPort(int port) {
        telnet.setPort(port);
        return this;
    }

    public RhineServer setOutBinary(boolean outBinary) {
        this.outBinary = outBinary;
        return this;
    }

    public RhineServer setInBinary(boolean inBinary) {
        this.inBinary = inBinary;
        return this;
    }

    public boolean isOutBinary() {
        return outBinary;
    }

    public boolean isInBinary() {
        return inBinary;
    }

    public RhineServer setCharset(Charset charset) {
        this.charset = charset;
        return this;
    }

    public Charset getCharset() {
        return charset;
    }

}

