package com.rhine.core.server;

import com.rhine.terminal.RhineServer;
import com.rhine.terminal.api.TtyConnection;
import com.rhine.terminal.readline.ReadLine;
import com.rhine.terminal.readline.key.Keymap;
import com.rhine.terminal.readline.key.ReadLineFunction;
import com.rhine.terminal.util.Helper;

import java.lang.instrument.Instrumentation;
import java.util.List;

/**
 * @author LDZ
 * @date 2020-02-23 16:26
 */
public class RhineServiceBootstrap {

    private Instrumentation instrumentation;

    public RhineServiceBootstrap() {
    }

    public RhineServiceBootstrap(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }

    private static volatile RhineServiceBootstrap rhineServiceBootstrap;

    public static RhineServiceBootstrap getInstance(Instrumentation instrumentation) {
        if (null == rhineServiceBootstrap) {
            synchronized (RhineServiceBootstrap.class) {
                if (null == rhineServiceBootstrap) {
                    rhineServiceBootstrap = new RhineServiceBootstrap(instrumentation);
                }
            }
        }
        return rhineServiceBootstrap;
    }

    public static void main(String[] args) {

        RhineServiceBootstrap rhineServiceBootstrap = new RhineServiceBootstrap();
        rhineServiceBootstrap.bind();

    }

    public void bind() {
        RhineServer rhineServer = new RhineServer()
                .setHost("127.0.0.1")
                .setPort(1234);

        rhineServer.open(Handler::handler);
    }

    static class Handler {
        static void handler(TtyConnection connection) {

            List<ReadLineFunction> readlineFunctions
                    = Helper.loadServices(ReadLineFunction.class.getClassLoader(), ReadLineFunction.class);
            ReadLine readLine = new ReadLine(Keymap.getDefault()).addFunctions(readlineFunctions);
            readline(readLine, connection);
        }

        static void readline(ReadLine readLine, TtyConnection connection) {
            readLine.readline(connection, "#", line -> {
                if (null == line) {
                    connection.write("777");
                } else {
                    connection.write("user entered" + line + "\n");
                    readline(readLine, connection);
                }

            });
        }
    }
}
