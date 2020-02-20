/*
 * Copyright 2015 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rhine.terminal.readline.key;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析 inputrc
 *
 * @author LDZ
 * @date 2020-02-20 19:49
 */
@Slf4j
abstract class InputrcParser {

    private static final Pattern COMMENT = Pattern.compile("#.*");
    private static final Pattern CONDITIONAL = Pattern.compile("\\$.*");
    private static final Pattern SET_VARIABLE = Pattern.compile("set\\s+(\\S)+\\s+(\\S)+\\s*");
    private static final Pattern BIND = Pattern.compile("(?:(?:\"(.*)\")|(.*))" + ":\\s*" + "(?:(?:\"(.*)\")|(?:'(.*)')|(\\S+))" + "\\s*");
    private static final Pattern A = Pattern.compile("^\\\\([0-9]{1,3})");
    private static final Pattern B = Pattern.compile("^\\\\x([0-9,A-F,a-f]{1,2})");

    static void parse(String s, InputrcParser handler) throws UnsupportedEncodingException {
        parse(new ByteArrayInputStream(s.getBytes(StandardCharsets.US_ASCII.name())), handler);
    }

    static void parse(InputStream s, InputrcParser handler) {

        Scanner sc = new Scanner(s, "US-ASCII").useDelimiter("\n");
        while (sc.hasNext()) {
            Matcher matcher;
            String next = sc.next();
            if (COMMENT.matcher(next).matches()) {
                log.warn("Inputrc comment not implemented");
            } else if (CONDITIONAL.matcher(next).matches()) {
                log.warn("Inputrc conditional not implemented");
            } else if (SET_VARIABLE.matcher(next).matches()) {
                log.warn("Inputrc set variable not implemented");
            } else if ((matcher = BIND.matcher(next)).matches()) {
                String keyseq = matcher.group(1);
                String keyName = matcher.group(2);
                String macro1 = matcher.group(3);
                String macro2 = matcher.group(4);
                String functionName = matcher.group(5);
                if (keyseq != null) {
                    int[] f = parseKeySeq(keyseq);
                    if (functionName != null) {
                        handler.bindFunction(f, functionName);
                    } else if (macro1 != null) {
                        handler.bindMacro(f, macro1);
                    } else {
                        handler.bindMacro(f, macro2);
                    }
                } else {
                    if (functionName != null) {
                        handler.bindFunction(keyName, functionName);
                    } else if (macro1 != null) {
                        handler.bindMacro(keyName, macro1);
                    } else {
                        handler.bindMacro(keyName, macro2);
                    }
                }
            }
        }
    }


    private void bindMacro(String keyName, String macro) {
    }

    private void bindFunction(String keyName, String functionName) {
    }

    private void bindMacro(int[] keySequence, String macro) {
    }

    void bindFunction(int[] keySequence, String functionName) {
    }

    static Keymap create() {
        InputStream inputrc = InputrcParser.class.getResourceAsStream("inputrc");
        return new Keymap(inputrc);
    }

    static int[] parseKeySeq(String keyseq) {
        ArrayList<Integer> builder = new ArrayList<>();
        while (keyseq.length() > 0) {
            if (keyseq.startsWith("\\C-") && keyseq.length() > 3) {
                int c = (Character.toUpperCase(keyseq.charAt(3)) - '@') & 0x7F;
                builder.add(c);
                keyseq = keyseq.substring(4);
            } else if (keyseq.startsWith("\\M-") && keyseq.length() > 3) {
                int c = (Character.toUpperCase(keyseq.charAt(3)) - '@') & 0x7F;
                builder.add(27);
                builder.add(c);
                keyseq = keyseq.substring(4);
            } else if (keyseq.startsWith("\\e")) {
                builder.add(27);
                keyseq = keyseq.substring(2);
            } else if (keyseq.startsWith("\\\\")) {
                builder.add((int) '\\');
                keyseq = keyseq.substring(2);
            } else if (keyseq.startsWith("\\\"")) {
                builder.add((int) '"');
                keyseq = keyseq.substring(2);
            } else if (keyseq.startsWith("\\'")) {
                builder.add((int) '\'');
                keyseq = keyseq.substring(2);
            } else if (keyseq.startsWith("\\a")) {
                builder.add(7);
                keyseq = keyseq.substring(2);
            } else if (keyseq.startsWith("\\b")) {
                builder.add(8);
                keyseq = keyseq.substring(2);
            } else if (keyseq.startsWith("\\d")) {
                builder.add(127);
                keyseq = keyseq.substring(2);
            } else if (keyseq.startsWith("\\f")) {
                builder.add(12);
                keyseq = keyseq.substring(2);
            } else if (keyseq.startsWith("\\n")) {
                builder.add(10);
                keyseq = keyseq.substring(2);
            } else if (keyseq.startsWith("\\r")) {
                builder.add(13);
                keyseq = keyseq.substring(2);
            } else if (keyseq.startsWith("\\t")) {
                builder.add(9);
                keyseq = keyseq.substring(2);
            } else if (keyseq.startsWith("\\v")) {
                builder.add(11);
                keyseq = keyseq.substring(2);
            } else {
                Matcher matcher = A.matcher(keyseq);
                if (matcher.find()) {
                    builder.add(Integer.parseInt(matcher.group(1), 8));
                    keyseq = keyseq.substring(matcher.end());
                } else {
                    matcher = B.matcher(keyseq);
                    if (matcher.find()) {
                        builder.add(Integer.parseInt(matcher.group(1), 16));
                        keyseq = keyseq.substring(matcher.end());
                    } else {
                        builder.add((int) keyseq.charAt(0));
                        keyseq = keyseq.substring(1);
                    }
                }
            }
        }
        int[] f = new int[builder.size()];
        for (int i = 0; i < builder.size(); i++) {
            f[i] = builder.get(i);
        }
        return f;
    }
}
