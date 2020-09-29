package com.yihuizhi.netty_chat;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    //用来保存所有的客户端连接
    private static ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 有新的事件消息会自动调用
     * @param ctx
     * @param msg
     * @throws Exception
     */
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        //当接收到数据后会自动调用
        String text = msg.text();
        System.out.println("接收到的数据为 = " + text);
        System.out.println(ctx.channel());
        for (Channel client : clients) {
            //将消息发送到所有的客户端
            client.writeAndFlush(new TextWebSocketFrame("用户"+ctx.channel().id().asShortText()+"---"+sdf.format(new Date())+":"+text));
        }
    }

    /**
     * 新的通道加入建立连接时调用
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        //将新的通道加入到clients
        clients.add(ctx.channel());
        //super.handlerAdded(ctx);
    }

    /**
     * 当有通道断开时
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
//        super.handlerRemoved(ctx);
        System.out.println("客户端断开连接,channel对应的长id是"+ctx.channel().id().asLongText());
        System.out.println("客户端断开连接,channel对应的短id是"+ctx.channel().id().asShortText());
    }
}
