package com.rhine.terminal.readline;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author LDZ
 * @date 2019-11-11 17:43
 */
public class EventDecoder implements Consumer<int[]> {

    private Consumer<int[]> readHandler;
    private final int vintr;
    private final int veof;
    private final int vsusp;

    public EventDecoder(int vintr, int vsusp, int veof) {
        this.vintr = vintr;
        this.vsusp = vsusp;
        this.veof = veof;
    }

    public Consumer<int[]> getReadHandler() {
        return readHandler;
    }

    public void setReadHandler(Consumer<int[]> readHandler) {
        this.readHandler = readHandler;
    }


    @Override
    public void accept(int[] data) {
        if (readHandler != null && data.length > 0) {
            readHandler.accept(data);
        }
    }
}
