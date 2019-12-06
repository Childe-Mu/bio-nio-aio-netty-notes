package com.moon.reactor.server.multiworkerthread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Description: 客户端读写请求处理器
 * @Author: moon
 * @Date: 2019-11-23 17:30:18
 */
class Processor {
    private static final Logger LOGGER = LoggerFactory.getLogger(Processor.class);
    private ExecutorService service = Executors.newFixedThreadPool(1);

    void process(SelectionKey selectionKey) {
        // 下面说的问题，就是多线程的原因，会产生并发问题导致进入catch到IOException，不停打印 客户端异常中断
        service.submit(() -> {
            try {
                SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                ByteBuffer readBuffer = ByteBuffer.allocate(64);
                int count = socketChannel.read(readBuffer);
                // 客户端主动中断
                if (count < 0) {
                    socketChannel.close();
                    selectionKey.cancel();
                    LOGGER.info(Thread.currentThread().getName() + "{}\t Read ended", socketChannel);
                } else {
                    LOGGER.info(Thread.currentThread().getName() + "{}\t Read message {}", socketChannel, new String(readBuffer.array()));
                }
            } catch (IOException e) {
                // 客户端被动中断
                selectionKey.cancel();
                // 会多次进入，是多线程的问题
                LOGGER.info(Thread.currentThread().getName() + " 客户端异常中断");
            }
        });
    }
}
