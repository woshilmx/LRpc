package com.lmx.core.handler;

import com.lmx.core.LRpcBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class MyServerhandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {

        String s = byteBuf.toString(StandardCharsets.UTF_8);
        log.info("服务端收到消息: {}", s);
//      根据报文调用对应的方法
        channelHandlerContext.channel().writeAndFlush(Unpooled.copiedBuffer("服务端返回值".getBytes(StandardCharsets.UTF_8)));
    }
}
