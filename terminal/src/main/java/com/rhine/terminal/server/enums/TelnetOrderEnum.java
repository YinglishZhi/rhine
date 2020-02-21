package com.rhine.terminal.server.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * telnet order enum
 *
 * @author LDZ
 * @date 2020-02-21 17:18
 */
@Getter
@AllArgsConstructor
public enum TelnetOrderEnum {
    /**
     * IAC
     */
    BYTE_IAC((byte) 0xFF),

    /**
     * DONT
     */
    BYTE_DONT((byte) 0xFE),

    /**
     * DO
     */
    BYTE_DO((byte) 0xFD),

    /**
     * WONT
     */
    BYTE_WONT((byte) 0xFC),

    /**
     * WILL
     */
    BYTE_WILL((byte) 0xFB),

    /**
     * SB
     */
    BYTE_SB((byte) 0xFA),

    /**
     * SE
     */
    BYTE_SE((byte) 0xF0);

    public byte code;


    public static TelnetOrderEnum getTelnetOrderByCode(byte b) {
        for (TelnetOrderEnum telnetOrderEnum : values()) {
            if (telnetOrderEnum.code == b) {
                return telnetOrderEnum;
            }
        }
        return null;
    }

}
