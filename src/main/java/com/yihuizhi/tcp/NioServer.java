package com.yihuizhi.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Set;

public class NioServer {
    public static void main(String[] args) throws IOException {
        //创建一个ServerSocket
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        //设置端口号
        serverChannel.bind(new InetSocketAddress(8086));
        //设置异步锁
        serverChannel.configureBlocking(false);
        //创建一个Selector查询器
        Selector selector = SelectorProvider.provider().openSelector();
        //把ServerSocketChannel注册到事件查询器上,并且关注OP_ACCEPT事件

        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        //创建另外一个事件查询器
        //EventLoop eventLoop = new EventLoop();

        //创建一事件查询器
        EventLoopGroup eventLoopGroup = new EventLoopGroup();


        while (true){
            //阻塞方法,等待系统有IO事件发生
            int eventNum = selector.select();
            System.out.println("系统发生IO事件,数量为"+eventNum);
            Set<SelectionKey> keySet = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keySet.iterator();
            while (iterator.hasNext()){
                SelectionKey selectionKey = iterator.next();
                iterator.remove();
                //连接接入事件
                if(selectionKey.isAcceptable()){
                    //accept事件保证获取的channel类型必然是ServerSocketChannel
                    ServerSocketChannel ssc = (ServerSocketChannel) selectionKey.channel();
                    //事件机制触发,必然会有连接
                    SocketChannel socketChannel = ssc.accept();
                    //设置SocketChannel非阻塞模式
                    socketChannel.configureBlocking(false);
                    System.out.println("服务器接受了一个新连接"+socketChannel.getRemoteAddress());
                    //把SocketChannel注册到事件查询器上,并且关注OP_READ事件
                    //socketChannel.register(selector, SelectionKey.OP_READ);
                    //eventLoop.register(socketChannel,SelectionKey.OP_READ);
                    eventLoopGroup.register(socketChannel,SelectionKey.OP_READ);

                }

            }

        }





    }
}
