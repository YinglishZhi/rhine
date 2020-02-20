package com.rhine.terminal.readline;

import com.rhine.terminal.api.TtyConnection;

import javax.annotation.processing.Completion;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.IntStream;

/**
 * 按行读取
 *
 * @author LDZ
 * @date 2019-11-11 17:51
 */
public class ReadLine {

    private Interaction interaction;

    public void readline(TtyConnection connection, String prompt, Consumer<String> requestHandler) {

        install(connection);
        connection.write(prompt);

        System.out.println("111");

    }


    private void install(TtyConnection connection) {
        connection.setReadHandler(data -> {
            synchronized (ReadLine.class) {
                for (int d : data) {
                    System.out.println("read line" + (char) d);
                }
            }
            connection.write("$$$");
        });
    }

    /**
     * 交互类
     *
     * @author LDZ
     * @date 2020-02-20 11:37
     */
    class Interaction {
        final TtyConnection conn;
        private Consumer<int[]> prevReadHandler;
        private Consumer<Vector> prevSizeHandler;
        private final String prompt;
        private final Consumer<String> requestHandler;
        private final Map<String, Object> data;
        private int historyIndex = -1;
        private String currentPrompt;
        private boolean paused;

        private Interaction(
                TtyConnection conn,
                String prompt,
                Consumer<String> requestHandler,
                Consumer<Completion> completionHandler) {
            this.conn = conn;
            this.prompt = prompt;
            this.data = new HashMap<>();
            this.currentPrompt = prompt;
            this.requestHandler = requestHandler;
        }

        /**
         * End the current interaction with a callback.
         *
         * @param s the
         */
        private boolean end(String s) {
            synchronized (ReadLine.this) {
                if (interaction == null) {
                    return false;
                }
                interaction = null;
                conn.setStdinHandler(prevReadHandler);
            }
            requestHandler.accept(s);
            return true;
        }


    }

}
