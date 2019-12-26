package com.moon.reactor.server.ifeve;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * @Description:
 * @Author: moon
 * @Date: 2019-12-13 19:51:08
 */
final class Handler implements Runnable {
    private static Logger log = LoggerFactory.getLogger(Handler.class);

    private static final int MAX_IN = 1024;
    private static final int MAX_OUT = 1024;
    private final SocketChannel socket;
    private final SelectionKey sk;
    private ByteBuffer input = ByteBuffer.allocate(MAX_IN);
    private ByteBuffer output = ByteBuffer.allocate(MAX_OUT);
    private static final int READING = 0, SENDING = 1;
    private int state = READING;

    Handler(Selector selector, SocketChannel c) throws IOException {
        socket = c;
        c.configureBlocking(false);
        // Optionally try first read now
        sk = socket.register(selector, 0);
        // 注意在Handler里面又执行了一次attach，这样，覆盖前面的Acceptor，
        // 下次该Handler又有READ事件发生时，
        // 将直接触发Handler.从而开始了数据的读->处理->写->发出等流程处理。
        sk.attach(this);
        sk.interestOps(SelectionKey.OP_READ);
        selector.wakeup();
    }

    private boolean inputIsCompleted() {
        return true; //只是返回true，具体的判断没有实现
    }

    private boolean outputIsCompleted() {
        return true;//只是返回true，具体的判断没有实现
    }

    private void process() { //没有具体实现
        log.error("->来自客户端的数据：{}", new String(input.array()));
        output.put("hello world".getBytes());
    }

    // class Handler continued
    public void run() {
        try {
            if (state == READING)
                read();
            else if (state == SENDING)
                send();
        } catch (IOException ex) { /* . . . */ }
    }

    private void read() throws IOException {
        log.info("->read into byteBuffer from socketChannel inputs");
        socket.read(input);
        if (inputIsCompleted()) {
            log.info("->read complete");
            process();
            state = SENDING;
            // Normally also do first write now
            // 读完了数据之后，注册OP_WRITE事件
            sk.interestOps(SelectionKey.OP_WRITE);
        }
    }

    private void send() throws IOException {
        log.info("->write into socketChannel from byteBuffer outputs");
        socket.write(output);
        if (outputIsCompleted()) {
            /*
             * The key will be removed from all of the selector's key sets
             * during the next selection operation.
             */
            sk.cancel();
            socket.close(); //关闭通过，也就关闭了连接
            log.info("->close socketChannel after write complete");
        }
    }
}

