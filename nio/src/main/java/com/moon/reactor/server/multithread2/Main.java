package com.moon.reactor.server.multithread2;

import java.io.IOException;

/**
 * @Description:
 * @Author: moon
 * @Date: 2019-12-13 11:02:04
 */
public class Main {


    public static void main(String[] args) {
        // TODO Auto-generated method stub
        try {
            TCPReactor reactor = new TCPReactor(1333);
            reactor.run();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
