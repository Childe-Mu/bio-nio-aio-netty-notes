package com.moon;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

/**
 * @Description: AIOClient
 * @Author: moon
 * @Date: 2019-11-22 11:21:28
 */
public class AIOClient {
    private AsynchronousSocketChannel clientChannel;

    public AIOClient(String host, int port) {
        init(host,port);
    }

    private void init(String host, int port) {
        try {
            clientChannel = AsynchronousSocketChannel.open();
            clientChannel.connect(new InetSocketAddress(host,port));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void doWrite(String line) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put(line.getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        clientChannel.write(buffer);
    }

    public void doRead() {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        try {
            // read() 是一个异步方法，实际由OS实现，
            // get()会阻塞,此处使用阻塞是因为后面要把结果打印
            // 也可以去掉get，但是就必须实现 CompletionHandler
            // 就像server端读取数据那样
            clientChannel.read(buffer).get();
            buffer.flip();
            System.out.println("from server: "+new String(buffer.array(),StandardCharsets.UTF_8));
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void doDestroy() {
        if (null != clientChannel) {
            try {
                clientChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        AIOClient client = new AIOClient("localhost", 7777);
        try {
            System.out.println("enter your message to server : ");
            Scanner s = new Scanner(System.in);
            String line = s.nextLine();
            client.doWrite(line);
            client.doRead();
        } finally {
            client.doDestroy();
        }
    }
}