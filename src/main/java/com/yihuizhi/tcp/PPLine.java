package com.yihuizhi.tcp;

import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

@Slf4j
public class PPLine {
    private MyChannel channel;
    private EventLoop eventLoop;

    HandlerContext headCtx;
    HandlerContext tailCtx;

    public PPLine(MyChannel channel, EventLoop eventLoop) {
        this.channel = channel;
        this.eventLoop = eventLoop;
        PPHandler handler = new PPHandler();
        this.headCtx = new HandlerContext(handler,channel);
        this.tailCtx = new HandlerContext(handler,channel);

        //构建链表
        this.headCtx.next = this.tailCtx;
        this.tailCtx.prev = this.headCtx;
    }


    public void addLast(Handler handler){
        HandlerContext context = new HandlerContext(handler, channel);
        HandlerContext p = this.tailCtx.prev;
        p.next = context;
        context.prev = p;
        context.next = tailCtx;
        tailCtx.prev = context;
    }

    public class PPHandler implements Handler{

        @Override
        public void ChannelRead(HandlerContext ctx, Object msg) {
//           log.debug(msg.toString());
            System.out.println("最后的Handler,释放资源"+msg);
        }

        @Override
        public void write(HandlerContext ctx, Object msg) {
           log.debug(msg.toString());
           if(!(msg instanceof ByteBuffer)){
               throw  new RuntimeException("类型错误"+msg.getClass());
           }
           PPLine.this.channel.addWriteQueue((ByteBuffer) msg);
        }

        @Override
        public void flush(HandlerContext ctx) {
          log.debug("flush");
          PPLine.this.channel.doFlush();

        }
    }

}
