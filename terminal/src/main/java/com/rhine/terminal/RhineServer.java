package com.rhine.terminal;

import com.rhine.terminal.api.TtyConnection;
import com.rhine.terminal.readline.ReadLine;
import com.rhine.terminal.server.BaseTelnetBootstrap;
import com.rhine.terminal.server.TelnetBootstrap;
import com.rhine.terminal.server.TelnetHandler;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.nio.charset.StandardCharsets.UTF_8;
import static jdk.nashorn.internal.runtime.ScriptingFunctions.readLine;

/**
 * Netty Telnet Bootstrap
 *
 * @author LDZ
 * @date 2019-11-07 19:29
 */
public class RhineServer {

    private final TelnetBootstrap telnet;
    //    Enable or disable the TELNET BINARY option on output.
    private boolean outBinary;
    //    Enable or disable the TELNET BINARY option on input.
    private boolean inBinary;
    private Charset charset = UTF_8;


    public RhineServer() {
        telnet = new TelnetBootstrap();
    }

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

    public RhineServer setCharset(Charset charset) {
        this.charset = charset;
        return this;
    }

    public boolean isOutBinary() {
        return outBinary;
    }

    public boolean isInBinary() {
        return inBinary;
    }

    public Charset getCharset() {
        return charset;
    }


    private Function<CompletableFuture<?>, Consumer<Throwable>> startHandler =
            completableFuture -> err -> {
                if (null == err) {
                    completableFuture.complete(null);
                } else {
                    completableFuture.completeExceptionally(err);
                }
            };

    public CompletableFuture<?> open(Consumer<TtyConnection> factory) {
        CompletableFuture<?> future = new CompletableFuture<>();
        open(factory, startHandler.apply(future));
        return future;
    }


    public void open(Consumer<TtyConnection> factory, Consumer<Throwable> doneHandler) {
        telnet.open(() -> new RhineTelnetConnection(inBinary, outBinary, charset, factory));
    }

    public void stop() {
        telnet.close();
    }

}

