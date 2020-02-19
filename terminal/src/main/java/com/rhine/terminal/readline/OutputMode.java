package com.rhine.terminal.readline;

import java.util.function.Consumer;

/**
 * @author LDZ
 * @date 2019-11-08 18:00
 */
public class OutputMode implements Consumer<int[]> {

    private final Consumer<int[]> readHandler;

    public OutputMode(Consumer<int[]> readHandler) {
        this.readHandler = readHandler;
    }

    @Override
    public void accept(int[] data) {
        if (readHandler != null && data.length > 0) {
            readHandler.accept(new int[]{'\r', '\n'});
            readHandler.accept(data);
        }
    }
}
