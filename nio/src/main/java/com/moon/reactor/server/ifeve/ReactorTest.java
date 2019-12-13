package com.moon.reactor.server.ifeve;

import java.io.IOException;

/**
 * @Description:
 * @Author: moon
 * @Date: 2019-12-13 19:52:27
 */
public class ReactorTest {

    public static void main(String args[]) throws IOException {
        Reactor reactor = new Reactor(7777);
        reactor.run();
    }
}
