package com.rhine.heck;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author LDZ
 * @date 2019-11-01 16:55
 */
class AgentClassLoader extends URLClassLoader {
    AgentClassLoader(URL[] urls) {
        super(urls, ClassLoader.getSystemClassLoader().getParent());
    }
}
