package com.rhine.terminal.readline;

import com.rhine.terminal.api.TtyConnection;

import java.util.function.Consumer;

/**
 * @author LDZ
 * @date 2019-11-11 17:51
 */
public class ReadLine {


    public void readline(TtyConnection connection, String prompt, Consumer<String> requestHandler) {


        install(connection);
        connection.write(prompt);


    }



    public void install(TtyConnection connection) {
        connection.setReadHandler(data -> {
            synchronized (ReadLine.class) {
                for (int d : data) {
                    System.out.println((char) d);
                }
            }
        });
    }


}
