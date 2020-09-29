package com.yihuizhi.netty_chat;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * 通道初始化器
 * 用来加载通道处理器(ChannelHandler)
 */
public class WebSocketChannelInitializer extends ChannelInitializer<SocketChannel> {
    //初始化通道
    //加载对应的ChannelHandler
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        //获取管道,将一个个ChannelHandler加入到管道中
        ChannelPipeline pipeline = socketChannel.pipeline();
        //添加一个http编码解析器
        pipeline.addLast(new HttpServerCodec());
        //添加一个支持大数量流
        pipeline.addLast(new ChunkedWriteHandler());
        //添加一个聚合器,目的将httpMessage聚合成FullHttpRequest/Response
        pipeline.addLast(new HttpObjectAggregator(1024*64));

        //需要指定接收请求的路由
        //以ws结尾
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));

        //添加自定义Handler
        pipeline.addLast(new ChatHandler());

    }
}
