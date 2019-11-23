package com.moon.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @Description: NIOClient
 * @Author: moon
 * @Date: 2019-11-22 16:31:27
 */
public class NIOClient {
    public static void main(String[] args) {
        try {
            for (int i = 0; i < 10; i++) {
                new Thread(NIOClient::sendAndReceiveClient).start();
            }
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void sendAndReceiveClient() {
        try {
            SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("localhost", 7777));
            ByteBuffer sendBuffer = ByteBuffer.allocate(1024).put("hello world".getBytes());
            System.out.println(socketChannel + "发送到服务端:"+new String(sendBuffer.array()));
            socketChannel.write(sendBuffer);
            sendBuffer.flip();
            ByteBuffer readBuffer = ByteBuffer.allocate(1024);
            socketChannel.read(readBuffer);
            System.out.println(socketChannel + "接受到服务端:" + new String(readBuffer.array()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
