package com.rhine.terminal.api;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * A connection to a tty
 *
 * @author LDZ
 * @date 2019-11-08 14:09
 */
public interface TtyConnection {


    /**
     * 获取输入句柄
     *
     * @return 输入句柄
     */
    Consumer<int[]> getStdinHandler();

    /**
     * 输入的句柄
     *
     * @param handler 输入句柄
     */
    void setStdinHandler(Consumer<int[]> handler);

    /**
     * 输出的句柄
     *
     * @return 输出的句柄
     */
    Consumer<int[]> stdoutHandler();


    default void write(String s) {
        stdoutHandler().accept(s.codePoints().toArray());
    }

    /**
     * 安排一个任务执行
     *
     * @param task 任务
     */
    void execute(Runnable task);

    /**
     * 安排一个任务执行
     *
     * @param task  任务
     * @param delay 延时时间
     * @param unit  时间单位
     */
    void schedule(Runnable task, long delay, TimeUnit unit);


    /**
     * 设置读句柄
     */
    void setReadHandler(Consumer<int[]> handler);


    /**
     * 获取读句柄
     */
    Consumer<int[]> getReadHandler();

}
