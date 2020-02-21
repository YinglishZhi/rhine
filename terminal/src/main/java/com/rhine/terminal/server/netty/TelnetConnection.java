package com.rhine.terminal.server.netty;

import com.rhine.terminal.server.enums.Option;
import com.rhine.terminal.server.enums.TelnetConnectStatus;
import com.rhine.terminal.server.enums.TelnetOrderEnum;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static com.rhine.terminal.server.enums.TelnetConnectStatus.DATA;
import static com.rhine.terminal.server.enums.TelnetOrderEnum.*;

/**
 * telnet的连接
 *
 * @author LDZ
 * @date 2019-11-01 15:45
 */
public class TelnetConnection {

    /**
     * pending buffer
     */
    private byte[] pendingBuffer = new byte[256];

    /**
     * pending length
     */
    private int pendingLength = 0;

    /**
     * telnet connect status
     */
    public TelnetConnectStatus status;

    /**
     *
     */
    public Byte paramsOptionCode;

    /**
     *
     */
    public byte[] paramsBuffer;

    /**
     *
     */
    public int paramsLength;

    /**
     *
     */
    public boolean paramsIac;

    /**
     * send binary ？
     */
    public boolean sendBinary;

    /**
     * receive binary ?
     */
    public boolean receiveBinary;

    /**
     * telnet 句柄 通过telnet 连接处理与Netty通道句柄处理的关系
     */
    public final TelnetHandler handler;

    /**
     *
     */
    private final ChannelHandlerContext context;


    public TelnetConnection(TelnetHandler handler, ChannelHandlerContext context) {
        this.status = DATA;
        this.paramsOptionCode = null;
        this.paramsBuffer = null;
        this.paramsIac = false;
        this.sendBinary = false;
        this.receiveBinary = false;
        this.handler = handler;
        this.context = context;
    }

    /**
     * 连接初始化
     */
    public void onInit() {
        handler.onOpen(this);
    }

    /**
     * 处理接受数据
     *
     * @param data 数据
     */
    public void receive(byte[] data) {

        for (byte b : data) {
            status.handle(this, b);
        }

        flushDataIfNecessary();
    }

    /**
     * 关闭连接
     */
    public void onClose() {
        handler.onClose();
    }

    /**
     * 发送数据
     *
     * @param data 数据
     */
    public void send(byte[] data) {
        context.writeAndFlush(Unpooled.buffer().writeBytes(data));
    }

    /**
     * 执行任务
     *
     * @param task 任务
     */
    public void execute(Runnable task) {
        context.channel().eventLoop().execute(task);
    }

    /**
     * 周期执行任务
     *
     * @param task  任务
     * @param delay 周期
     * @param unit  时间单位 {@link TimeUnit}
     */
    public void schedule(Runnable task, long delay, TimeUnit unit) {
        context.channel().eventLoop().schedule(task, delay, unit);
    }

    /**
     * handle option call back by option code
     *
     * @param optionCode option code
     * @param status     status
     */
    public void onOptionTypeFunction(byte optionCode, TelnetConnectStatus status) {
        Option option = Option.getOptionByCode(optionCode);
        assert option != null;
        switch (status) {
            case DONT:
                option.handleDont(this);
                break;
            case DO:
                option.handleDo(this);
                send(new byte[]{BYTE_IAC.code, BYTE_WONT.code, optionCode});
                break;
            case WILL:
                option.handleWill(this);
                send(new byte[]{BYTE_IAC.code, BYTE_DONT.code, optionCode});
                break;
            case WONT:
                option.handleWont(this);
                break;
            default:
        }
    }

    public void writeOptionFunction(TelnetOrderEnum telnetOrder, Option option) {
        switch (telnetOrder) {
            // Write a do or will option request to the client.
            case BYTE_DO:
            case BYTE_WILL:
                send(new byte[]{BYTE_IAC.code, telnetOrder.code, option.code});
                break;
            default:
                break;
        }
    }

    /**
     * append a byte to {@link #paramsBuffer}
     *
     * @param b a byte
     */
    public void appendToParams(byte b) {
        while (paramsLength >= paramsBuffer.length) {
            paramsBuffer = Arrays.copyOf(paramsBuffer, paramsBuffer.length + 100);
        }
        paramsBuffer[paramsLength++] = b;
    }


    /**
     * 拼接一个字符在 {@link #pendingBuffer}里.
     * 当 {@link #pendingBuffer} 满了，刷新数据
     * is flushed.
     *
     * @param b the byte
     * @see #flushData()
     */
    public void appendData(byte b) {
        if (pendingLength >= pendingBuffer.length) {
            flushData();
        }
        pendingBuffer[pendingLength++] = b;
    }


    /**
     * 当 {@link #pendingBuffer}中数据不为空的时候 刷新数据
     *
     * @see #flushData()
     */
    public void flushDataIfNecessary() {
        if (pendingLength > 0) {
            flushData();
        }
    }

    /**
     * 刷新 {@link #pendingBuffer}到{@link TelnetHandler#onData(byte[])}.
     */
    private void flushData() {
        byte[] data = Arrays.copyOf(pendingBuffer, pendingLength);
        pendingLength = 0;
        handler.onData(data);
    }

    /**
     * Handle option parameters call back.
     *
     * @param optionCode the option code
     * @param parameters 参数
     */
    public void onOptionParameters(byte optionCode, byte[] parameters) {
        Option option = Option.getOptionByCode(optionCode);
        assert option != null;
        option.handleParameters(this, parameters);
    }

}
