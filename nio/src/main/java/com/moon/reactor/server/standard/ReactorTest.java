package com.moon.reactor.server.standard;

import java.io.IOException;

/**
 * @Description:
 * @Author: moon
 * @Date: 2019-11-23 10:59:14
 */
public class ReactorTest {
    public static void main(String[] args) throws IOException {
        int port = 7777;
        AbstractReactor main = new MainReactor(port);
        main.myStart();
        // 只是为了阻塞线程，不让main线程结束
        System.in.read();
    }
}
