package com.lmx.core.proxy.Handler;

import com.lmx.core.LRpcBootstrap;
import com.lmx.core.discovery.Registry;
import com.lmx.core.netty.NettyBootstrapInitialization;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
@Slf4j
public class NettyInvovationHandler implements InvocationHandler {
    private Class<?> interfancecomsumer;

    //    注册中心的配置，通过这个类可以获取到注册中心
    private Registry registry;

    public NettyInvovationHandler(Class<?> interfancecomsumer, Registry registry) {
        this.interfancecomsumer = interfancecomsumer;
        this.registry = registry;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //                发现服务，选择一个可用的节点，
        String name = method.getName(); // 方法名称
        String serviceName = interfancecomsumer.getName(); // 当前调用接口的全类名

        InetSocketAddress ipAdress = registry.lookup(serviceName);//寻找该接口的所有可用节点
//                使用netty向服务端发送信息
        log.info("获取的ip地址是" + ipAdress);

//                取出与当前服务接口相关联的通过
        Channel serviceChannel = LRpcBootstrap.SERVER_CHANNEL_CACHE.get(ipAdress);
//                如果channel不存在，重新连接
        if (serviceChannel == null) {
            // 获取唯一的bootstrap对象，单例
            Bootstrap bootstrap = NettyBootstrapInitialization.getBootstrap();

            // 启动客户端去连接服务器端，判断是否操作成功，连接成功后将
            CompletableFuture<Channel> channelFutureCompletableFuture = new CompletableFuture<>();
            ChannelFuture channelFuture = bootstrap.connect(ipAdress).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    if (channelFuture.isDone()) {
                        log.info("成功建立连接");
//                                如果结束
                        channelFutureCompletableFuture.complete(channelFuture.channel()); // 设置结果
                    } else if (!channelFuture.isSuccess()) {
//                                抛出异常
                        channelFutureCompletableFuture.completeExceptionally(channelFuture.cause());
                    }
                }
            }); // 阻塞，直到获取到channel
            serviceChannel = channelFutureCompletableFuture.get(3, TimeUnit.SECONDS);
            LRpcBootstrap.SERVER_CHANNEL_CACHE.put(ipAdress, serviceChannel);
        }

//                此时说明连接失败，抛出异常
        if (serviceChannel == null) {
            throw new RuntimeException("channel error");
        }

        CompletableFuture<Object> objectCompletableFuture = new CompletableFuture<>();
        serviceChannel.writeAndFlush(Unpooled.copiedBuffer("hello,server".getBytes(StandardCharsets.UTF_8))).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {

//                        如果操作没有成功，抛出异常
                if (!channelFuture.isSuccess()) {
                    objectCompletableFuture.completeExceptionally(channelFuture.cause());
                }
            }
        });
//                保存当前的CompletableFuture
        LRpcBootstrap.PEDDING_Future.put(1L, objectCompletableFuture);
        return objectCompletableFuture.get(3, TimeUnit.SECONDS);

    }
}
