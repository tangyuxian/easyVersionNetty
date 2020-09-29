package com.tangyuxian.netty_test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

public class ByteBufTest {
    public static void main(String[] args) {
//        byteBufStart();
        byteBufDuplicate();
    }

    public static void byteBufStart(){
        ByteBuf buf = PooledByteBufAllocator.DEFAULT.heapBuffer(16);
        buf.writeByte(1);
        System.out.println(buf.readByte());
        buf.discardReadBytes(); //将读过的字节进行丢弃
        System.out.println(buf.readerIndex()+"|"+buf.writerIndex()); //读和写的下标
        System.out.println(buf.readableBytes()+"|"+buf.writableBytes()); //剩余可读写情况
        buf.retain(); //增加引用计数器
        System.out.println(buf.refCnt()); //引用计数器
        buf.release();  //释放
    }

    /**
     * 复制索引
     */
    public static void byteBufDuplicate(){
        ByteBuf buf = PooledByteBufAllocator.DEFAULT.heapBuffer(16);
        buf.writeByte(1);
        buf.writeByte(2);
        buf.writeByte(3);
        buf.writeByte(4);
        buf.writeByte(5);
        System.out.println(buf.readerIndex()+"|"+buf.writerIndex()); //读和写的下标
        ByteBuf newBuf = buf.duplicate(); //复制一份索引出来,但是指向的内存地址是相同的;retainedDuplicate会使计数器加1

        newBuf.writeByte(6);
        System.out.println(buf.readByte());

    }

    public static void byteBufSlice(){
        ByteBuf buf = PooledByteBufAllocator.DEFAULT.heapBuffer(16);
        buf.writeByte(1);
        buf.writeByte(2);
        buf.writeByte(3);
        buf.writeByte(4);
        buf.writeByte(5);
        System.out.println(buf.readByte());
        System.out.println(buf.readByte());
        System.out.println(buf.readerIndex()+"|"+buf.writerIndex()); //读和写的下标

        ByteBuf newBuf = buf.slice(buf.readerIndex(), buf.readableBytes());
        newBuf.setByte(1,9);//通过指定的下标设置值
        System.out.println(newBuf.readerIndex()+"|"+newBuf.writerIndex()); //读和写的下标

        ByteBuf readBuf = buf.readSlice(buf.readerIndex());


    }
}
