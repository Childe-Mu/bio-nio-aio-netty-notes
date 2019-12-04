package com.moon.reactor.server.standard;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.Executors;

/**
 * @Description: main reactor和sub reactor,io数据处理，
 * 在同一个线程轮询selector和分发客户端io连接事件给acceptor处理
 * @Author: moon
 * @Date: 2019-11-23 10:52:26
 */
class MainReactor extends AbstractReactor {

    MainReactor(int port) throws IOException {
        // 1.初始化
        serverSocketChannel = ServerSocketChannel.open();
        // 2.1.bind
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        // 2.2.配置非阻塞
        serverSocketChannel.configureBlocking(false);
        // 3.创建selector
        selector = Selector.open();
        // 4.注册事件监听(监听客户端连接事件)
        SelectionKey selectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        /*
        attach():将给定对象附加到此键。
        以后可以通过附件方法检索附加对象。一次只能附着一个物体;
        调用此方法将导致丢弃以前的任何附件。通过附加null可以丢弃当前附件
         */
        // 5.将给定对象附加到此键
        selectionKey.attach(new Acceptor(serverSocketChannel, selector));
        // 6.初始化线程池
        threadPool = Executors.newSingleThreadExecutor();
    }
}