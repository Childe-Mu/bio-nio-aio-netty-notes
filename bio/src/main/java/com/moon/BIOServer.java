package com.moon;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @Description: BIOServer
 * @Author: moon
 * @Date: 2019-11-22 11:03:11
 */
public class BIOServer {
    public BIOServer() throws IOException {
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress("localhost", 6666));
        while (true) {
            // 会阻塞在这里，等待客户端连进来
            Socket client = serverSocket.accept();
            if (null != client) {
                // 使用线程池处理读写操作
                byte[] readBuf = new byte[128];
                byte[] sendBuf = new byte[128];
                InputStream inputStream = client.getInputStream();
                int len = inputStream.read(readBuf);
                if (len > 0) {
                    System.out.println(new String(readBuf));
                } else {
                    System.out.println("client says nothing");
                }
                OutputStream outputStream = client.getOutputStream();
                outputStream.write("hello client".getBytes());
            }
        }
    }
}
