package com.lmx.core.handler;

import com.lmx.core.LRpcBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.EventExecutorGroup;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class MyClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {


//        读取消息，获取对应的CompletableFuture，然后调用complete，返回
        CompletableFuture<Object> objectCompletableFuture = LRpcBootstrap.PEDDING_Future.get(1L);
        objectCompletableFuture.complete(byteBuf.toString(StandardCharsets.UTF_8));
    }
}
