package com.moon.reactor.server.standard;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @Description: 处理客户端socket连接事件
 * @Author: moon
 * @Date: 2019-11-23 10:56:36
 */
class Acceptor {

    /**
     * 服务端channel
     */
    private ServerSocketChannel serverSocketChannel;

    /**
     * 选择器
     */
    private Selector selector;

    /**
     * 读写事件处理
     */
    private SubReactor subReactor;

    /**
     * Acceptor构造器
     *
     * @param serverSocketChannel 服务端channel
     * @param selector            客户端连接事件监听器
     * @throws IOException 异常
     */
    public Acceptor(ServerSocketChannel serverSocketChannel, Selector selector) throws IOException {
        this.serverSocketChannel = serverSocketChannel;
        this.selector = selector;
        this.subReactor = new SubReactor();
        subReactor.myStart();
    }

    /**
     * 与客户端的连接建立
     */
    public void myAccept() {
        try {
            // 客户端连接建立
            SocketChannel client = serverSocketChannel.accept();
            if (client != null) {
                System.out.println("收到连接请求");
                // 通过acceptor将mainreactor、subreactor连接起来
                // netty handler对应
                subReactor.registerInSubReactor(client);
            }
        } catch (IOException ex) {
            System.out.println(ex);
            ex.printStackTrace();
        }
    }
}