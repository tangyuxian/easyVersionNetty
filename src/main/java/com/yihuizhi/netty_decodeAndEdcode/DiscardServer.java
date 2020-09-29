package com.yihuizhi.netty_decodeAndEdcode;

import com.yihuizhi.netty_test.DiscardServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

public class DiscardServer {
    private int port;

    public DiscardServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        //bossGroup 专门处理连接,一般只用一个就够了(ServerSocketChannel)
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        //workerGroup 线程专门处理IO事件的,一般设置为256个到512个(SocketChannel)
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            //启动的一个引导类,让开发者配置信息
            ServerBootstrap b = new ServerBootstrap(); // (2)
            //把线程组设置进去
            b.group(bossGroup, workerGroup)
                    //设置IO模型,使用的是java的NIO模型
                    .channel(NioServerSocketChannel.class) // (3)
                    //配置Channel-PPline当中的Handler(编解码器)
                    .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                           // ch.pipeline().addLast("fixedDecoder",new FixedLengthFrameDecoder(3));//定长
                            //ch.pipeline().addLast("lineDecoder",new LineBasedFrameDecoder(1024));//回车换行分隔
                           // ch.pipeline().addLast("DelimiterBasedFrameDecoder",new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, Unpooled.wrappedBuffer("||".getBytes())));
                            //指定长度
                            //ch.pipeline().addLast("lengthDecoder",new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,2,2,0));
                           ch.pipeline().addLast("stringDecoder", new StringDecoder(CharsetUtil.UTF_8));
                           ch.pipeline().addLast("stringEncoder", new StringEncoder(CharsetUtil.UTF_8));

                           // ch.pipeline().addLast("lineDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,3,2,-3,7));
                           // ch.pipeline().addLast("discardTwoBytes", new DiscardTwoBytes());
                           // ch.pipeline().addLast("subLineDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,1,2,-3,3));

                            ch.pipeline().addLast("myHandler", new SecondHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

            // Bind and start to accept incoming connections.
            // 绑定端口
            ChannelFuture f = b.bind(port).sync(); // (7)

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            //阻塞当前线程,直到Server端关闭
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 8080;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        new DiscardServer(port).run();
    }
}
