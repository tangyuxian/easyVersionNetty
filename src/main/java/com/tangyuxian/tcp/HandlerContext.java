package com.tangyuxian.tcp;

public class HandlerContext {
    private Handler handler;
    private MyChannel myChannel;

    //双向链表
    HandlerContext prev;
    HandlerContext next;


    public HandlerContext(Handler handler, MyChannel myChannel) {
        this.handler = handler;
        this.myChannel = myChannel;
    }

    public void fireChannelRead(Object msg){
        HandlerContext n = this.next;
        if(n != null){
            n.handler.ChannelRead(n,msg);
        }
    }
    public void write(Object msg){
        HandlerContext p = this.prev;
        if(p != null){
            p.handler.write(p,msg);
        }
    }

    public void flush(){
        HandlerContext p = this.prev;
        if(p != null){
            p.handler.flush(p);
        }
    }

    public MyChannel getMyChannel() {
        return myChannel;
    }
}
