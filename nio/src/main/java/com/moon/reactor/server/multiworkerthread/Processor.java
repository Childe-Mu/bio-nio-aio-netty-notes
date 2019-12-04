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
    private static final ExecutorService service = Executors.newFixedThreadPool(1);

    void process(SelectionKey selectionKey) {
        // 下面说的问题，就是多选称的原因，多线程不是这么用的
        // service.submit(() -> {
        service.execute(() -> {
            try {
                ByteBuffer readBuffer = ByteBuffer.allocate(64);
                SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                int count;
                count = socketChannel.read(readBuffer);

                // 客户端主动中断
                if (count < 0) {
                    socketChannel.close();
                    selectionKey.cancel();
                    LOGGER.info("{}\t Read ended", socketChannel);
                    // return null;
                }
                // 客户端发送数据为空
                else if (count == 0) {
                    // return null;
                }
                LOGGER.info("{}\t Read message {}", socketChannel, new String(readBuffer.array()));
                // return null;
            } catch (IOException e) {
                // 客户端被动中断
                // socketChannel.close();
                selectionKey.cancel();
                // 不懂这一块为什么会多次进入？？？？？？？？？？？？,感觉是多线程的问题
                LOGGER.info(Thread.currentThread().getName() + " 客户端异常中断");
                // return null;
            }
        });
    }
}
