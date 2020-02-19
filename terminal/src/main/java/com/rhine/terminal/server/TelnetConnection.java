package com.rhine.terminal.server;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.TimeUnit;

/**
 * telnet的连接
 *
 * @author LDZ
 * @date 2019-11-01 15:45
 */
public class TelnetConnection {

    private final TelnetHandler handler;
    final ChannelHandlerContext context;

    TelnetConnection(TelnetHandler handler, ChannelHandlerContext context) {
        this.handler = handler;
        this.context = context;
    }

    void onInit() {
        handler.onOpen(this);
    }

    void receive(byte[] data) {
        handler.onData(data);
    }

    void onClose() {
        handler.onClose();
    }

    public void send(byte[] data) {
        context.writeAndFlush(Unpooled.buffer().writeBytes(data));
    }

    public void execute(Runnable task) {
        context.channel().eventLoop().execute(task);
    }

    public void schedule(Runnable task, long delay, TimeUnit unit) {
        context.channel().eventLoop().schedule(task, delay, unit);
    }
}
