package com.lmx.core.configure;


import com.lmx.core.LRpcBootstrap;
import com.lmx.core.ProtocolConfig;
import com.lmx.core.RegistryConfig;
import com.lmx.core.ServiceConfig;
import com.lmx.core.discovery.Registry;
import com.lmx.core.loadbalancer.LoadBalancer;
import com.lmx.core.loadbalancer.iml.RoundLoadBalancer;
import com.lmx.generator.IDGenerator;
import io.netty.channel.Channel;
import lombok.Data;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局的配置类
 */
@Data
public class Configuration {
    //    保存的是可用服务的响应时间
//    public static final Map<InetSocketAddress, Long> SERVICE_RESPOSE_TIME = new ConcurrentHashMap<>(16);
//    private static final LRpcBootstrap lRpcBootstrap = new LRpcBootstrap();
    /**
     * 应用的名称
     */
    private String applicationName = "default";
    /**
     * 注册中心的配置
     */
    private RegistryConfig registryConfig;

    /**
     * 协议的配置
     */
    private ProtocolConfig protocolConfig;


    //    生成全局的id生成器，单例
    public  final IDGenerator idGenerator = new IDGenerator(1, 2);

    /**
     * 端口
     */
    public   int SERVICE_PORT = 8747;
    /**
     * 序列化方式
     */
    public  String SERIALIZA_TYPE = "jdk"; // 序列化的方式,默认使用jdk
    /**
     * 压缩的方式
     */
    public  String COMPRESS_TYPE = "gzip"; // 压缩的方式，默认使用gzip
    /**
     * 负载均衡的方式
     */
    public  LoadBalancer LOADBALANCER = new RoundLoadBalancer(); // 默认
}
