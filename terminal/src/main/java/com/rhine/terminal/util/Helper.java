package com.rhine.terminal.util;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * @author LDZ
 * @date 2020-02-20 00:04
 */
public class Helper {


    public static <S> List<S> loadServices(ClassLoader loader, Class<S> serviceClass) {
        ArrayList<S> services = new ArrayList<>();
        for (S s : ServiceLoader.load(serviceClass, loader)) {
            try {
                services.add(s);
            } catch (Exception ignore) {
                // Log me
            }
        }
        return services;
    }
}
