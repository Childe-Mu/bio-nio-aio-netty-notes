package com.moon.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

/**
 * DiscardServerHandler扩展了ChannelHandlerAdapter，它是ChannelHandler的一个实现。
 * ChannelHandler提供可以覆盖的各种事件处理程序方法。目前，只需要扩展ChannelHandlerAdapter就足够了，而不必自己去实现它
 *
 * @Author: moon
 * @Date: 2020-01-09 10:19:40
 */
public class DiscardServerHandler extends ChannelHandlerAdapter {
	/**
	 * 这里我们覆盖了chanelRead()事件处理方法。每当从客户端收到新的数据时，这个方法会在收到消息时被调用，
	 * 这个例子中，收到的消息的类型是ByteBuf
	 *
	 * @param ctx 上下文
	 * @param msg 消息
	 */
	public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)
		// Discard the received data silently.、
		/*
		 * [1]为了实现DISCARD协议，处理器不得不忽略所有接受到的消息。ByteBuf是一个引用计数对象，
		 * 这个对象必须显示地调用release()方法来释放。请记住处理器的职责是释放所有传递到处理器
		 * 的引用计数对象。通常，channelRead()方法的实现就像下面的这段代码：
		 */
		/*((ByteBuf) msg).release();*/

		// [2]为了证明他仍然是在工作的，让我们修改服务端的程序来打印出他到底接收到了什么。
		ByteBuf in = (ByteBuf) msg;
		try {
			// 这个低效的循环事实上可以简化为:System.out.println(in.toString(io.netty.util.CharsetUtil.US_ASCII))
			// while (in.isReadable()) {
			// 	System.out.print((char) in.readByte());
			// 	System.out.flush();
			// }
			System.out.println(in.toString(io.netty.util.CharsetUtil.US_ASCII));
		} finally {
			// 或者可以在这里调用in.release()
			ReferenceCountUtil.release(msg);
		}

		// [3] ECHO服务（响应式协议）
		// ChannelHandlerContext对象提供了许多操作，使你能够触发各种各样的I/O事件和操作。
		// 这里我们调用了write(Object)方法来逐字地把接受到的消息写入。
		// 请注意不同于DISCARD的例子我们并没有释放接受到的消息，这是因为当写入的时候Netty已经帮我们释放了。
		// ctx.write(msg);
		// ctx.write(Object)方法不会使消息写入到通道上，他被缓冲在了内部，
		// 你需要调用ctx.flush()方法来把缓冲区中数据强行输出。
		// 或者你可以用更简洁的cxt.writeAndFlush(msg)以达到同样的目的。
		// ctx.flush();
		// ctx.writeAndFlush(msg);
	}

	/**
	 * exceptionCaught()事件处理方法是当出现Throwable对象才会被调用，
	 * 即当Netty由于IO错误或者处理器在处理事件时抛出的异常时。
	 * 在大部分情况下，捕获的异常应该被记录下来并且把关联的channel给关闭掉。
	 * 然而这个方法的处理方式会在遇到不同异常的情况下有不同的实现，
	 * 比如你可能想在关闭连接之前发送一个错误码的响应消息。
	 *
	 * @param ctx
	 * @param cause
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
		// Close the connection when an exception is raised.
		cause.printStackTrace();
		ctx.close();
	}
}
