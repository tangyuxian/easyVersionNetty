package com.yihuizhi.netty_test;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SecondHandler extends ChannelDuplexHandler {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.debug(msg.toString());

        ctx.channel().write("client hello");
//        ctx.channel().writeAndFlush("client hello");
        if(msg.equals("flush")){
            ctx.channel().flush();
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
//        super.write(ctx, msg, promise);
        ByteBuf buffer = ctx.alloc().buffer(32);
        String str = (String) msg;
        buffer.writeBytes(str.getBytes("utf-8"));
        ctx.write(buffer);
    }
}
