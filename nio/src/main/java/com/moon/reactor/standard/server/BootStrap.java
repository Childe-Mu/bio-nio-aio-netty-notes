package com.moon.reactor.standard.server;

import java.io.IOException;

/**
 * @Description:
 * @Author: moon
 * @Date: 2019-11-23 10:58:51
 */
public class BootStrap {
    public static void start(int port) throws IOException {
        AbstractReactor main = new MainReactor(port);
        main.myStart();
    }
}
