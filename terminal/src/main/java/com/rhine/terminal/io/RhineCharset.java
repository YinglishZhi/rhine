package com.rhine.terminal.io;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

/**
 * 自定义的charset
 *
 * @author LDZ
 * @date 2019-11-11 17:15
 */
public class RhineCharset extends Charset {

    public static final Charset INSTANCE = new RhineCharset();

    private RhineCharset() {
        super("RHINE", new String[0]);
    }

    @Override
    public boolean contains(Charset cs) {
        return false;
    }

    @Override
    public CharsetDecoder newDecoder() {
        return new CharsetDecoder(this, 1.0f, 1.0f) {
            private boolean prevCR;

            @Override
            protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
                int pos = in.position();
                int limit = in.limit();
                try {
                    while (pos < limit) {
                        byte b = in.get(pos);
                        char c;
                        if (b >= 0) {
                            if (prevCR && (b == '\n' || b == 0)) {
                                pos++;
                                prevCR = false;
                                continue;
                            }
                            c = (char) b;
                            prevCR = b == '\r';
                        } else {
                            c = (char) (256 + b);
                        }
                        if (out.position() >= out.limit()) {
                            return CoderResult.OVERFLOW;
                        }
                        pos++;
                        out.put(c);
                    }
                    return CoderResult.UNDERFLOW;
                } finally {
                    in.position(pos);
                }
            }
        };
    }

    @Override
    public CharsetEncoder newEncoder() {
        throw new UnsupportedOperationException();
    }
}
