package com.rhine.terminal.readline;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * @author LDZ
 * @date 2019-11-08 18:03
 */
public class BinaryEncoder implements Consumer<int[]> {

    private volatile Charset charset;
    private final Consumer<byte[]> onBytes;

    public BinaryEncoder(Charset charset, Consumer<byte[]> onBytes) {
        this.charset = charset;
        this.onBytes = onBytes;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    @Override
    public void accept(int[] codePoints) {
        final char[] tmp = new char[2];
        int capacity = 0;
        for (int codePoint : codePoints) {
            capacity += Character.charCount(codePoint);
        }
        CharBuffer charBuf = CharBuffer.allocate(capacity);
        for (int codePoint : codePoints) {
            int size = Character.toChars(codePoint, tmp, 0);
            charBuf.put(tmp, 0, size);
        }
        charBuf.flip();
        ByteBuffer bytesBuf = charset.encode(charBuf);
        byte[] bytes = bytesBuf.array();
        if (bytesBuf.limit() < bytesBuf.array().length) {
            bytes = Arrays.copyOf(bytes, bytesBuf.limit());
        }
        onBytes.accept(bytes);
    }
}
