package com.rhine.terminal.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

/**
 * 处理业务类
 *
 * @author LDZ
 * @date 2019-11-01 15:36
 */
@Slf4j
public class TelnetChannelHandler extends ChannelInboundHandlerAdapter {

    private final Supplier<TelnetHandler> factory;
    private TelnetConnection connection;

    TelnetChannelHandler(Supplier<TelnetHandler> factory) {
        this.factory = factory;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf buf = (ByteBuf) msg;
        int size = buf.readableBytes();
        byte[] data = new byte[size];
        buf.getBytes(0, data);
        // 从这里开始
        connection.receive(data);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        connection = new TelnetConnection(factory.get(), ctx);
        connection.onInit();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        connection.onClose();
        connection = null;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause.getMessage());
        ctx.close();
    }

}
