package com.moon.nio;

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
 * @Description: NIOServer
 * @Author: moon
 * @Date: 2019-11-22 14:10:47
 */
public class NIOServer {
    public static void main(String[] args) {
        try {
            // 1.初始化
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            // 2.1.bind
            serverSocketChannel.bind(new InetSocketAddress("localhost", 7777));
            // 2.2.配置非阻塞
            serverSocketChannel.configureBlocking(false);
            // 3.创建selector
            Selector selector = Selector.open();
          /*
            SelectionKey.OP_ACCEPT —— 接收连接继续事件，表示服务器监听到了客户连接，服务器可以接收这个连接了
            SelectionKey.OP_CONNECT —— 连接就绪事件，表示客户与服务器的连接已经建立成功
            SelectionKey.OP_READ —— 读就绪事件，表示通道中已经有了可读的数据，可以执行读操作了（通道目前有数据，可以进行读操作了）
            SelectionKey.OP_WRITE —— 写就绪事件，表示已经可以向通道写数据了（通道目前可以用于写操作）
            这里 注意，下面两种，SelectionKey.OP_READ ，SelectionKey.OP_WRITE ，
            1.当向通道中注册SelectionKey.OP_READ事件后，如果客户端有向缓存中write数据，下次轮询时，则会 isReadable()=true；
            2.当向通道中注册SelectionKey.OP_WRITE事件后，这时你会发现当前轮询线程中isWritable()一直为ture，如果不设置为其他事件
            */
            // 4.注册事件监听(监听客户端连接事件)
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            int selectNum = 0;
            while (true) {
                // 5.阻塞select，等待io事件就绪
                selectNum = selector.select();
                if (selectNum == 0) {
                    continue;
                }
                // 6.io已就绪的channel稽核，遍历
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> selectionKeyIterator = selectionKeys.iterator();
                while (selectionKeyIterator.hasNext()) {
                    SelectionKey next = selectionKeyIterator.next();
                    // [!]手动remove,否则会导致select一直返回0
                    selectionKeyIterator.remove();
                    // 6.1 连接事件
                    if (next.isAcceptable()) {
                        // 6.1.1 客户端连接，channel
                        SocketChannel clientChannel = serverSocketChannel.accept();
                        if (clientChannel != null) {
                            clientChannel.configureBlocking(false);
                            System.out.println("新连接：" + clientChannel);
                            // 6.1.2 注册read、write事件
                            clientChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                        }
                    }
                    // 6.2 读事件就绪
                    else if (next.isReadable()) {
                        // 6.2.1 创建readBuffer
                        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                        SocketChannel channel = (SocketChannel) next.channel();
                        // 6.2.2 读取readBuffer
                        channel.read(readBuffer);
                        // 6.2.3 Buffer切换读写模式
                        readBuffer.flip();
                        System.out.println(next.channel() + "客户端发来数据:" + new String(readBuffer.array()));
                        // 6.2.4 一次读操作结束以后将关注点切换到写操作
                        next.interestOps(SelectionKey.OP_WRITE);
                    }
                    // 6.2 写事件就绪
                    else if (next.isWritable()) {
                        ByteBuffer sendBuffer = ByteBuffer.allocate(1024);
                        sendBuffer.put("hello world from server".getBytes());
                        SocketChannel channel = (SocketChannel) next.channel();
                        System.out.println("服务端发送返回：---》" + channel);
                        channel.write(sendBuffer);
                        sendBuffer.flip();
                        next.interestOps(SelectionKey.OP_READ);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
