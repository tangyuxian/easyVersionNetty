package com.tangyuxian.netty_test;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.Charset;

public class DiscardServerHandler extends ChannelDuplexHandler {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)
        // Discard the received data silently.
        //((ByteBuf) msg).release(); // (3)
        ByteBuf buf = (ByteBuf) msg;
        try {
/*            int count = buf.readableBytes();
            byte[] content = new byte[count];
            buf.readBytes(content);
            System.out.println(new String(content));
            String str = new String(content);*/
            String str = buf.toString(Charset.forName("utf-8"));
            ctx.fireChannelRead(str);
        }finally {
            buf.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}
