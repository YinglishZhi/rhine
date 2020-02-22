package com.rhine.terminal.io;

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
            int prev = 0;
            int ptr = 0;
            while (ptr < data.length) {
                int cp = data[ptr];
                if (cp == '\n') {
                    if (ptr > prev) {
                        sendChunk(data, prev, ptr);
                    }
                    readHandler.accept(new int[]{'\r', '\n'});
                    prev = ++ptr;
                } else {
                    ptr++;
                }
            }
            if (ptr > prev) {
                sendChunk(data, prev, ptr);
            }
        }
    }

    private void sendChunk(int[] data, int prev, int ptr) {
        int len = ptr - prev;
        int[] buf = new int[len];
        System.arraycopy(data, prev, buf, 0, len);
        readHandler.accept(buf);
    }
}
