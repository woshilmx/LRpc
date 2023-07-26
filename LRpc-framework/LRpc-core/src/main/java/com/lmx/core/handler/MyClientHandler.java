package com.lmx.core.handler;

import com.lmx.core.LRpcBootstrap;
import com.lmx.core.messageenum.ResposeCode;
import com.lmx.core.transport.message.LRpcRespose;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class MyClientHandler extends SimpleChannelInboundHandler<LRpcRespose> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, LRpcRespose lRpcRespose) throws Exception {


//        读取消息，获取对应的CompletableFuture，然后调用complete，返回
//        TODO 通过请求的id获取到 PEDDING_Future中的CompletableFuture，然后返回
        final long requestId = lRpcRespose.getRequestId();
        CompletableFuture<Object> objectCompletableFuture = LRpcBootstrap.PEDDING_Future.get(requestId);
//        if (lRpcRespose.getCode() == ResposeCode.CORRENT_CODE.getCode()) {
//
//            objectCompletableFuture.complete(lRpcRespose.getBody());
//        } else {
//            throw new RuntimeException("调用失败");
//        }
        objectCompletableFuture.complete(lRpcRespose);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("{}抛出异常:{}", ctx.channel().remoteAddress(), cause.getMessage());
        ctx.channel().close();

    }

}
