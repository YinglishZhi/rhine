package com.rhine.terminal.readline.key;

import java.nio.IntBuffer;

/**
 * key 事件
 *
 * @author LDZ
 * @date 2020-02-20 19:34
 */
public interface KeyEvent {

    default IntBuffer buffer() {
        int length = length();
        IntBuffer buf = IntBuffer.allocate(length);
        for (int i = 0; i < length; i++) {
            buf.put(getCodePointAt(i));
        }
        buf.flip();
        return buf;
    }

    int getCodePointAt(int index) throws IndexOutOfBoundsException;

    int length();
}
