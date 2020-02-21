package com.rhine.terminal.server.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

/**
 * Netty 通道处理句柄
 * telnet连接、读取的总入口
 *
 * @author LDZ
 * @date 2019-11-01 15:36
 */
@Slf4j
public class TelnetChannelHandler extends ChannelInboundHandlerAdapter {

    /**
     * 业务处理类 supplier
     */
    private final Supplier<TelnetHandler> factory;

    /**
     * telnet 的连接
     */
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
        // 处理接受数据
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
