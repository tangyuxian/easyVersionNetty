package com.tangyuxian.tcp;

import java.io.IOException;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

public class EventLoop implements Runnable {
    public Selector selector;
    private Thread thread;
    private Queue<Runnable> taskQueue = new LinkedBlockingQueue<>(32);

    EventLoop() throws IOException {
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
    public void register(SocketChannel channel, int keyOps) {
        //注册的逻辑封装成一个任务
        taskQueue.add(() -> {
            try {
                MyChannel myChannel = new MyChannel(channel, EventLoop.this);
                SelectionKey selectionKey = channel.register(selector, keyOps);
                selectionKey.attach(myChannel);
            } catch (ClosedChannelException e) {
                System.out.println("错误信息" + e);
                e.printStackTrace();
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
                        MyChannel myChannel = (MyChannel) selectionKey.attachment();
                        if (selectionKey.isReadable()) {
                            myChannel.read(selectionKey);
                            continue;
                        }

                        //可写事件
                        if (selectionKey.isWritable()) {
                            myChannel.write(selectionKey);
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
