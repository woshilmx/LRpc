package com.lmx.core.heartbeat;

import com.lmx.core.LRpcBootstrap;
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
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class HeartbeatDetection {

    private static final Timer timer = new Timer();

    /**
     * 开始心跳检测任务
     */
    public static void startHeartbeatTask(String serviceName) {
        // 每隔2秒调用一次heartBeat()方法
        // 使用lambda表达式将serviceName作为参数传递给heartBeat方法
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    heartBeat(serviceName);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }

            }
        };
        // 每隔2秒调用一次heartBeat()方法
        timer.scheduleAtFixedRate(task, 0, 2000);
    }

    /**
     * 停止心跳检测任务
     */
    public void stopHeartbeatTask() {
        timer.cancel();
    }

    /**
     * 心跳检测的方法,订阅哪个方法，启动哪个的心跳检测
     */
    public static void heartBeat(String serviceName) {
         Registry registry = LRpcBootstrap.getInstance().getRegistry();
        List<InetSocketAddress> lookup = registry.lookup(serviceName);
        log.info(serviceName + "正在进行心跳检测");

        if (lookup == null) {
            log.info("{}无可用服务节点", serviceName);
            return;
        }
        // 用一个Map来记录每个地址的失败次数
        Map<InetSocketAddress, Integer> failedAttempts = new HashMap<>();
        log.info(serviceName + "正在进行心跳检测1");
        // 遍历获取
        for (InetSocketAddress inetSocketAddress : lookup) {
            int failureCount = failedAttempts.getOrDefault(inetSocketAddress, 0);

            // 如果失败次数超过三次，跳过该地址
            if (failureCount >= 3) {
                log.warn("跳过地址 {}，因为失败次数超过三次", inetSocketAddress);
                continue;
            }
            log.info(serviceName + "正在进行心跳检测2");

            // 构建channel
            Channel serviceChannel = LRpcBootstrap.SERVER_CHANNEL_CACHE.get(inetSocketAddress);
            if (serviceChannel == null || !serviceChannel.isOpen()) {
                // 建立连接
                Bootstrap bootstrap = NettyBootstrapInitialization.getBootstrap();
                ChannelFuture channelFuture = null;
                try {
                    channelFuture = bootstrap.connect(inetSocketAddress).sync();
                    serviceChannel = channelFuture.channel();
                    log.info("与{}建立连接成功", inetSocketAddress);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            log.info(serviceName + "正在进行心跳检测3");

            if (serviceChannel == null) {
                log.info(serviceName + "无可用服务节点");
                return;
            }
            log.info(serviceName + "正在进行心跳检测4");
            //                生成一个心跳请求
            Long id = LRpcBootstrap.getInstance().getConfiguration().getIdGenerator().getId();
            LRpcRequest lRpcRequest = LRpcRequest.builder()
                    .requestId(id) // 获取唯一请求id
                    .compressType(CompressFactory.getCompressWapper(LRpcBootstrap.getInstance().getConfiguration().getCOMPRESS_TYPE()).getCode())
                    .serializationType(SerializaFactory.getSerializa(LRpcBootstrap.getInstance().getConfiguration().getSERIALIZA_TYPE()).getCode())
                    .requestType(RequestTypeEnum.HEART_BEAT_REQUEST.getCode()) // 构建请求
                    .build();
            log.info(serviceName + "正在进行心跳检测5");
            // 发送请求
            if (serviceChannel != null && serviceChannel.isOpen()) {
                try {
                    // 发送请求
                    CompletableFuture<Object> objectCompletableFuture = new CompletableFuture<>();
                    serviceChannel.writeAndFlush(lRpcRequest).sync();
                    log.info("{}心跳检测请求发送成功", inetSocketAddress);
                    LRpcBootstrap.PEDDING_Future.put(id, objectCompletableFuture);
                    log.info(serviceName + "正在进行心跳检测6");
                    // 获取请求结果
                    LRpcRespose lRpcRespose = (LRpcRespose) objectCompletableFuture.get(3, TimeUnit.SECONDS);
                    log.info("心跳检测响应结果{}", lRpcRespose.toString());
                    long timestamp = lRpcRespose.getTimestamp();
                    long currentTimeMillis = System.currentTimeMillis();
                    long timeDifference = currentTimeMillis - timestamp;
                    LRpcBootstrap.SERVICE_RESPOSE_TIME.put(inetSocketAddress, timeDifference);
                    LRpcBootstrap.SERVER_CHANNEL_CACHE.put(inetSocketAddress, serviceChannel);
                    log.info(serviceName + "正在进行心跳检测7");
                    // 成功请求时重置失败次数
                    failedAttempts.put(inetSocketAddress, 0);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    log.warn("{}发生异常", inetSocketAddress);
                    // 请求失败时增加失败次数并保存到Map中
                    failedAttempts.put(inetSocketAddress, failureCount + 1);
                }
            }
        }
        // 记录检测时间
    }
}





