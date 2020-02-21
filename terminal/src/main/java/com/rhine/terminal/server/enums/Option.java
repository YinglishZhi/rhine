package com.rhine.terminal.server.enums;

import com.rhine.terminal.server.netty.TelnetConnection;

import static com.rhine.terminal.server.enums.TelnetOrderEnum.*;

/**
 * A telnet option.
 *
 * @author LDZ
 * @date 2020-02-21 18:49
 */
public enum Option {

    /**
     * Telnet Binary Transmission (<a href="https://tools.ietf.org/html/rfc856">RFC856</a>).
     */
    BINARY((byte) 0) {
        @Override
        public void handleDo(TelnetConnection session) {
            session.sendBinary = true;
//            session.handler.onSendBinary(true);
        }

        @Override
        public void handleDont(TelnetConnection session) {
            session.sendBinary = false;
//            session.handler.onSendBinary(false);
        }

        @Override
        public void handleWill(TelnetConnection session) {
            session.receiveBinary = true;
//            session.handler.onReceiveBinary(true);
        }

        @Override
        public void handleWont(TelnetConnection session) {
            session.receiveBinary = false;
//            session.handler.onReceiveBinary(false);
        }
    },

    /**
     * Telnet Echo Option (<a href="https://tools.ietf.org/html/rfc857">RFC857</a>).
     */
    ECHO((byte) 1) {
        @Override
        public void handleDo(TelnetConnection session) {
//            session.handler.onEcho(true);
        }

        @Override
        public void handleDont(TelnetConnection session) {
//            session.handler.onEcho(false);
        }
    },

    /**
     * Telnet Suppress Go Ahead Option (<a href="https://tools.ietf.org/html/rfc858">RFC858</a>).
     */
    SGA((byte) 3) {
        @Override
        public void handleDo(TelnetConnection session) {
//            session.handler.onSGA(true);
        }

        @Override
        public void handleDont(TelnetConnection session) {
//            session.handler.onSGA(false);
        }
    },

    /**
     * Telnet Terminal Type Option (<a href="https://tools.ietf.org/html/rfc884">RFC884</a>).
     */
    TERMINAL_TYPE((byte) 24) {

        static final byte BYTE_IS = 0;
        static final byte BYTE_SEND = 1;

        @Override
        public void handleWill(TelnetConnection session) {
            session.send(new byte[]{BYTE_IAC.code, BYTE_SB.code, code, BYTE_SEND, BYTE_IAC.code, BYTE_SE.code});
        }

        @Override
        public void handleParameters(TelnetConnection session, byte[] parameters) {
//            if (parameters.length > 0 && parameters[0] == BYTE_IS) {
//                String terminalType = new String(parameters, 1, parameters.length - 1);
//                session.handler.onTerminalType(terminalType);
//            }
        }
    },

    /**
     * Telnet Window Size Option (<a href="https://www.ietf.org/rfc/rfc1073.txt">RFC1073</a>).
     */
    NAWS((byte) 31) {
        @Override
        public void handleWill(TelnetConnection session) {
//            session.handler.onNAWS(true);
        }

        @Override
        public void handleWont(TelnetConnection session) {
//            session.handler.onNAWS(false);
        }

        @Override
        public void handleParameters(TelnetConnection session, byte[] parameters) {
            if (parameters.length == 4) {
                int width = ((parameters[0] & 0xff) << 8) + (parameters[1] & 0xff);
                int height = ((parameters[2] & 0xff) << 8) + (parameters[3] & 0xff);
                session.handler.onSize(width, height);
            }
        }
    };

    /**
     * The option code.
     */
    public final byte code;

    Option(byte code) {
        this.code = code;
    }

    /**
     * Handle a <code>DO</code> message.
     *
     * @param session the session
     */
    public void handleDo(TelnetConnection session) {
    }

    /**
     * Handle a <code>DON'T</code> message.
     *
     * @param session the session
     */
    public void handleDont(TelnetConnection session) {
    }

    /**
     * Handle a <code>WILL</code> message.
     *
     * @param session the session
     */
    public void handleWill(TelnetConnection session) {
    }

    /**
     * Handle a <code>WON'T</code> message.
     *
     * @param session the session
     */
    public void handleWont(TelnetConnection session) {
    }

    /**
     * Handle a parameters message.
     *
     * @param session    the session
     * @param parameters the parameters
     */
    public void handleParameters(TelnetConnection session, byte[] parameters) {
    }

    /**
     * 根据 option code 获取 Option
     *
     * @param optionCode option code
     * @return option
     */
    public static Option getOptionByCode(byte optionCode) {
        for (Option option : Option.values()) {
            if (option.code == optionCode) {
                return option;
            }
        }
        return null;
    }

}