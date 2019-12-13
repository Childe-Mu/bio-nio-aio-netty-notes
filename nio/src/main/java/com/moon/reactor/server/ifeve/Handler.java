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
    private static Logger log = LoggerFactory.getLogger(Reactor.class);

    private static final int MAXIN = 1024;
    private static final int MAXOUT = 1024;
    private final SocketChannel socket;
    private final SelectionKey sk;
    private ByteBuffer input = ByteBuffer.allocate(MAXIN);
    private ByteBuffer output = ByteBuffer.allocate(MAXOUT);
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

    private boolean inputIsComplete() {
        return true; //只是返回true，具体的判断没有实现
    }

    private boolean outputIsComplete() {
        return true;//只是返回true，具体的判断没有实现
    }

    private void process() { //没有具体实现
        output.put("helloworld".getBytes());
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
        log.info("->read into bytebuffer from socketchannel inputs");
        socket.read(input);
        if (inputIsComplete()) {
            log.info("->read complete");
            process();
            state = SENDING;
            // Normally also do first write now
            // 读完了数据之后，注册OP_WRITE事件
            sk.interestOps(SelectionKey.OP_WRITE);
        }
    }

    private void send() throws IOException {
        log.info("->write into socketchannel from bytebuffer outputs");
        socket.write(output);
        if (outputIsComplete()) {
            /*
             * The key will be removed fromall of the selector's key sets
             * during the next selection operation.
             */
            sk.cancel();
            socket.close(); //关闭通过，也就关闭了连接
            log.info("->close socketchannel after write complete");
        }
    }
}

