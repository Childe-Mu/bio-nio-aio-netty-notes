package com.moon.reactor.standard.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * @Description: 一个channel一个handler
 * @Author: moon
 * @Date: 2019-11-23 10:57:24
 */
public class WorkerHandler implements Runnable {
    private static final int READING = 0, SENDING = 1;
    private int state = READING;

    /**
     * 客户端channel
     */
    private final SocketChannel socketChannel;

    /**
     * 事件选择器
     */
    private SelectionKey selectionKey;

    /**
     * 读buffer
     */
    private ByteBuffer readBuffer = ByteBuffer.allocate(128);

    /**
     * 写buffer
     */
    private ByteBuffer sendBuffer = ByteBuffer.allocate(128);

    /**
     * WorkerHandler构造器
     *
     * @param socketChannel 客户端channel
     * @param selectionKey  注册读写事件的选择器
     */
    public WorkerHandler(SocketChannel socketChannel, SelectionKey selectionKey) {
        this.socketChannel = socketChannel;
        this.selectionKey = selectionKey;
    }

    /**
     * 处理拆包，判断是否读完
     *
     * @return true：读完， false：未读完
     */
    private boolean readIsComplete(int l) {
        //TODO
        return true;
    }

    /**
     * 处理粘包，判断是否写完
     *
     * @return true：写完， false：未写完
     */
    private boolean writeIsComplete(int l) {
        //TODO
        return true;
    }

    @Override
    public void run() {
        System.out.println(this + "运行在线程：" + Thread.currentThread());
        try {
            if (selectionKey.isReadable()) {
                read();
            } else if (selectionKey.isWritable()) {
                send();
            } else {
                selectionKey.cancel();
                socketChannel.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        //        handleInputData(socket,input, output);
    }


    /**
     * 读数据
     */
    private void read() throws IOException {
        //read是线程安全的，加锁，避免使用多线程访问同一个channel
        int readLen = socketChannel.read(readBuffer);
        System.out.println("收到客户端" + socketChannel + "数据：" + new String(readBuffer.array()));
        System.out.println();
        if (readIsComplete(readLen)) {
            readBuffer.flip();
            sendBuffer.put(readBuffer);
            state = SENDING;
            selectionKey.interestOps(SelectionKey.OP_WRITE);
        }
    }

    /**
     * 写数据
     */
    private void send() throws IOException {
        System.out.println("发送给客户端:" + socketChannel + "数据：" + new String(sendBuffer.array()));
        if (socketChannel.isConnected() && socketChannel.isOpen()) {
            int write = socketChannel.write(sendBuffer);
            // if (writeIsComplete(write)) {
            //     System.out.println("服务端写完了");
            //     selectionKey.cancel();
            // }
            sendBuffer.flip();
            //需要注册其他事件否则一直会有可写事件
            selectionKey.interestOps(SelectionKey.OP_READ);
        }
    }

    /**
     * 连接成功打印信息
     *
     * @param socketChannel 客户端连接的channel
     */
    void registered(SocketChannel socketChannel) {
        System.out.println("新连接完成" + socketChannel);
    }
}