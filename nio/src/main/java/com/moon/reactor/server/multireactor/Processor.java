package com.moon.reactor.server.multireactor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Description:
 * @Author: moon
 * @Date: 2019-11-23 17:39:46
 */
class Processor {
    private static final Logger LOGGER = LoggerFactory.getLogger(Processor.class);
    private static final ExecutorService service =
            Executors.newFixedThreadPool(2 * Runtime.getRuntime().availableProcessors());

    private Selector selector;

    Processor() throws IOException {
        this.selector = SelectorProvider.provider().openSelector();
        start();
    }

    void addChannel(SocketChannel socketChannel) throws ClosedChannelException {
        socketChannel.register(this.selector, SelectionKey.OP_READ);
    }

    private void start() {
        service.submit(() -> {
            while (true) {
                if (selector.selectNow() <= 0) {
                    continue;
                }
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (key.isReadable()) {
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        int count = socketChannel.read(buffer);
                        if (count < 0) {
                            socketChannel.close();
                            key.cancel();
                            LOGGER.info("{}\t Read ended", socketChannel);
                        } else if (count == 0) {
                            LOGGER.info("{}\t Message size is 0", socketChannel);
                        } else {
                            LOGGER.info("{}\t Read message {}", socketChannel, new String(buffer.array()));
                        }
                    }
                }
            }
        });
    }
}
