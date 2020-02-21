package com.rhine.terminal;

import com.rhine.terminal.api.TtyConnection;
import com.rhine.terminal.readline.*;
import com.rhine.terminal.server.TelnetConnection;
import com.rhine.terminal.server.TelnetHandler;
import com.rhine.terminal.util.Vector;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.rhine.terminal.server.TelnetConnection.*;
import static java.nio.charset.StandardCharsets.US_ASCII;

/**
 * telnet tty connect
 * server 各种handler
 *
 * @author LDZ
 * @date 2019-11-08 14:44
 */
public class RhineTelnetConnection extends TelnetHandler implements TtyConnection {

    private final boolean inBinary;
    private final boolean outBinary;
    private Vector size;
    private Consumer<Vector> sizeHandler;
    private Charset charset;
    private Consumer<TtyConnection> handler;

    private TelnetConnection connection;

    private EventDecoder eventDecoder = new EventDecoder(3, 26, 4);
    private ReadBuffer readBuffer = new ReadBuffer(this::execute);
    private BinaryDecoder decoder = new BinaryDecoder(512, RhineCharset.INSTANCE, readBuffer);
    private BinaryEncoder encoder = new BinaryEncoder(US_ASCII, data -> connection.send(data));
    private final Consumer<int[]> stdout = new OutputMode(encoder);

    public RhineTelnetConnection(boolean inBinary, boolean outBinary, Charset charset, Consumer<TtyConnection> handler) {
        this.inBinary = inBinary;
        this.outBinary = outBinary;
        this.charset = charset;
        this.handler = handler;
    }

    @Override
    public void onOpen(TelnetConnection connection) {
        this.connection = connection;
//
        connection.send(new byte[]{BYTE_IAC, BYTE_WILL, (byte) 1});
        connection.send(new byte[]{BYTE_IAC, BYTE_WILL, (byte) 3});

        if (inBinary) {
            connection.send(new byte[]{BYTE_IAC, BYTE_DO, (byte) 0});
        }

        if (outBinary) {
            connection.send(new byte[]{BYTE_IAC, BYTE_WILL, (byte) 0});
        }
        // Window size
        connection.send(new byte[]{BYTE_IAC, BYTE_DO, (byte) 31});

        readBuffer.setReadHandler(eventDecoder);

        handler.accept(this);
    }


    @Override
    public void onData(byte[] data) {
        decoder.write(data);
    }


    @Override
    public Vector size() {
        return size;
    }

    @Override
    protected void onSize(int width, int height) {
        this.size = new Vector(width, height);
        if (sizeHandler != null) {
            sizeHandler.accept(size);
        }
    }

    @Override
    public Consumer<Vector> getSizeHandler() {
        return this.sizeHandler;
    }

    @Override
    public void setSizeHandler(Consumer<Vector> handler) {
        this.sizeHandler = handler;
    }

    @Override
    public Consumer<int[]> getStdinHandler() {
        return eventDecoder.getReadHandler();
    }

    @Override
    public void setStdinHandler(Consumer<int[]> handler) {
        eventDecoder.setReadHandler(handler);
    }

    @Override
    public Consumer<int[]> stdoutHandler() {
        return stdout;
    }

    @Override
    public void execute(Runnable task) {
        connection.execute(task);
    }

    @Override
    public void schedule(Runnable task, long delay, TimeUnit unit) {
        connection.schedule(task, delay, unit);
    }

    @Override
    public void setReadHandler(Consumer<int[]> handler) {
        eventDecoder.setReadHandler(handler);
    }

    @Override
    public Consumer<int[]> getReadHandler() {
        return eventDecoder.getReadHandler();
    }

}
