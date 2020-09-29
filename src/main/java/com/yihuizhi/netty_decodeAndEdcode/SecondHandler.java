package com.yihuizhi.netty_decodeAndEdcode;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SecondHandler extends ChannelDuplexHandler {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.debug(msg.toString());
        ByteBuf buf = (ByteBuf) msg;
        log.debug(ByteBufUtil.hexDump(buf));

        buf.release();
      //  ctx.channel().write("client hello");
          ctx.channel().writeAndFlush("client hello");
//        if(msg.equals("flush")){
//            ctx.channel().flush();
//        }
    }

    /**
     * TCP建立连接以后的回调
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

    }

    /**
     * 连接断开之后的回调
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }
}
