package com.tangyuxian.tcp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

public class EventLoopStart implements Runnable {
    private Selector selector;
    private Thread thread;
    private Queue<Runnable> taskQueue = new LinkedBlockingQueue<>(32);

    EventLoopStart() throws IOException {
        /*
            分成两个Selector,需要两个线程,该Selector主要负责OP_Read,OP_Write
         */
        this.selector = SelectorProvider.provider().openSelector();
        this.thread = new Thread(this);
        this.thread.start();
    }

    /**
     * 把Channel注册到事件查询器上
     *
     * @param channel
     * @param keyOps
     */
    public void register(final SocketChannel channel, final int keyOps) {
        //注册的逻辑封装成一个任务
        taskQueue.add(new Runnable() {
            @Override
            public void run() {
                try {
                    channel.register(selector, keyOps);
                } catch (ClosedChannelException e) {
                    System.out.println("错误信息"+e);
                    e.printStackTrace();
                }
            }
        });
        /*
            使尚未返回的第一个操作立即返回
            如果另一个线程正在select()或select(long)方法的调用时,会立即返回
         */
        //唤起selector上阻塞的线程
        selector.wakeup();


        /*
            直接使用此方法会产生死锁
            原因:selector.select()会持有selector内部的一把锁,并没有释放,所以不能用其它线程去调用
            channel.register(selector,keyOps);
         */

}


    public void run() {
        //是否为中断状态 不中断状态处理逻辑
        while (!Thread.interrupted()) {
            try {
                System.out.println(this.thread + "开始查询IO事件");
                //阻塞方法,等待系统有IO事件发生
                int eventNum = selector.select();
                System.out.println("EventLoop系统发生IO事件,数量为" + eventNum);
                if (eventNum > 0) {
                    Set<SelectionKey> keySet = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = keySet.iterator();

                    while (iterator.hasNext()) {
                        SelectionKey selectionKey = iterator.next();
                        iterator.remove();
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
                Runnable task;
                while ((task = taskQueue.poll()) != null) {
                    task.run();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }
}
