package com.tangyuxian.tcp;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

public class EventLoopGroupStart {

    private EventLoopStart[] eventLoops = new EventLoopStart[16];


    private final AtomicInteger idx = new AtomicInteger(0);//可以原子自增的Integer计数器

    EventLoopGroupStart() throws IOException {
        for (int i = 0; i < eventLoops.length; i++) {
            eventLoops[i] = new EventLoopStart();
        }
    }


    public EventLoopStart next() {
        /*
        与运算符
        与运算符用符号“&”表示，即两个操作数中位都为1，结果才为1，否则结果为0
         */
        //轮询算法
        return eventLoops[idx.getAndIncrement() & eventLoops.length - 1];
    }

    public void register( SocketChannel channel,  int keyOps) {
        next().register(channel,keyOps);
    }
}

