package com.rhine.terminal.readline;

import java.util.function.Consumer;

/**
 * @author LDZ
 * @date 2019-11-11 17:43
 */
public class EventDecoder implements Consumer<int[]> {

    private Consumer<int[]> readHandler;


    @Override
    public void accept(int[] data) {
        readHandler.accept(data);
    }

    public EventDecoder setReadHandler(Consumer<int[]> readHandler) {
        this.readHandler = readHandler;
        return this;
    }

    public Consumer<int[]> getReadHandler() {
        return readHandler;
    }
}
