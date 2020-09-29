package com.tangyuxian.netty_decodeAndEdcode;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class DiscardTwoBytes extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        super.channelRead(ctx, msg);
        ByteBuf buf = (ByteBuf) msg;
        ByteBuf newBuf = buf.readRetainedSlice(buf.readableBytes()-2); //引用计数器加1
        ctx.fireChannelRead(newBuf);
        buf.release();  //释放
    }
}
