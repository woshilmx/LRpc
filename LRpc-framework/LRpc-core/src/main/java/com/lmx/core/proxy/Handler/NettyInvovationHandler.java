package com.lmx.core.proxy.Handler;

import com.lmx.annotation.Retry;
import com.lmx.core.LRpcBootstrap;
import com.lmx.core.compress.CompressFactory;
import com.lmx.core.discovery.Registry;
import com.lmx.core.exception.CircuitBreakerException;
import com.lmx.core.exception.ResposeException;
import com.lmx.core.loadbalancer.LoadBalancer;
import com.lmx.core.loadbalancer.iml.RoundLoadBalancer;
import com.lmx.core.messageenum.RequestTypeEnum;
import com.lmx.core.messageenum.ResposeCode;
import com.lmx.core.protection.CircuitBreaker;
import com.lmx.core.serialization.SerializaFactory;
import com.lmx.core.transport.message.LRpcRespose;
import com.lmx.generator.IDGenerator;
import com.lmx.core.netty.NettyBootstrapInitialization;
import com.lmx.core.transport.message.LRpcRequest;
import com.lmx.core.transport.message.Payload;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NettyInvovationHandler implements InvocationHandler {
    private Class<?> interfancecomsumer;

//    //    注册中心的配置，通过这个类可以获取到注册中心
//    private Registry registry;

    public NettyInvovationHandler(Class<?> interfancecomsumer) {
        this.interfancecomsumer = interfancecomsumer;
//        this.registry = registry;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Retry annotation = method.getAnnotation(Retry.class);
        int timeout = annotation.timeout();
        int trycount = annotation.trycount();
        CircuitBreaker circuitBreaker = null;
        while (true) {
            try {
                //                发现服务，选择一个可用的节点，
                String name = method.getName(); // 方法名称
                String serviceName = interfancecomsumer.getName(); // 当前调用接口的全类名

                //        创建一个负载均衡器，在负载均衡器中，选择合适的节点
                //        RoundLoadBalancer roundLoadBalancer = new RoundLoadBalancer(); // 使用工厂
                LoadBalancer loadbalancer = LRpcBootstrap.getInstance().getConfiguration().getLOADBALANCER();

                InetSocketAddress ipAdress = loadbalancer.getLoadBalance(serviceName);


                circuitBreaker = LRpcBootstrap.getInstance().getConfiguration().CircuitBreaker_Ip_Map.get(ipAdress);
                if (circuitBreaker == null) {
                    circuitBreaker = new CircuitBreaker(3, 1000); // 时间间隔为一秒
                    LRpcBootstrap.getInstance().getConfiguration().CircuitBreaker_Ip_Map.put(ipAdress, circuitBreaker);
                }
                boolean b = circuitBreaker.allowRequest();
                if (!b) {
                    log.info("熔断器启动，不被允许");
                    break;
//                    System.out.println();
//                    return;
                } else {
                    //        InetSocketAddress ipAdress = registry.lookup(serviceName);//寻找该接口的所有可用节点
//                使用netty向服务端发送信息
                    log.info("获取的ip地址是" + ipAdress);

//                取出与当前服务接口相关联的通过
                    Channel serviceChannel = LRpcBootstrap.SERVER_CHANNEL_CACHE.get(ipAdress);
//                如果channel不存在，重新连接,或者为关闭状态
                    if (serviceChannel == null || !serviceChannel.isOpen()) {
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

//       -------------------------封装报文---------------------------------------
//        封装有关调用接口的信息
                    Payload build = Payload.builder()
                            .interfanceName(interfancecomsumer.getName())
                            .methodName(method.getName())
                            .parameterType(method.getParameterTypes())
                            .returnType(method.getReturnType())
                            .parameterValue(args)
                            .build();
//        封装请求
                    final Long id = LRpcBootstrap.getInstance().getConfiguration().getIdGenerator().getId();
                    log.info("序列化方式是" + LRpcBootstrap.getInstance().getConfiguration().getSERIALIZA_TYPE());
                    LRpcRequest lRpcRequest = LRpcRequest.builder()
                            .requestId(id) // 获取唯一请求id
                            .compressType(CompressFactory.getCompressWapper(LRpcBootstrap.getInstance().getConfiguration().getCOMPRESS_TYPE()).getCode())
                            .serializationType(SerializaFactory.getSerializa(LRpcBootstrap.getInstance().getConfiguration().getSERIALIZA_TYPE()).getCode())
                            .requestType(RequestTypeEnum.COMMEN_REQUEST.getCode()) // 构建请求
                            .timestamp(new Date().getTime())
                            .payload(build)
                            .build();
                    CompletableFuture<Object> objectCompletableFuture = new CompletableFuture<>();
//        将封装好的请求写出去
                    serviceChannel.writeAndFlush(lRpcRequest).addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture channelFuture) throws Exception {

//                        如果操作没有成功，抛出异常
                            if (!channelFuture.isSuccess()) {
                                objectCompletableFuture.completeExceptionally(channelFuture.cause());
                            }
                        }
                    });
//                保存当前的CompletableFuture
                    LRpcBootstrap.PEDDING_Future.put(id, objectCompletableFuture);

//        返回的是响应的结果
                    LRpcRespose lRpcRespose = (LRpcRespose) objectCompletableFuture.get(3, TimeUnit.SECONDS);

                    if (lRpcRespose.getCode() == ResposeCode.CORRENT_CODE.getCode()) {
                        return lRpcRespose.getBody();
                    } else if (lRpcRespose.getCode() == ResposeCode.SHUTDOWM_CODE.getCode()) {
                        log.error("服务端停机中");
                        //        移除通道连接
                        LRpcBootstrap.SERVER_CHANNEL_CACHE.remove(ipAdress);
                        final List<InetSocketAddress> inetSocketAddresses = LRpcBootstrap.STRING_LIST_MAP.get(interfancecomsumer.getName());
                        inetSocketAddresses.remove(inetSocketAddresses);
                        throw new ResposeException(lRpcRespose.getCode(),"服务端停机，重新进行负载均衡");
                    }
//                    else if (lRpcRespose.getCode() == ResposeCode.RATE_LIMATE.getCode()) {
////                    如果被限流了
//                        log.error(method.getName() + "被限流了");
//                        circuitBreaker.recordFailure();
//                    }
                }

            } catch (Exception e) {
//                如果是熔断器异常，直接抛出异常
                trycount--;
                Thread.sleep(timeout);
                if (trycount < 0) {
                    break;
                }
                log.info("{}接口的{}方法正在进行第{}次重试", interfancecomsumer.getName(), method.getName(), trycount);
//                circuitBreaker.recordFailure();
                if (circuitBreaker != null) {
                    circuitBreaker.recordFailure();
                }
            }
        }
//        throw new RuntimeException("3次异常重试错误" + interfancecomsumer.getName() + "\t" + method.getName());
        return null;

    }
}
