package com.moon.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @Description: NIOServer
 * @Author: moon
 * @Date: 2019-11-22 14:10:47
 */
public class NIOServer {
    public static void main(String[] args) {
        try {
            // 1.初始化
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            // 2.1.bind
            serverSocketChannel.bind(new InetSocketAddress("localhost", 7777));
            // 2.2.配置非阻塞
            serverSocketChannel.configureBlocking(false);
            // 3.创建selector
            Selector selector = Selector.open();
          /*
            SelectionKey.OP_ACCEPT —— 接收连接继续事件，表示服务器监听到了客户连接，服务器可以接收这个连接了
            SelectionKey.OP_CONNECT —— 连接就绪事件，表示客户与服务器的连接已经建立成功
            SelectionKey.OP_READ —— 读就绪事件，表示通道中已经有了可读的数据，可以执行读操作了（通道目前有数据，可以进行读操作了）
            SelectionKey.OP_WRITE —— 写就绪事件，表示已经可以向通道写数据了（通道目前可以用于写操作）
            这里 注意，下面两种，SelectionKey.OP_READ ，SelectionKey.OP_WRITE ，
            1.当向通道中注册SelectionKey.OP_READ事件后，如果客户端有向缓存中write数据，下次轮询时，则会 isReadable()=true；
            2.当向通道中注册SelectionKey.OP_WRITE事件后，这时你会发现当前轮询线程中isWritable()一直为ture，如果不设置为其他事件
            */
            // 4.注册事件监听(监听客户端连接事件)
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            int selectNum = 0;
            while (true) {
                // 5.阻塞select，等待io事件就绪
                selectNum = selector.select();
                // 这里的判断其实没有用，select()方法只要返回，selectNum就一定大于0，只用调用selectNow()方法时才有用
                if (selectNum == 0) {
                    continue;
                }
                // 6.io已就绪的channel稽核，遍历
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> selectionKeyIterator = selectionKeys.iterator();
                while (selectionKeyIterator.hasNext()) {
                    SelectionKey next = selectionKeyIterator.next();
                    // 6.1 连接事件
                    if (next.isAcceptable()) {
                        // 6.1.1 客户端连接，channel
                        ServerSocketChannel channel = (ServerSocketChannel) next.channel();
                        SocketChannel clientChannel = channel.accept();
                        if (clientChannel != null) {
                            clientChannel.configureBlocking(false);
                            System.out.println("新连接：" + clientChannel);
                            // 6.1.2 注册read、write事件
                            clientChannel.register(selector, SelectionKey.OP_READ);
                        }
                    }
                    // 6.2 读事件就绪
                    else if (next.isReadable()) {
                        try {
                            // 6.2.1 创建readBuffer,因为readBuffer只是从channel读一次数据，所以不需要调用clear()，也不需要再往channel里写数据，所以不需要调用flip()
                            ByteBuffer readBuffer = ByteBuffer.allocate(64);
                            SocketChannel channel = (SocketChannel) next.channel();
                            // 6.2.2 读取readBuffer
                            int len = channel.read(readBuffer);
                            // read()返回值为 -1时，说明客户端的连接已经 主动 关闭了
                            if (len == -1) {
                                // 关闭channel（key将失效）
                                channel.close();
                                continue;
                            }
                            System.out.println(next.channel() + "客户端发来数据:" + new String(readBuffer.array()));
                            // 6.2.3 一次读操作结束以后将关注点切换到写操作
                            next.interestOps(SelectionKey.OP_WRITE);
                        } catch (IOException e) {
                            /*
                            当客户端 被动 切断连接的时候，比如，线程直接结束等，服务端 Socket 的读事件（FD_READ）仍然起作用，
                            也就是说，服务端 Socket 的状态仍然是有东西可读，但是读取时（步骤6.2.2）就会抛出IOException异常。
                            这时需要cancel对应的key。
                             */
                            //取消selectionKey
                            next.cancel();
                        }
                    }
                    // 6.3 写事件就绪
                    else if (next.isValid() && next.isWritable()) {
                        // 6.3.1 创建readBuffer
                        ByteBuffer sendBuffer = ByteBuffer.allocate(64);
                        sendBuffer.put("hello world from server".getBytes());
                        SocketChannel channel = (SocketChannel) next.channel();
                        // 6.3.2 因为sendBuffer前面调用了put(),所以需要调用flip()，将position置为0，否则后面write拿不到数据
                        sendBuffer.flip();
                        System.out.println("服务端发送返回：----> " + new String(sendBuffer.array()));
                        channel.write(sendBuffer);
                        next.interestOps(SelectionKey.OP_READ);
                    }
                    /*
                     * 6.3
                     * 每次迭代末尾的remove()调用，Selector不会自己从已选择的SelectionKey集合中
                     * 移除SelectionKey实例的，必须在处理完通道时自己移除
                     */
                    selectionKeyIterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
