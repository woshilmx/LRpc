package com.lmx.core.handler;


import com.lmx.core.LRpcBootstrap;
import com.lmx.core.ServiceConfig;
import com.lmx.core.messageenum.ResposeCode;
import com.lmx.core.transport.message.LRpcRequest;
import com.lmx.core.transport.message.LRpcRespose;
import com.lmx.core.transport.message.Payload;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

/**
 * 得到LRpcRequest对象
 */
public class LRpcServerHandler extends SimpleChannelInboundHandler<LRpcRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, LRpcRequest lRpcRequest) throws Exception {
        Payload payload = lRpcRequest.getPayload();
        ServiceConfig<?> serviceConfig = LRpcBootstrap.serviceConfigMap.get(payload.getInterfanceName());
        Object ref = serviceConfig.getRef(); // 获取具体实现类
        Class<?> aClass = ref.getClass();
        Method method = aClass.getMethod(payload.getMethodName(), payload.getParameterType());
        Object invoke = method.invoke(ref, payload.getParameterValue()); // 调用方法获取到返回值，然后写会客户端

//       TODO 封装响应对象，返回数据 //
        LRpcRespose lRpcRespose = new LRpcRespose();
        lRpcRespose.setBody(invoke);
        lRpcRespose.setCode(ResposeCode.CORRENT_CODE.getCode());
        lRpcRespose.setCompressType(lRpcRespose.getCompressType()); // 压缩类型
        lRpcRespose.setSerializationType(lRpcRequest.getSerializationType());  // 序列化类型
        lRpcRespose.setRequestId(lRpcRequest.getRequestId());  // 请求id

//        写出结果，进入下一个handler
        channelHandlerContext.channel().writeAndFlush(lRpcRespose);

    }
}
