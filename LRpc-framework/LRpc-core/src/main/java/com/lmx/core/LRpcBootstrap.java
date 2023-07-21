package com.lmx.core;


import com.lmx.model.Contanst;
import com.lmx.model.ZookeepNode;
import com.lmx.util.IpUtil;
import com.lmx.util.ZookeeperUtil;
import io.netty.handler.codec.http.HttpClientUpgradeHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;
import sun.security.krb5.internal.PAData;

import java.util.regex.MatchResult;

/**
 * 依赖启动类
 */
@Slf4j
public class LRpcBootstrap {
    private static final LRpcBootstrap lRpcBootstrap = new LRpcBootstrap();
    private String applicationName = "default";
    private RegistryConfig registryConfig;
    private ProtocolConfig protocolConfig;
    private ZooKeeper zooKeeper;

    private static int port = 8083;

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
//        创建一个注册中心的连接对象 ,后续我们将对他进行抽象，现在无法扩展
        zooKeeper = ZookeeperUtil.createZookeeper(Contanst.DEFAULT_CONNECTSTRING, Contanst.SESSIONTIMEOUT);
        log.info("connect " + registryConfig.getConnectString() + "success");
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
//        获取注册接口的全路径名称

        String serviceNme = service.getInterface().getName();

//     保存到providers节点下
        String path = Contanst.PROVIDER_PATH + serviceNme;

        ZookeepNode zookeepNode = new ZookeepNode(path, null, CreateMode.PERSISTENT);

        ZookeeperUtil.createNode(zooKeeper, zookeepNode, null);


//        保存临时节点,临时节点是ip:port


        String ipNodepath = path + "/" + IpUtil.getIp() + ":" + port;

//        保存临时节点
        ZookeepNode ipNode = new ZookeepNode(ipNodepath, null, CreateMode.EPHEMERAL);// 这个节点是临时节点
        ZookeeperUtil.createNode(zooKeeper, ipNode, null);

        log.info(service.getInterface().getName() + " publish success");
        return this;
    }


    /**
     * 启动引导类
     */
    public void start() {
        log.info("start success");
        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * 客户端的方法，订阅一个service
     */
    public LRpcBootstrap reference(ReferenceConfig<?> reference) {

        return this;
    }
}
