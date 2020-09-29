package com.yihuizhi.tcp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class MyChannelStart {
    private SocketChannel channel;
    private EventLoop eventLoop;
    private Queue<ByteBuffer> writeQueue = new ArrayBlockingQueue<ByteBuffer>(16);


    public MyChannelStart(SocketChannel channel, EventLoop eventLoop) {
        this.channel = channel;
        this.eventLoop = eventLoop;
    }

    public void read(SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        try {
            int readNum = socketChannel.read(buffer);
            if (readNum == -1) { //读取流结束,关闭socket
                System.out.println("读取到-1,关闭服务");
                selectionKey.channel(); //关闭SelectionKey
                socketChannel.close();
                return;
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
           // selectionKey.attach("hello client".getBytes());

            //加入缓冲区
            writeQueue.add(ByteBuffer.wrap("helloClient".getBytes()));

            if(new String((bytes)).equals("flush")){
                //把selectionKey关注的事件切换为OP_WRITE
                selectionKey.interestOps(SelectionKey.OP_WRITE);
            }

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
        }
    }

    public void write(SelectionKey selectionKey) throws IOException {
        ByteBuffer buffer;
        while ((buffer = writeQueue.poll()) != null){
            channel.write(buffer);
        }

        //把selectionKey关注的事件切换为OP_READ
        selectionKey.interestOps(SelectionKey.OP_READ);
    }
}
