package com.rhine.terminal.server;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * telnet的连接
 *
 * @author LDZ
 * @date 2019-11-01 15:45
 */
public class TelnetConnection {


    public static final byte BYTE_IAC = (byte) 0xFF;
    public static final byte BYTE_DONT = (byte) 0xFE;
    public static final byte BYTE_DO = (byte) 0xFD;
    public static final byte BYTE_WONT = (byte) 0xFC;
    public static final byte BYTE_WILL = (byte) 0xFB;
    public static final byte BYTE_SB = (byte) 0xFA;
    public static final byte BYTE_SE = (byte) 0xF0;

    /**
     * connect status
     */
    private byte[] pendingBuffer = new byte[256];
    private int pendingLength = 0;
    Status status;
    Byte paramsOptionCode;
    byte[] paramsBuffer;
    int paramsLength;
    boolean paramsIac;
    boolean sendBinary;
    boolean receiveBinary;
    final TelnetHandler handler;

    final ChannelHandlerContext context;

    TelnetConnection(TelnetHandler handler, ChannelHandlerContext context) {
        this.status = Status.DATA;
        this.paramsOptionCode = null;
        this.paramsBuffer = null;
        this.paramsIac = false;
        this.sendBinary = false;
        this.receiveBinary = false;
        this.handler = handler;
        this.context = context;
    }

    void onInit() {
        handler.onOpen(this);
    }

    void receive(byte[] data) {

        for (byte b : data) {
            status.handle(this, b);
        }


        handler.onData(data);
    }

    void onClose() {
        handler.onClose();
    }

    public void send(byte[] data) {
        context.writeAndFlush(Unpooled.buffer().writeBytes(data));
    }

    public void execute(Runnable task) {
        context.channel().eventLoop().execute(task);
    }

    public void schedule(Runnable task, long delay, TimeUnit unit) {
        context.channel().eventLoop().schedule(task, delay, unit);
    }


    /**
     * Handle option <code>DO</code> call back. The implementation will try to find a matching option
     * via the {@code Option#values()} and invoke it's {@link Option#handleDo(TelnetConnection)} method
     * otherwise a <code>WON'T</code> will be sent to the client.<p>
     *
     * This method can be subclassed to handle an option.
     *
     * @param optionCode the option code
     */
    protected void onOptionDo(byte optionCode) {
        for (Option option : Option.values()) {
            if (option.code == optionCode) {
                option.handleDo(this);
                return;
            }
        }
        send(new byte[]{BYTE_IAC, BYTE_WONT, optionCode});
    }

    /**
     * Handle option <code>DON'T</code> call back. The implementation will try to find a matching option
     * via the {@code Option#values()} and invoke it's {@link Option#handleDont(TelnetConnection)} method.<p>
     *
     * This method can be subclassed to handle an option.
     *
     * @param optionCode the option code
     */
    protected void onOptionDont(byte optionCode) {
        for (Option option : Option.values()) {
            if (option.code == optionCode) {
                option.handleDont(this);
                return;
            }
        }
    }

    /**
     * Handle option <code>WILL</code> call back. The implementation will try to find a matching option
     * via the {@code Option#values()} and invoke it's {@link Option#handleWill(TelnetConnection)} method
     * otherwise a <code>DON'T</code> will be sent to the client.<p>
     *
     * This method can be subclassed to handle an option.
     *
     * @param optionCode the option code
     */
    protected void onOptionWill(byte optionCode) {
        for (Option option : Option.values()) {
            if (option.code == optionCode) {
                option.handleWill(this);
                return;
            }
        }
        send(new byte[]{BYTE_IAC, BYTE_DONT, optionCode});
    }
    /**
     * Handle option <code>WON'T</code> call back. The implementation will try to find a matching option
     * via the {@code Option#values()} and invoke it's {@link Option#handleWont(TelnetConnection)} method.<p>
     *
     * This method can be subclassed to handle an option.
     *
     * @param optionCode the option code
     */
    protected void onOptionWont(byte optionCode) {
        for (Option option : Option.values()) {
            if (option.code == optionCode) {
                option.handleWont(this);
                return;
            }
        }
    }

    private void appendToParams(byte b) {
        while (paramsLength >= paramsBuffer.length) {
            paramsBuffer = Arrays.copyOf(paramsBuffer, paramsBuffer.length + 100);
        }
        paramsBuffer[paramsLength++] = b;
    }


    /**
     * Append a byte in the {@link #pendingBuffer} buffer. When the {@link #pendingBuffer} buffer is full, data
     * is flushed.
     *
     * @param b the byte
     * @see #flushData()
     */
    private void appendData(byte b) {
        if (pendingLength >= pendingBuffer.length) {
            flushData();
        }
        pendingBuffer[pendingLength++] = b;
    }


    /**
     * Flush the {@link #pendingBuffer} buffer when it is not empty.
     *
     * @see #flushData()
     */
    private void flushDataIfNecessary() {
        if (pendingLength > 0) {
            flushData();
        }
    }

    /**
     * Flush the {@link #pendingBuffer} buffer to {@link TelnetHandler#onData(byte[])}.
     */
    private void flushData() {
        byte[] data = Arrays.copyOf(pendingBuffer, pendingLength);
        pendingLength = 0;
        handler.onData(data);
    }

    /**
     * Handle option parameters call back. The implementation will try to find a matching option
     * via the {@code Option#values()} and invoke it's {@link Option#handleParameters(TelnetConnection, byte[])} method.
     *
     * This method can be subclassed to handle an option.
     *
     * @param optionCode the option code
     */
    protected void onOptionParameters(byte optionCode, byte[] parameters) {
        for (Option option : Option.values()) {
            if (option.code == optionCode) {
                option.handleParameters(this, parameters);
                return;
            }
        }
    }

    // ====== Status =======
    enum Status {

        DATA() {
            @Override
            void handle(TelnetConnection session, byte b) {
                if (b == BYTE_IAC) {
                    if (session.receiveBinary) {
                        session.status = ESC;
                    } else {
                        session.flushDataIfNecessary();
                        session.status = IAC;
                    }
                } else {
                    session.appendData(b);
                }
            }
        },

        ESC() {
            @Override
            void handle(TelnetConnection session, byte b) {
                if (b == BYTE_IAC) {
                    session.appendData((byte)-1);
                } else {
                    session.flushDataIfNecessary();
                    IAC.handle(session, b);
                }
            }
        },

        IAC() {
            @Override
            void handle(TelnetConnection session, byte b) {
                if (b == BYTE_DO) {
                    session.status = DO;
                } else if (b == BYTE_DONT) {
                    session.status = DONT;
                } else if (b == BYTE_WILL) {
                    session.status = WILL;
                } else if (b == BYTE_WONT) {
                    session.status = WONT;
                } else if (b == BYTE_SB) {
                    session.paramsBuffer = new byte[100];
                    session.paramsLength = 0;
                    session.status = SB;
                } else {
//                    session.handler.onCommand(b);
                    session.status = DATA;
                }
            }
        },

        SB() {
            @Override
            void handle(TelnetConnection session, byte b) {
                if (session.paramsOptionCode == null) {
                    session.paramsOptionCode = b;
                } else {
                    if (session.paramsIac) {
                        session.paramsIac = false;
                        if (b == BYTE_SE) {
                            try {
                                session.onOptionParameters(session.paramsOptionCode, Arrays.copyOf(session.paramsBuffer, session.paramsLength));
                            } finally {
                                session.paramsOptionCode = null;
                                session.paramsBuffer = null;
                                session.status = DATA;
                            }
                        } else if (b == BYTE_IAC) {
                            session.appendToParams((byte) -1);
                        }
                    } else {
                        if (b == BYTE_IAC) {
                            session.paramsIac = true;
                        } else {
                            session.appendToParams(b);
                        }
                    }
                }
            }
        },

        DO() {
            @Override
            void handle(TelnetConnection session, byte b) {
                try {
                    session.onOptionDo(b);
                } finally {
                    session.status = DATA;
                }
            }
        },

        DONT() {
            @Override
            void handle(TelnetConnection session, byte b) {
                try {
                    session.onOptionDont(b);
                } finally {
                    session.status = DATA;
                }
            }
        },

        WILL() {
            @Override
            void handle(TelnetConnection session, byte b) {
                try {
                    session.onOptionWill(b);
                } finally {
                    session.status = DATA;
                }
            }
        },

        WONT() {
            @Override
            void handle(TelnetConnection session, byte b) {
                try {
                    session.onOptionWont(b);
                } finally {
                    session.status = DATA;
                }
            }
        },

        ;

        abstract void handle(TelnetConnection session, byte b);
    }
}
