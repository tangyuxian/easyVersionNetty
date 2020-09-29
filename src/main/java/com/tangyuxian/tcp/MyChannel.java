package com.tangyuxian.tcp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class MyChannel  {
    private SocketChannel channel;
    private EventLoop eventLoop;
    private Queue<ByteBuffer> writeQueue = new ArrayBlockingQueue<ByteBuffer>(16);
    private PPLine ppLine;

    public MyChannel(SocketChannel channel, EventLoop eventLoop) {
        this.channel = channel;
        this.eventLoop = eventLoop;
        this.ppLine = new PPLine(this, eventLoop);
        this.ppLine.addLast(new MyHandler1());
        this.ppLine.addLast(new MyHandler2());

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
            this.ppLine.headCtx.fireChannelRead(buffer);


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

    public void doWrite(Object msg){
        this.ppLine.tailCtx.write(msg);
    }

    public void addWriteQueue(ByteBuffer buffer){
        writeQueue.add(buffer);
    }

    public void flush(){
        this.ppLine.tailCtx.flush();
    }

    public void doFlush(){
        this.channel.keyFor(eventLoop.selector).interestOps(SelectionKey.OP_WRITE);
    }

}
