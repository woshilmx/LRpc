package com.lmx.core;


import com.lmx.core.compress.CompressFactory;
import com.lmx.core.discovery.Registry;
import com.lmx.core.handler.LRpcRequestByteToMessageDecoder;
import com.lmx.core.handler.LRpcReposeMessageToByteEncoder;
import com.lmx.core.handler.LRpcServerHandler;
import com.lmx.core.loadbalancer.LoadBalancer;
import com.lmx.core.loadbalancer.iml.RoundLoadBalancer;
import com.lmx.core.serialization.SerializaFactory;
import com.lmx.generator.IDGenerator;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 依赖启动类
 */
@Slf4j
@Data
public class LRpcBootstrap {
    private static final LRpcBootstrap lRpcBootstrap = new LRpcBootstrap();
    private String applicationName = "default";
    private RegistryConfig registryConfig;

    private ProtocolConfig protocolConfig;
    private ZooKeeper zooKeeper;
    //    设置服务消费者与nettychannel通道缓存，避免每次调用方法重新连接
    public static final Map<InetSocketAddress, Channel> SERVER_CHANNEL_CACHE = new ConcurrentHashMap<>(16);
    // 保存的是服务提供者发布的接口的实现类
    public static final Map<String, ServiceConfig<?>> serviceConfigMap = new ConcurrentHashMap<>(16);
    //  阻塞的CompletableFuture，
    public static final Map<Long, CompletableFuture<Object>> PEDDING_Future = new ConcurrentHashMap<>(16);

    //    生成全局的id生成器，单例
    public static final IDGenerator idGenerator = new IDGenerator(1, 2);

    //    保存可用服务的列表
    public static final Map<String, List<InetSocketAddress>> STRING_LIST_MAP = new ConcurrentHashMap<>(16);
    /**
     * 注册中心
     */
    private Registry registry;

    public final static int SERVICE_PORT = 8890;
    public static String SERIALIZA_TYPE = "jdk"; // 序列化的方式,默认使用jdk
    public static String COMPRESS_TYPE = "gzip"; // 压缩的方式，默认使用gzip
    public static LoadBalancer LOADBALANCER = new RoundLoadBalancer(); // 默认使用轮询的负载均衡策略

    public LRpcBootstrap() {
//        执行引导类初始化的工作
    }

    /**
     * 使用单例模式，获取唯一的引导类对象
     */
    public static LRpcBootstrap getInstance() {
        return lRpcBootstrap;

    }

    /**
     * 执行应用的名称
     */
    public LRpcBootstrap application(String applicationName) {
        this.applicationName = applicationName;
        return this;

    }

    /**
     * 注册到注册中心中
     */
    public LRpcBootstrap registry(RegistryConfig registryConfig) {

        this.registryConfig = registryConfig;
        this.registry = registryConfig.getRegistry();
////        创建一个注册中心的连接对象 ,后续我们将对他进行抽象，现在无法扩展
//        zooKeeper = ZookeeperUtil.createZookeeper(Contanst.DEFAULT_CONNECTSTRING, Contanst.SESSIONTIMEOUT);
//        log.info("connect " + registryConfig.getConnectString() + "success");
        return this;
    }


    /**
     * 配置序列化协议
     */
    public LRpcBootstrap protocol(ProtocolConfig protocolConfig) {
        this.protocolConfig = protocolConfig;
        return this;
    }


    /**
     * 服务提供者将服务发布到注册中心中
     */
    public LRpcBootstrap service(ServiceConfig<?> service) {
        registry.registry(service);
//        当服务注册完成后，需要保存当前接口的具体实现，不需要再次创建该实例对象
        serviceConfigMap.put(service.getInterface().getName(), service);
        return this;
    }


    /**
     * 启动服务端引导类
     */
    public void start() {
        NioEventLoopGroup bossgroup = new NioEventLoopGroup();
        NioEventLoopGroup workgroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossgroup, workgroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                            final ChannelPipeline pipeline = nioSocketChannel.pipeline();
//                            添加一个处理器
                            pipeline.addLast(new LRpcRequestByteToMessageDecoder());
                            pipeline.addLast(new LRpcServerHandler());
                            pipeline.addLast(new LRpcReposeMessageToByteEncoder());
                        }
                    });
//            以888端口启动

            ChannelFuture sync = serverBootstrap.bind(SERVICE_PORT).sync();
            sync.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
//            最终关闭
            bossgroup.shutdownGracefully();
            workgroup.shutdownGracefully();
        }

    }

    /**
     * 客户端的方法，订阅一个service
     */
    public LRpcBootstrap reference(ReferenceConfig<?> reference) {
        reference.setRegistry(this.registry);
        return this;
    }

    /**
     * 指定序列化的方式
     */
    public LRpcBootstrap serializa(String serializaType) {
        SERIALIZA_TYPE = serializaType;
//        判断是否支出该序列化方式
        Boolean issupport = SerializaFactory.isSupporType(serializaType);
        if (issupport) {
            return this;
        } else {
            throw new RuntimeException("不支持" + serializaType + "类型的序列化方式");
        }


    }

    public LRpcBootstrap compress(String compressType) {
        COMPRESS_TYPE = compressType;
        Boolean issupport = CompressFactory.isSupporType(compressType);
        if (issupport) {
            return this;
        } else {
            throw new RuntimeException("不支持" + compressType + "类型的序列化方式");
        }

    }


    /**
     * 自动负载均衡策略
     */
    public LRpcBootstrap loadBlance(LoadBalancer loadBalancer) {
        LOADBALANCER = loadBalancer;
        return this;
    }
}
