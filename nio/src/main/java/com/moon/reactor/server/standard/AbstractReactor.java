package com.moon.reactor.server.standard;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public abstract class AbstractReactor implements Runnable {

    /**
     * 选择器
     */
    Selector selector;

    /**
     * ServerSocketChannel
     */
    ServerSocketChannel serverSocketChannel;

    /**
     * 线程池
     */
    ExecutorService threadPool;

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                // select和register竞争锁会阻塞，需要有个超时时间
                selector.select(100);
                Set<SelectionKey> selected = selector.selectedKeys();
                iterator(selected);
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
    }

    private void iterator(Set<SelectionKey> selected) {
        Iterator it = selected.iterator();
        while (it.hasNext()) {
            SelectionKey next = (SelectionKey) it.next();
            //线程不安全的
            it.remove();
            dispatch(next);
        }
    }

    private void dispatch(SelectionKey selectionKey) {
        try {
            if (selectionKey.isAcceptable()) {
                Acceptor acceptor = (Acceptor) (selectionKey.attachment());
                if (acceptor != null) {
                    acceptor.myAccept();
                }
            }
            if (selectionKey.isValid() && (selectionKey.isReadable() || selectionKey.isWritable())) {
                Runnable workerHandler = (Runnable) (selectionKey.attachment());
                execute(workerHandler);
                // 线程不安全的，remove有问题
                // selected.remove(selectionKey);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void myStart() {
        threadPool.execute(this);
    }

    void execute(Runnable runnable) {
    }
}