package com.rhine.terminal.readline;

import com.rhine.terminal.api.TtyConnection;
import com.rhine.terminal.readline.key.EventQueue;
import com.rhine.terminal.readline.key.FunctionEvent;
import com.rhine.terminal.readline.key.KeyEvent;
import com.rhine.terminal.readline.key.Keymap;
import com.rhine.terminal.readline.line.LineBuffer;
import com.rhine.terminal.readline.line.LineStatus;
import com.rhine.terminal.util.Vector;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.IntStream;

/**
 * 按行读取
 *
 * @author LDZ
 * @date 2019-11-11 17:51
 */
@Slf4j
public class ReadLine {

    private final Map<String, ReadLineFunction> functions = new HashMap<>();

    private Vector size;

    private List<int[]> history;

    private final EventQueue decoder;

    /**
     * add a read line function
     *
     * @param function read line function
     */
    private void addFunction(ReadLineFunction function) {
        functions.put(function.name(), function);
    }

    public ReadLine addFunctions(Iterable<ReadLineFunction> functions) {
        for (ReadLineFunction function : functions) {
            addFunction(function);
        }
        return this;
    }


    public ReadLine(Keymap keymap) {
//    this.device = TermInfo.defaultInfo().getDevice("xterm"); // For now use xterm
        this.decoder = new EventQueue(keymap);
        this.history = new ArrayList<>();
        addFunction(ACCEPT_LINE);
    }

    private Interaction interaction;

    public void readline(TtyConnection connection, String prompt, Consumer<String> requestHandler) {

        synchronized (this) {
            if (interaction != null) {
                throw new IllegalStateException("Already reading a line");
            }
            interaction = new Interaction(connection, prompt, requestHandler);
        }
        interaction.install();
        connection.write(prompt);

    }


    private void deliver() {
        while (true) {
            Interaction handler;
            KeyEvent event;
            synchronized (this) {
                if (decoder.hasNext() && interaction != null && !interaction.paused) {
                    event = decoder.next();
                    handler = interaction;
                } else {
                    return;
                }
            }
            handler.handle(event);
        }
    }

    /**
     * 交互类
     *
     * @author LDZ
     * @date 2020-02-20 11:37
     */
    public class Interaction {

        /**
         * tty connection
         */
        final TtyConnection conn;

        /**
         * 前缀
         */
        private final String prompt;

        /**
         * 数据
         */
        private final Map<String, Object> data;

        /**
         * 请求执行句柄
         */
        private final Consumer<String> requestHandler;

        /**
         * line
         */
        private final LineBuffer line = new LineBuffer();

        /**
         * line buffer
         */
        private final LineBuffer buffer = new LineBuffer();

        /**
         * 历史 index
         */
        private int historyIndex = -1;

        /**
         * 当前前缀
         */
        private String currentPrompt;

        /**
         * 暂停
         */
        private boolean paused;

        private Interaction(
                TtyConnection conn,
                String prompt,
                Consumer<String> requestHandler) {
            this.conn = conn;
            this.prompt = prompt;
            this.data = new HashMap<>();
            this.requestHandler = requestHandler;
            this.currentPrompt = prompt;
        }

        /**
         * 结束当前交互
         *
         * @param s the
         */
        private boolean end(String s) {
            synchronized (ReadLine.this) {
                if (interaction == null) {
                    return false;
                }
                interaction = null;
            }
            requestHandler.accept(s);
            return true;
        }


        private void handle(KeyEvent event) {
            // 特殊情况
            if (event.length() == 1) {
                if (event.getCodePointAt(0) == 4 && buffer.getSize() == 0) {
                    // 输入ctrl-D 并且是个空行的话，么得字符可以删除，直接结束本次会话
                    end(null);
                    return;
                } else if (event.getCodePointAt(0) == 3) {
                    // ctrl-c 清空
                    line.clear();
                    buffer.clear();
                    data.clear();
                    historyIndex = -1;
                    currentPrompt = prompt;
                    conn.stdoutHandler().accept(new int[]{'\n'});
                    conn.write(interaction.prompt);
                    return;
                }
            }

            if (event instanceof FunctionEvent) {
                // 定义组合键的事件触发
                FunctionEvent functionName = (FunctionEvent) event;
                ReadLineFunction function = functions.get(functionName.name());
                if (function != null) {
                    synchronized (this) {
                        paused = true;
                    }
                    function.apply(this);
                } else {
                    log.warn("Unimplemented function = {}", functionName.name());
                }
            } else {
                // 输入的普通字符
                LineBuffer buf = buffer.copy();
                for (int i = 0; i < event.length(); i++) {
                    int codePoint = event.getCodePointAt(i);
                    try {
                        buf.insert(codePoint);
                    } catch (IllegalArgumentException e) {
                        conn.stdoutHandler().accept(new int[]{'\007'});
                    }
                }
                refresh(buf);
            }
        }

        /**
         * Refresh the current buffer with the argument buffer.
         *
         * @param buffer the new buffer
         */
        public Interaction refresh(LineBuffer buffer) {
            // size 初始化
            refresh(buffer, size.x());
            return this;
        }

        private void refresh(LineBuffer update, int width) {
            IntStream.Builder consumer = IntStream.builder();

            LineBuffer copy3 = new LineBuffer();
            copy3.insert(currentPrompt.codePoints().toArray());
            copy3.insert(buffer().toArray());
            copy3.setCursor(currentPrompt.length() + buffer().getCursor());

            LineBuffer copy2 = new LineBuffer();
            copy2.insert(currentPrompt.codePoints().toArray());
            copy2.insert(update.toArray());
            copy2.setCursor(currentPrompt.length() + update.getCursor());

            copy3.update(copy2, d -> {
                for (int cp : d) {
                    consumer.accept(cp);
                }
            }, width);
            // 在这输出的
            conn.stdoutHandler().accept(consumer.build().toArray());
            buffer.clear();
            buffer.insert(update.toArray());
            buffer.setCursor(update.getCursor());
        }


        public void resume() {
            synchronized (ReadLine.this) {
                if (!paused) {
                    throw new IllegalStateException();
                }
                paused = false;
            }
        }

        private void install() {
            conn.setStdinHandler(data -> {
                synchronized (ReadLine.this) {
                    decoder.append(data);
                }
                deliver();
            });
            size = conn.size();
            conn.setSizeHandler(dim -> {
                if (size != null) {
                    // Not supported for now
                    // interaction.resize(size.width(), dim.width());
                }
                size = dim;
            });
        }
        // get

        public LineBuffer buffer() {
            return buffer;
        }

        public List<int[]> history() {
            return history;
        }

        public int getHistoryIndex() {
            return historyIndex;
        }

        public void setHistoryIndex(int historyIndex) {
            this.historyIndex = historyIndex;
        }


        public Map<String, Object> data() {
            return data;
        }

    }


    // Need to access internal state
    private final ReadLineFunction ACCEPT_LINE = new ReadLineFunction() {

        @Override
        public String name() {
            return "accept-line";
        }

        @Override
        public void apply(Interaction interaction) {
            interaction.line.insert(interaction.buffer.toArray());
            LineStatus pb = new LineStatus();
            for (int i = 0; i < interaction.line.getSize(); i++) {
                pb.accept(interaction.line.getAt(i));
            }
            interaction.buffer.clear();
            if (pb.isEscaping()) {
                interaction.line.delete(-1); // Remove \
                interaction.currentPrompt = "> ";
                interaction.conn.write("\n> ");
                interaction.resume();
            } else {
                if (pb.isQuoted()) {
                    interaction.line.insert('\n');
                    interaction.conn.write("\n> ");
                    interaction.currentPrompt = "> ";
                    interaction.resume();
                } else {
                    String raw = interaction.line.toString();
                    if (interaction.line.getSize() > 0) {
                        history.add(0, interaction.line.toArray());
                    }
                    interaction.line.clear();
                    interaction.conn.write("\n");
                    interaction.end(raw);
                }
            }
        }
    };

}
