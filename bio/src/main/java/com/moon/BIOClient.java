package com.moon;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @Description: BIOClient
 * @Author: moon
 * @Date: 2019-11-22 11:21:28
 */
public class BIOClient {
    public static void main(String[] args) throws IOException {
        Socket client = new Socket();
        client.connect(new InetSocketAddress("localhost", 6666));
        OutputStream outputStream = client.getOutputStream();
        outputStream.write("hello server".getBytes());
        InputStream inputStream = client.getInputStream();
        byte[] readBuf = new byte[128];
        int len = inputStream.read(readBuf);
        if (len > 0) {
            System.out.println(new String(readBuf));
        } else {
            System.out.println("server says nothing");
        }
    }
}
