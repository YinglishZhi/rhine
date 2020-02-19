package com.rhine.terminal.server;


import com.rhine.terminal.RhineServer111;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@Ignore
public class RhineServerTest {
    private RhineServer111 rhineServer = null;

    @Before
    public void setUp() {
        rhineServer = new RhineServer111(null).setHost("localhost").setPort(8888);
    }

    @Test
    public void open() {
        rhineServer.open(
                () -> new TelnetHandler() {
                    @Override
                    public void onOpen(TelnetConnection connection) {
                        log.info("connection open");
                    }

                    @Override
                    public void onData(byte[] data) {
                        log.info(new String(data, UTF_8));
                    }
                }
        );



    }

    @Test
    public void close() {
    }
}
