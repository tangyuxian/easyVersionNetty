package com.yihuizhi.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

public class NioServerStart {
    public static void main(String[] args) throws IOException {
        //创建一个ServerSocket
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        //设置端口号
        serverChannel.bind(new InetSocketAddress(8085));
        //设置异步锁
        serverChannel.configureBlocking(false);
        //创建一个Selector查询器
        Selector selector = SelectorProvider.provider().openSelector();
        //把ServerSocketChannel注册到事件查询器上,并且关注OP_ACCEPT事件
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
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
                    socketChannel.register(selector, SelectionKey.OP_READ);
                }
                //可读事件
                if (selectionKey.isReadable()) {
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    try {
                        int readNum = socketChannel.read(buffer);
                        if (readNum == -1) { //读取流结束,关闭socket
                            System.out.println("读取到-1,关闭服务");
                            selectionKey.channel(); //关闭SelectionKey
                            socketChannel.close();
                            break;
                        }

                        /*
                         * limit = position;position = 0;mark = -1;
                         * 翻转，也就是让flip之后的position到limit这块区域变成之前的0到position这块，
                         * 翻转就是将一个处于存数据状态的缓冲区变为一个处于准备取数据的状态
                         */
                        buffer.flip();//指针压缩一下,然后才能读数据;

                        byte[] bytes = new byte[readNum]; //设置byte数组大小
                        ByteBuffer byteBuffer = buffer.get(bytes, 0, readNum); //读取内容,从0开始,读取readNum个
                        System.out.println("消息内容是 = " + new String(bytes));

                        //在selectionKey上附加一个对象?
                        selectionKey.attach("hello client".getBytes());

                        //把selectionKey关注的事件切换为OP_WRITE
                        selectionKey.interestOps(SelectionKey.OP_WRITE);


                        //byte[] response = "client hello".getBytes();

                        /*
                            position = 0;limit = capacity;mark = -1;
                            有点初始化的味道，但是并不影响底层byte数组的内容
                         */
                        //  buffer.clear();
                        //
                        //  buffer.put(response);
                        //  buffer.flip();
                        //  socketChannel.write(buffer);


                    } catch (IOException e) {
                        selectionKey.channel(); //关闭SelectionKey
                        socketChannel.close();
                        System.out.println("数据读取时发生异常" + e);
                        continue;
                    }

                }
                //可写事件
                if (selectionKey.isWritable()) {
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                    //取出对象
                    byte[] bytes = (byte[]) selectionKey.attachment();
                    selectionKey.attach(null); //清空
                    System.out.println("可写事件发生,写入消息" + Arrays.toString(bytes));
                    if (bytes != null) {
                        socketChannel.write(ByteBuffer.wrap(bytes));
                    }
                    //把selectionKey关注的事件切换为OP_READ
                    selectionKey.interestOps(SelectionKey.OP_READ);
                }
            }

        }





    }
}
