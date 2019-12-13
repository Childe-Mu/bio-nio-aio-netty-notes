package com.moon.reactor.server.standard;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executors;

/**
 * @Description: 创建轮询读写事件的selector，将读写事件分发给workerHandler处理
 * @Author: moon
 * @Date: 2019-11-23 10:54:45
 */
final class SubReactor extends AbstractReactor {

    /**
     * SubReactor构造器
     *
     * @throws IOException 创建selector时异常
     */
    SubReactor() throws IOException {
        threadPool = Executors.newFixedThreadPool(2);
        selector = Selector.open();
    }

    /**
     * 注册轮询读写事件的selector
     *
     * @param clientChannel 接收到的客户端链接channel
     * @throws IOException 配置和注册时异常
     */
    void registerInSubReactor(SocketChannel clientChannel) throws IOException {
        // 配置为非阻塞模式
        clientChannel.configureBlocking(false);
        // 注册读事件
        SelectionKey selectionKey = clientChannel.register(selector, SelectionKey.OP_READ);
        //        selectionKey.interestOps();
        // 让尚未返回的第一个选择操作立即返回。
        selector.wakeup();
        // 创建读写workerHandler
        WorkerHandler workerHandler = new WorkerHandler(clientChannel, selectionKey);
        // 给selectionKey固定一个线程和handler去执行读写，netty eventGroup的目的？
        selectionKey.attach(workerHandler);

        // workerHandler.registered(clientChannel);
        System.out.println("新连接完成" + clientChannel);
    }

    /**
     * 提交handler任务
     */
    void execute(Runnable runnable) {
        threadPool.execute(runnable);
    }
}