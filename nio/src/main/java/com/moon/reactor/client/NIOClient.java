package com.moon.reactor.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @Description: 公共客户端
 * @Author: moon
 * @Date: 2019-11-22 16:31:27
 */
public class NIOClient {
    public static void main(String[] args) {
        try {
           for (int i = 0; i < 1; i++) {
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
            ByteBuffer sendBuffer = ByteBuffer.allocate(64);

            sendBuffer.put("hello world from client".getBytes());
            // 因为sendBuffer前面调用了put(),所以需要调用flip()，将position置为0，否则后面write拿不到数据
            sendBuffer.flip();
            socketChannel.write(sendBuffer);
            System.out.println(socketChannel + "发送到服务端:" + new String(sendBuffer.array()));

            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
