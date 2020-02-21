package com.rhine.terminal.server.netty;

import com.rhine.terminal.server.netty.TelnetConnection;

/**
 * The handler that defines the callbacks for a telnet connection.
 *
 * @author LDZ
 * @date 2019-11-01 15:48
 */
public abstract class TelnetHandler {


    public abstract void onOpen(TelnetConnection connection);

    /**
     * process data send by the client
     *
     * @param data the data
     */
    public abstract void onData(byte[] data);

    public abstract void onClose();

    public abstract void onSize(int width, int height);
}
