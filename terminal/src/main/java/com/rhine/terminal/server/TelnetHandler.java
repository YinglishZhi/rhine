package com.rhine.terminal.server;

/**
 * The handler that defines the callbacks for a telnet connection.
 *
 * @author LDZ
 * @date 2019-11-01 15:48
 */
public class TelnetHandler {


    public void onOpen(TelnetConnection connection) {

    }

    /**
     * process data send by the client
     *
     * @param data the data
     */
    public void onData(byte[] data) {

    }

    public void onClose() {

    }

    protected void onSize(int width, int height) {}
}
