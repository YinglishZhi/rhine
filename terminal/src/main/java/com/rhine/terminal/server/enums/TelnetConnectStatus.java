package com.rhine.terminal.server.enums;

import com.rhine.terminal.server.netty.TelnetConnection;

import java.util.Arrays;

import static com.rhine.terminal.server.enums.TelnetOrderEnum.*;

/**
 * telnet 连接状态
 *
 * @author LDZ
 * @date 2020-02-21 18:31
 */
public enum TelnetConnectStatus {

    /**
     * Data
     */
    DATA() {
        @Override
        public void handle(TelnetConnection session, byte b) {
            if (b == BYTE_IAC.code) {
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

    /**
     * ESC
     */
    ESC() {
        @Override
        public void handle(TelnetConnection session, byte b) {
            if (b == BYTE_IAC.code) {
                session.appendData((byte) -1);
            } else {
                session.flushDataIfNecessary();
                IAC.handle(session, b);
            }
        }
    },

    IAC() {
        @Override
        public void handle(TelnetConnection session, byte b) {
            TelnetOrderEnum telnetOrder = getTelnetOrderByCode(b);
            assert telnetOrder != null;
            switch (telnetOrder) {
                case BYTE_DO:
                    session.status = DO;
                    break;
                case BYTE_DONT:
                    session.status = DONT;
                    break;
                case BYTE_WILL:
                    session.status = WILL;
                    break;
                case BYTE_WONT:
                    session.status = WONT;
                    break;
                case BYTE_SB:
                    session.paramsBuffer = new byte[100];
                    session.paramsLength = 0;
                    session.status = SB;
                    break;
                default:
                    session.status = DATA;
                    break;
            }
        }
    },

    SB() {
        @Override
        public void handle(TelnetConnection session, byte b) {
            if (session.paramsOptionCode == null) {
                session.paramsOptionCode = b;
            } else {
                if (session.paramsIac) {
                    session.paramsIac = false;
                    if (b == BYTE_SE.code) {
                        try {
                            session.onOptionParameters(session.paramsOptionCode, Arrays.copyOf(session.paramsBuffer, session.paramsLength));
                        } finally {
                            session.paramsOptionCode = null;
                            session.paramsBuffer = null;
                            session.status = DATA;
                        }
                    } else if (b == BYTE_IAC.code) {
                        session.appendToParams((byte) -1);
                    }
                } else {
                    if (b == BYTE_IAC.code) {
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
        public void handle(TelnetConnection session, byte b) {
            try {
                session.onOptionTypeFunction(b, this);
            } finally {
                session.status = DATA;
            }
        }
    },

    DONT() {
        @Override
        public void handle(TelnetConnection session, byte b) {
            try {
                session.onOptionTypeFunction(b, this);
            } finally {
                session.status = DATA;
            }
        }
    },

    WILL() {
        @Override
        public void handle(TelnetConnection session, byte b) {
            try {
                session.onOptionTypeFunction(b, this);
            } finally {
                session.status = DATA;
            }
        }
    },

    WONT() {
        @Override
        public void handle(TelnetConnection session, byte b) {
            try {
                session.onOptionTypeFunction(b, this);
            } finally {
                session.status = DATA;
            }
        }
    },

    ;

    public abstract void handle(TelnetConnection session, byte b);
}
