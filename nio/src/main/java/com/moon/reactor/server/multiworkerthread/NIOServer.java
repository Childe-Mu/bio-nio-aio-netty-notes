package com.moon.reactor.server.multiworkerthread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @Description: 多个工作线程的reactor模式
 * @Author: moon
 * @Date: 2019-11-23 17:28:08
 */
public class NIOServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(NIOServer.class);

    public static void main(String[] args) throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(7777));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            // selectNow():选择一组键，其对应的通道已准备好进行I/O操作。
            // 此方法执行非阻塞的选择操作。如果自从前一次选择操作后，没有通道变成可选择的，则此方法直接返回零。
            // 与此相对，select()方式是阻塞的，直到有通道变成可选择
            int num = selector.select();
            if (num == 0) {
                continue;
            }
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();
            while (iterator.hasNext()) {
                SelectionKey next = iterator.next();
                if (!next.isValid()) {
                    continue;
                }
                if (next.isAcceptable()) {
                    ServerSocketChannel acceptServerSocketChannel = (ServerSocketChannel) next.channel();
                    SocketChannel socketChannel = acceptServerSocketChannel.accept();
                    socketChannel.configureBlocking(false);
                    LOGGER.info("Accept request from {}", socketChannel.getRemoteAddress());
                    SelectionKey readKey = socketChannel.register(selector, SelectionKey.OP_READ);
                    // 绑定客户端读写请求处理器
                    // readKey.attach(new Processor());
                } else if (next.isReadable()) {
                    Processor processor = new Processor();
                    // Processor processor = (Processor) next.attachment();
                    processor.process(next);
                    LOGGER.info("进入 process() ");
                }
                iterator.remove();
            }

        }
    }
}
