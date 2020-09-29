package com.yihuizhi.netty_chat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.sctp.nio.NioSctpServerChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class WebSocketServer {
    public static void main(String[] args) {
//        创建两个线程池
        NioEventLoopGroup mainGrp = new NioEventLoopGroup(); //主线程池
        NioEventLoopGroup subGrp = new NioEventLoopGroup(); //从线程池

        try {
            //创建netty的服务器启动对象
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            //初始化服务器启动对象
            serverBootstrap
                    //指定上面创建的线程池
                    .group(mainGrp,subGrp)
                    //指定通道类型
                    .channel(NioServerSocketChannel.class)
                    //通道初始化器,加载channel收到消息后的业务处理
                    .childHandler(new WebSocketChannelInitializer());
            //绑定服务器端口,以同步的方式启动服务器
            ChannelFuture future = serverBootstrap.bind(9090).sync();
            //等待服务器关闭
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //关闭线程池
            mainGrp.shutdownGracefully();
            subGrp.shutdownGracefully();
        }
    }

}
