package com.yihuizhi.netty_decodeAndEdcode;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;

import java.io.IOException;
import java.net.Socket;

public class TcpClient {
    public static void main(String[] args) throws IOException, InterruptedException {
        Socket socket = new Socket("192.168.1.126", 8080);
        String json1 = "{c:'123'}\r\n";
        String json2 = "{d:'123'}\r\n";
        String  hexDump = ByteBufUtil.hexDump(json1.getBytes());
        ByteBufUtil.hexDump(json2.getBytes());
       // socket.getOutputStream().write(json1.getBytes());
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        String str = "Hello,World";

        buffer.writeBytes(str.getBytes());


        socket.getOutputStream().write(ByteBufUtil.getBytes(buffer));

        Thread.sleep(2000);
        socket.close();

    }
    public void  lengthFieldBasedFrameDecoder(){
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        String str = "Hello,World";
        /*
            lengthFieldOffset  起始位置,Length的偏移量,可以作为头部的长度
            lengthFieldLength  有效的数据体长度
            lengthAdjustment   满足公式:
                发送的字节数组bytes.length - lengthFieldLength = bytes[lengthFieldOffset, lengthFieldOffset+lengthFieldLength] + lengthFieldOffset + lengthAdjustment
            initialBytesToStrip 将数据传递给下一个channel时去除包含描述长度的信息
         */

        //new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,2,0,0)
        buffer.writeShort(str.getBytes().length);
        buffer.writeBytes(str.getBytes());


        //new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,2,-2,0)
        buffer.writeShort(str.getBytes().length+2);
        buffer.writeBytes(str.getBytes());

        //new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,2,3,0,0)
        buffer.writeBytes("AB".getBytes()); //头信息
        buffer.writeMedium(str.getBytes().length); //3个字节
        buffer.writeBytes(str.getBytes());

        //new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,3,2,0)
        buffer.writeMedium(str.getBytes().length); //3个字节
        buffer.writeBytes("AB".getBytes()); //HDR信息
        buffer.writeBytes(str.getBytes());

        //new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,1,2,1,3)
        buffer.writeBytes("A".getBytes()); //头信息
        buffer.writeShort(str.getBytes().length); //3个字节
        buffer.writeBytes("B".getBytes()); //HDR信息
        buffer.writeBytes(str.getBytes());


        String hexDump = ByteBufUtil.hexDump(buffer);
        System.out.println("hexDump = " + hexDump);
    }
}
