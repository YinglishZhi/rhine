package com.rhine.terminal.server.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.function.Supplier;

/**
 * 基本的启动 netty 的类
 * Netty Telnet Bootstrap
 *
 * @author LDZ
 * @date 2019-11-07 19:21
 */
public class TelnetBootstrap {

    private String host = "localhost";
    private int port = 3385;

    private EventLoopGroup group = new NioEventLoopGroup(1);

    /**
     * open telnet server
     */
    public void open(Supplier<TelnetHandler> factory) {

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(group)
                // 指定是一个NIO连接通道
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        // 添加处理业务的类
                        pipeline.addLast(new TelnetChannelHandler(factory));
                    }
                });
        // 绑定对应的端口号,并启动开始监听端口上的连接
        serverBootstrap.bind(getHost(), getPort());
    }

    /**
     * close
     */
    public void close() {
        group.shutdownGracefully();
    }

// ========================


    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

}
