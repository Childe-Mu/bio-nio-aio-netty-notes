package com.moon.reactor.server.multithread2;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Description:
 * @Author: moon
 * @Date: 2019-12-13 11:00:02
 */
public interface HandlerState {

    public void changeState(TCPHandler h);

    public void handle(TCPHandler h, SelectionKey sk, SocketChannel sc,
                       ThreadPoolExecutor pool) throws IOException;
}

