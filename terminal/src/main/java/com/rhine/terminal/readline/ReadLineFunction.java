package com.rhine.terminal.readline;

import java.util.ArrayList;
import java.util.List;

import com.rhine.terminal.util.Helper;

/**
 * read line function
 *
 * @author LDZ
 * @date 2020-02-19 23:31
 */
public interface ReadLineFunction {

    /**
     * Load the defaults function via the {@link java.util.ServiceLoader} SPI.
     *
     * @return the loaded function
     */
    static List<ReadLineFunction> loadDefaults() {
        return new ArrayList<>(Helper.loadServices(Thread.currentThread().getContextClassLoader(), ReadLineFunction.class));
    }

    /**
     * The function name, for instance
     */
    String name();

    /**
     * Apply the function on the current interaction
     */
    void apply(ReadLine.Interaction interaction);
}
