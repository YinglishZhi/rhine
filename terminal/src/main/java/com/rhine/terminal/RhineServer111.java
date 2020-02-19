package com.rhine.terminal;

import com.rhine.terminal.server.BaseTelnetBootstrap;
import com.rhine.terminal.server.TelnetChannelHandler;
import com.rhine.terminal.server.TelnetConnection;
import com.rhine.terminal.server.TelnetHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.instrument.Instrumentation;
import java.util.function.Supplier;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * server
 *
 * @author LDZ
 * @date 2019-11-01 14:43
 */
@Slf4j
public class RhineServer111 extends BaseTelnetBootstrap {


    private Instrumentation instrumentation;

    public RhineServer111(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }

    private EventLoopGroup group = new NioEventLoopGroup(1);
//    private EventLoopGroup workerGroup = new NioEventLoopGroup();


    @Override
    public RhineServer111 setHost(String host) {
        return (RhineServer111) super.setHost(host);
    }

    @Override
    public RhineServer111 setPort(int port) {
        return (RhineServer111) super.setPort(port);
    }

    /**
     * open telnet server
     */
    @Override
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
    @Override
    public void close() {
        group.shutdownGracefully();
    }

    private static volatile RhineServer111 rhineServer;

    public static RhineServer111 getInstance(Instrumentation instrumentation) {
        if (null == rhineServer) {
            synchronized (RhineServer111.class) {
                if (null == rhineServer) {
                    rhineServer = new RhineServer111(instrumentation);
                }
            }
        }
        return rhineServer;
    }

// ========================

    public void open() {
        open(() -> new TelnetHandler() {
            @Override
            public void onOpen(TelnetConnection connection) {
                log.info("connection open");
                connection.send("connection is open".getBytes());
            }

            @Override
            public void onData(byte[] data) {
                log.info(new String(data, UTF_8));
            }

            @Override
            public void onClose() {
                log.info("close");
            }
        });
    }

//    private static Thread getThread() {
//        return new Thread(() -> {
//            try {
//                log.info(Thread.currentThread().getName() + "开始任务=======================");
//                RhineServer111 rhineServer = new RhineServer111(null).setHost("localhost").setPort(8888);
//                rhineServer.open();
//                //模拟1秒钟任务
//                Thread.sleep(1000L);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            log.info(Thread.currentThread().getName() + "任务执行完成之后，下面才能执行，不然一直被阻塞");
//        });
//    }

    public static void main(String[] args) throws InterruptedException {


        Thread thread = new Thread(() -> {
            try {
                RhineServer111 rhineServer = new RhineServer111(null).setHost("localhost").setPort(3385);
                rhineServer.open();
                //模拟1秒钟任务
                Thread.sleep(1L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

//        Thread thread = getThread();
        thread.start();
        thread.join();

        System.out.println(Thread.currentThread().getName() + "server start.....");
//
//        Thread.sleep(1000);
//
//        try {
//            URLClassLoader classLoader = new URLClassLoader(
//                    new URL[]{new File("/Users/zhiyinglish/dev/rhine/client/target/client-jar-with-dependencies.jar").toURI().toURL()});
//            Class<?> telnetConsoleClas = classLoader.loadClass("com.rhine.client.TelnetConsole");
//            Method mainMethod = telnetConsoleClas.getMethod("main", String[].class);
//            mainMethod.invoke(null, new Object[]{new String[]{"123", "456"}});
//            System.out.println(1);
//        } catch (NoSuchMethodException | MalformedURLException | ClassNotFoundException | IllegalAccessException | InvocationTargetException e) {
//            e.printStackTrace();
//        }
    }
}
