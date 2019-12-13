package com.moon.reactor.server.ifeve;

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
 * @Description:
 * @Author: moon
 * @Date: 2019-12-13 19:50:19
 */
public class Reactor implements Runnable {

    private static Logger log = LoggerFactory.getLogger(Reactor.class);

    private final Selector selector;
    private final ServerSocketChannel serverSocket;

    Reactor(int port) throws IOException {
        selector = Selector.open();
        serverSocket = ServerSocketChannel.open();
        serverSocket.socket().bind(new InetSocketAddress(port));
        serverSocket.configureBlocking(false);
        SelectionKey sk = serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        //利用sk的attache功能绑定Acceptor 如果有事情，触发Acceptor
        sk.attach(new Acceptor());
        log.info("->attach(new Acceptor())");
    }


    // Alternatively,use explicit SPI provider :
    // SelectorProvider p = SelectorProvider.provider();
    // selector=p.openSelector();
    // serverSocket=p.openServerSocketChannel();

    // class Reactor continued
    public void run() { // normally in a new Thread
        try {
            while (!Thread.interrupted()) {
                selector.select();
                Set selected = selector.selectedKeys();
                Iterator it = selected.iterator();
                //Selector如果发现channel有OP_ACCEPT或READ事件发生，下列遍历就会进行。
                while (it.hasNext()) {
                    //来一个事件 第一次触发一个accepter线程
                    //以后触发Handler
                    SelectionKey sk = (SelectionKey) it.next();
                    log.info(">>>>>>acceptable=" + sk.isAcceptable() + ",readable=" + sk.isReadable() + ",writable=" + sk.isWritable());
                    dispatch(sk);
                }
                selected.clear();
            }
        } catch (IOException ex) {
            log.info("reactor stop!" + ex);
        }
    }

    void dispatch(SelectionKey k) {
        Runnable r = (Runnable) (k.attachment());
        if (r != null) {
            r.run();
        }
    }

    // class Reactor continued
    class Acceptor implements Runnable { // inner
        public void run() {
            try {
                log.debug("-->ready for accept!");
                SocketChannel c = serverSocket.accept();
                if (c != null) {
                    new Handler(selector, c);
                }
            } catch (IOException ex) { /* . . . */ }
        }
    }
}
