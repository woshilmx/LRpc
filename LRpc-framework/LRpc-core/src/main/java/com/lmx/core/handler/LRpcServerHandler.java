package com.lmx.core.handler;


import com.lmx.core.LRpcBootstrap;
import com.lmx.core.ServiceConfig;
import com.lmx.core.configure.Configuration;
import com.lmx.core.messageenum.RequestTypeEnum;
import com.lmx.core.messageenum.ResposeCode;
import com.lmx.core.protection.TokenBucketRateLimter;
import com.lmx.core.shudown.ShudownHookContant;
import com.lmx.core.transport.message.LRpcRequest;
import com.lmx.core.transport.message.LRpcRespose;
import com.lmx.core.transport.message.Payload;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 得到LRpcRequest对象
 */
@Slf4j
public class LRpcServerHandler extends SimpleChannelInboundHandler<LRpcRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, LRpcRequest lRpcRequest) throws Exception {

//        如果是true
        if (ShudownHookContant.IS_OPEN.get()) {
            LRpcRespose lRpcRespose = new LRpcRespose();
            lRpcRespose.setBody(null);
            lRpcRespose.setCode(ResposeCode.SHUTDOWM_CODE.getCode()); //返回停机的响应
            lRpcRespose.setCompressType(lRpcRequest.getCompressType()); // 压缩类型
            lRpcRespose.setTimestamp(lRpcRequest.getTimestamp());
            lRpcRespose.setSerializationType(lRpcRequest.getSerializationType());  // 序列化类型
            lRpcRespose.setRequestId(lRpcRequest.getRequestId());  // 请求id
            channelHandlerContext.writeAndFlush(lRpcRespose);
            return;
        }

//        开始计数,请求开始前增加1
        ShudownHookContant.REQUEST_COUNT.increment();
//        如果是心跳请求，不用限流
        LRpcRespose lRpcRespose = new LRpcRespose();
        if (lRpcRequest.getRequestType() == RequestTypeEnum.HEART_BEAT_REQUEST.getCode()) {
//       TODO 封装响应对象，返回数据 //
            lRpcRespose.setBody(null);
            lRpcRespose.setCode(ResposeCode.CORRENT_CODE.getCode());
            lRpcRespose.setCompressType(lRpcRequest.getCompressType()); // 压缩类型
            lRpcRespose.setTimestamp(lRpcRequest.getTimestamp());
            lRpcRespose.setSerializationType(lRpcRequest.getSerializationType());  // 序列化类型
            lRpcRespose.setRequestId(lRpcRequest.getRequestId());  // 请求id
            log.info("心跳检测完成");
//            channelHandlerContext.writeAndFlush(lRpcRespose);
//            return;
        } else {
//            如果是普通请求
            SocketAddress socketAddress = channelHandlerContext.channel().remoteAddress();
            Configuration configuration = LRpcBootstrap.getInstance().getConfiguration();
            Map<SocketAddress, TokenBucketRateLimter> rate_ip_limter = configuration.rate_ip_limter;
            TokenBucketRateLimter tokenBucketRateLimter = rate_ip_limter.get(socketAddress);
            Object invoke = null;

            if (tokenBucketRateLimter == null) {
                tokenBucketRateLimter = new TokenBucketRateLimter(5, 5);
                configuration.rate_ip_limter.put(socketAddress, tokenBucketRateLimter);
            }

            boolean b = tokenBucketRateLimter.allowRequest();
//            如果不允许通过
//            LRpcRespose lRpcRespose = new LRpcRespose();
//
            if (!b) {
                //   不允许通过
                lRpcRespose.setBody(invoke);
                lRpcRespose.setCode(ResposeCode.RATE_LIMATE.getCode());
                lRpcRespose.setCompressType(lRpcRequest.getCompressType()); // 压缩类型
                lRpcRespose.setTimestamp(lRpcRequest.getTimestamp());
                lRpcRespose.setSerializationType(lRpcRequest.getSerializationType());  // 序列化类型
                lRpcRespose.setRequestId(lRpcRequest.getRequestId());  // 请求id
                log.info("方法限流了");
            } else {
//                允许通过
                Payload payload = lRpcRequest.getPayload();
                if (payload != null) {
                    ServiceConfig<?> serviceConfig = LRpcBootstrap.serviceConfigMap.get(payload.getInterfanceName());
                    Object ref = serviceConfig.getRef(); // 获取具体实现类
                    Class<?> aClass = ref.getClass();
                    Method method = aClass.getMethod(payload.getMethodName(), payload.getParameterType());
                    invoke = method.invoke(ref, payload.getParameterValue()); // 调用方法获取到返回值，然后写会客户端
                }


//       TODO 封装响应对象，返回数据 //
//            LRpcRespose lRpcRespose = new LRpcRespose();
                lRpcRespose.setBody(invoke);
                lRpcRespose.setCode(ResposeCode.CORRENT_CODE.getCode());
                lRpcRespose.setCompressType(lRpcRequest.getCompressType()); // 压缩类型
                lRpcRespose.setTimestamp(lRpcRequest.getTimestamp());
                lRpcRespose.setSerializationType(lRpcRequest.getSerializationType());  // 序列化类型
                lRpcRespose.setRequestId(lRpcRequest.getRequestId());  // 请求id
                log.info("方法调用完成");
            }


//        写出结果，进入下一个handler

        }
        channelHandlerContext.channel().writeAndFlush(lRpcRespose);
        ShudownHookContant.REQUEST_COUNT.decrement(); // 请求结束减少1；
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close();
        throw new RuntimeException(cause);

    }
}
