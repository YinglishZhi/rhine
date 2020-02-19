package com.rhine.terminal.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * @author LDZ
 * @date 2020-02-20 00:04
 */
public class Helper {


    public static <S> List<S> loadServices(ClassLoader loader, Class<S> serviceClass) {
        ArrayList<S> services = new ArrayList<>();
        Iterator<S> i = ServiceLoader.load(serviceClass, loader).iterator();
        while (i.hasNext()) {
            try {
                S service = i.next();
                services.add(service);
            } catch (Exception ignore) {
                // Log me
            }
        }
        return services;
    }
}
