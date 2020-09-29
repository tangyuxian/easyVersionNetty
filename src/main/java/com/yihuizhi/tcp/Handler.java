package com.yihuizhi.tcp;

public interface Handler {

    /**
     * 读事件
     * @param ctx
     * @param msg
     */
    void ChannelRead(HandlerContext ctx,Object msg);

    /**
     * 写事件
     * @param ctx
     * @param msg
     */
    void write(HandlerContext ctx,Object msg);

    /**
     * 刷新数据
     * @param ctx
     */
    void flush(HandlerContext ctx);


}
