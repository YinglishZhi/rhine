package com.rhine.terminal.readline;

import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.function.Consumer;

/**
 * @author LDZ
 * @date 2019-11-11 15:31
 */
public class BinaryDecoder {

    private static final ByteBuffer EMPTY = ByteBuffer.allocate(0);

    private CharsetDecoder decoder;
    private ByteBuffer byteBuffer;

    private final CharBuffer charBuffer;
    private final Consumer<int[]> onChar;

    public BinaryDecoder(Charset charset, Consumer<int[]> onChar) {
        this(2, charset, onChar);
    }

    public BinaryDecoder(int initialSize, Charset charset, Consumer<int[]> onChar) {
        if (initialSize < 2) {
            throw new IllegalArgumentException("Initial size must be at least 2");
        }
        decoder = charset.newDecoder();
        decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
        decoder.onMalformedInput(CodingErrorAction.REPLACE);

        byteBuffer = EMPTY;
        charBuffer = CharBuffer.allocate(initialSize);
        this.onChar = onChar;
    }

    public void write(byte[] data) {
        write(data, 0, data.length);

    }

    private void write(byte[] data, int start, int len) {

        int remaining = byteBuffer.remaining();

        if (len > remaining) {
            ByteBuffer tmp = byteBuffer;
            int length = tmp.position() + len;
            byteBuffer = ByteBuffer.allocate(length);
            tmp.flip();
            byteBuffer.put(tmp);
        }
        byteBuffer.put(data, start, len);
        byteBuffer.flip();

        while (true) {
            IntBuffer intBuffer = IntBuffer.allocate(byteBuffer.remaining());
            CoderResult result = decoder.decode(byteBuffer, charBuffer, false);
            charBuffer.flip();

            while (charBuffer.hasRemaining()) {
                char c = charBuffer.get();
                intBuffer.put((int) c);
            }

            intBuffer.flip();

            int[] codePoints = new int[intBuffer.limit()];
            intBuffer.get(codePoints);
            onChar.accept(codePoints);
            charBuffer.compact();
            if (result.isOverflow()) {

            } else if (result.isUnderflow()) {
                break;
            } else {
                throw new UnsupportedOperationException("handle me gracefully");
            }
        }
        byteBuffer.compact();
    }
}
