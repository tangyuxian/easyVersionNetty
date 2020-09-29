package com.yihuizhi.tcp;

import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

@Slf4j
public class MyHandler1 implements Handler {
    @Override
    public void ChannelRead(HandlerContext ctx, Object msg) {
        log.debug(msg.toString());

        //解码(由底层的Byte[]转换为对象的过程)
        ByteBuffer buffer = (ByteBuffer) msg;
        int l = buffer.limit();
        byte[] content = new byte[l];
        buffer.get(content); //将buffer写入到content上
        String str = new String(content);

        //向下传递
        ctx.fireChannelRead(str);

        //磁化对象需要释放资源
        buffer.clear();
    }

    @Override
    public void write(HandlerContext ctx, Object msg) {
        log.debug(msg.toString());

        //编码(把一个对象转换成二进制的过程)
        ByteBuffer buffer = ByteBuffer.wrap(msg.toString().getBytes());
        ctx.write(buffer);

    }

    @Override
    public void flush(HandlerContext ctx) {
        log.debug("flush");
        ctx.flush();
    }
}
