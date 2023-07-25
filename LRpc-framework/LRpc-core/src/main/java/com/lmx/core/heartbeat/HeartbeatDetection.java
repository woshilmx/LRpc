package com.lmx.core.heartbeat;


import com.lmx.core.LRpcBootstrap;
import com.lmx.core.ServiceConfig;
import com.lmx.core.compress.CompressFactory;
import com.lmx.core.discovery.Registry;
import com.lmx.core.messageenum.RequestTypeEnum;
import com.lmx.core.netty.NettyBootstrapInitialization;
import com.lmx.core.serialization.SerializaFactory;
import com.lmx.core.transport.message.LRpcRequest;
import com.lmx.core.transport.message.LRpcRespose;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.*;

/**
 * 实现心跳检测的
 */
@Slf4j
public class HeartbeatDetection {
    // 定义ScheduledExecutorService
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * 开始心跳检测任务
     */
    public static void startHeartbeatTask(String serviceName) {
        // 每隔2秒调用一次heartBeat()方法
        // 使用lambda表达式将serviceName作为参数传递给heartBeat方法
        Runnable task = () -> heartBeat(serviceName);
        // 每隔2秒调用一次heartBeat()方法
        scheduler.scheduleAtFixedRate(task, 0, 2, TimeUnit.SECONDS);
    }

    /**
     * 停止心跳检测任务
     */
    public void stopHeartbeatTask() {
        scheduler.shutdown();
    }

    /**
     * 心跳检测的方法,订阅哪个方法，启动哪个的心跳检测
     */
    static void heartBeat(String serviceName) {
        final Registry registry = LRpcBootstrap.getInstance().getRegistry();
        List<InetSocketAddress> lookup = registry.lookup(serviceName);
//            遍历获取
        for (InetSocketAddress inetSocketAddress : lookup) {

//                构建channel
            Channel serviceChannel = LRpcBootstrap.SERVER_CHANNEL_CACHE.get(inetSocketAddress);
            if (serviceChannel == null) {
                //        建立连接
                Bootstrap bootstrap = NettyBootstrapInitialization.getBootstrap();
                ChannelFuture channelFuture = null;
                try {
                    channelFuture = bootstrap.connect(inetSocketAddress).sync();
                    serviceChannel = channelFuture.channel();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

//                生成一个心跳请求
            Long id = LRpcBootstrap.idGenerator.getId();
            LRpcRequest lRpcRequest = LRpcRequest.builder()
                    .requestId(id) // 获取唯一请求id
                    .compressType(CompressFactory.getCompressWapper(LRpcBootstrap.COMPRESS_TYPE).getCode())
                    .serializationType(SerializaFactory.getSerializa(LRpcBootstrap.SERIALIZA_TYPE).getCode())
                    .requestType(RequestTypeEnum.HEART_BEAT_REQUEST.getCode()) // 构建请求
                    .build();
//                发送请求
            if (serviceChannel != null) {
                // 启动客户端去连接服务器端，判断是否操作成功，连接成功后将
//                CompletableFuture<Channel> channelFutureCompletableFuture = new CompletableFuture<>();
                try {

                    //                发送请求
                    CompletableFuture<Object> objectCompletableFuture = new CompletableFuture<>();
//        将封装好的请求写出去
                    serviceChannel.writeAndFlush(lRpcRequest).sync();
                    log.info("{}心跳检测请求发送成功", inetSocketAddress);
//                保存当前的CompletableFuture
                    LRpcBootstrap.PEDDING_Future.put(id, objectCompletableFuture);

//                    获取请求结果
                    LRpcRespose lRpcRespose = (LRpcRespose) objectCompletableFuture.get(3, TimeUnit.SECONDS);// 结果等待三秒
                    log.info("心跳检测响应结果{}", lRpcRespose.toString());
//                    获取时间戳，计算响应时间，保存
                    long timestamp = lRpcRespose.getTimestamp();
                    long currentTimeMillis = System.currentTimeMillis();
                    long timeDifference = currentTimeMillis - timestamp;
//                    保存时间差值
                    LRpcBootstrap.SERVICE_RESPOSE_TIME.put(inetSocketAddress, timeDifference); // 保存时间差值

                    LRpcBootstrap.SERVER_CHANNEL_CACHE.put(inetSocketAddress, serviceChannel); // 保存通道
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        //        记录检测时间
    }
}





