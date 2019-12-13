package com.moon.reactor.server.simple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @Description: 经典的Reactor模式
 * 在Reactor模式中，包含如下角色：
 * - Reactor 将I/O事件发派给对应的Handler
 * - Acceptor 处理客户端连接请求
 * - Handlers 执行非阻塞读/写
 * 为了方便阅读，代码将Reactor模式中的所有角色放在了一个类中
 * @Author: moon
 * @Date: 2019-11-23 16:07:30
 */
public class NIOServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(NIOServer.class);

    public static void main(String[] args) throws IOException {
        // 1.初始化
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 2.1.bind
        serverSocketChannel.bind(new InetSocketAddress("localhost", 7777));
        // 2.2.配置非阻塞
        serverSocketChannel.configureBlocking(false);
        // 3.创建selector
        Selector selector = Selector.open();
        // 4.注册事件监听(监听客户端连接事件)
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        // 5.阻塞select，等待io事件就绪
        while (selector.select() > 0) {
            // 6.io已就绪的channel稽核，遍历
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                // 6.1 连接就绪事件
                if (key.isAcceptable()) {
                    ServerSocketChannel acceptServerSocketChannel = (ServerSocketChannel) key.channel();
                    SocketChannel socketChannel = acceptServerSocketChannel.accept();
                    socketChannel.configureBlocking(false);
                    LOGGER.info("Accept request from {}", socketChannel.getRemoteAddress());
                    socketChannel.register(selector, SelectionKey.OP_READ);
                }
                // 6.2 读事件就绪
                else if (key.isReadable()) {
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int count = socketChannel.read(buffer);
                    if (count <= 0) {
                        socketChannel.close();
                        key.cancel();
                        LOGGER.info("Received invalid data, close the connection");
                        continue;
                    }
                    LOGGER.info("Received message {}", new String(buffer.array()));
                }
                keys.remove(key);
            }
        }
    }
}