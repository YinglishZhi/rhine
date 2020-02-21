package com.rhine.terminal.server;


import com.rhine.terminal.RhineServer;
import com.rhine.terminal.server.netty.RhineTelnetConnection;
import com.rhine.terminal.api.TtyConnection;
import com.rhine.terminal.readline.ReadLine;
import com.rhine.terminal.readline.key.Keymap;
import com.rhine.terminal.server.netty.TelnetHandler;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.function.Supplier;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@Ignore
public class RhineServerTest {

    @Before
    public void setUp() {

    }

    public static void main(String[] args) {
        RhineServer rhineServer = new RhineServer().setHost("127.0.0.1").setPort(2134);
        rhineServer.open(Handler::handler);
    }

    @Test
    public void tty() {
        Supplier<TelnetHandler> handlerSupplier = () -> new RhineTelnetConnection(false, false, UTF_8, Handler::handler);
        TelnetHandler handler = handlerSupplier.get();
        System.out.println(handler);
    }


    static class Handler {
        static void handler(TtyConnection connection) {
            readline(new ReadLine(Keymap.getDefault()), connection);
        }

        static void readline(ReadLine readLine, TtyConnection connection) {
            readLine.readline(connection, "**", line -> {
                if (null == line) {
                    connection.write("777");
                } else {
                    connection.write("user entered" + line + "\n");
                    readline(readLine, connection);
                }

            });
        }
    }

    @Test
    public void close() {
    }
}
