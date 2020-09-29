package com.tangyuxian.tcp;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyHandler2 implements Handler {
    @Override
    public void ChannelRead(HandlerContext ctx, Object msg) {
        String str = (String) msg;
        //处理业务
        log.debug(str);
        ctx.getMyChannel().doWrite("hello client");
        if(str.equals("flush")){
            ctx.getMyChannel().flush();
        }

    }

    @Override
    public void write(HandlerContext ctx, Object msg) {
        log.debug(msg.toString());
        msg+="!!!";
        ctx.write(msg);
    }

    @Override
    public void flush(HandlerContext ctx) {
        log.debug("flush");
        ctx.flush();
    }
}
