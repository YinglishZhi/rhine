package com.rhine.terminal.readline;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

/**
 * @author LDZ
 * @date 2019-11-11 17:15
 */
public class RhineCharset extends Charset {

    public static final Charset INSTANCE = new RhineCharset();

    public RhineCharset() {
        super("RHINE", new String[0]);
    }

    @Override
    public boolean contains(Charset cs) {
        return false;
    }

    @Override
    public CharsetDecoder newDecoder() {
        return null;
    }

    @Override
    public CharsetEncoder newEncoder() {
        return null;
    }
}
