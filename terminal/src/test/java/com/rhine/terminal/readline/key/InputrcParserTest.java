package com.rhine.terminal.readline.key;

import org.junit.Test;

public class InputrcParserTest {


    @Test
    public void create() {

        Keymap keymap = InputrcParser.create();
        System.out.println(keymap.bindings.size());
    }
}
