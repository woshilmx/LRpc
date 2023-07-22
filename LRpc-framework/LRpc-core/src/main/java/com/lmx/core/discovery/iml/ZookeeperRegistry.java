package com.lmx.core.discovery.iml;


import com.lmx.core.LRpcBootstrap;
import com.lmx.core.ServiceConfig;
import com.lmx.core.discovery.Registry;
import com.lmx.model.Contanst;
import com.lmx.model.ZookeepNode;
import com.lmx.util.IpUtil;
import com.lmx.util.ZookeeperException;
import com.lmx.util.ZookeeperUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * zookeeper的注册实例
 */
@Slf4j
public class ZookeeperRegistry implements Registry {

    private ZooKeeper zooKeeper;
    private int port;
    private String host;
    int serviceyport = LRpcBootstrap.SERVICE_PORT; // service服务的端口

    public ZookeeperRegistry(String host, int port) {
        this.port = port;
        this.host = host;
        this.zooKeeper = ZookeeperUtil.createZookeeper(host + ":" + port, Contanst.SESSIONTIMEOUT);
    }

    @Override
    public void registry(ServiceConfig<?> service) {
        //        获取注册接口的全路径名称

        String serviceNme = service.getInterface().getName();

//     保存到providers节点下
        String path = Contanst.PROVIDER_PATH + serviceNme;

        ZookeepNode zookeepNode = new ZookeepNode(path, null, CreateMode.PERSISTENT);

        ZookeeperUtil.createNode(zooKeeper, zookeepNode, null);


//        保存临时节点,临时节点是ip:port


        String ipNodepath = path + "/" + IpUtil.getIp() + ":" + serviceyport; // 这个端口应该是netty的端口

//        保存临时节点
        ZookeepNode ipNode = new ZookeepNode(ipNodepath, null, CreateMode.EPHEMERAL);// 这个节点是临时节点
        ZookeeperUtil.createNode(zooKeeper, ipNode, null);

        log.info(service.getInterface().getName() + " publish success");
    }


    /**
     * 获取该服务子节点下的所有节点
     *
     * @param serviceName
     * @return
     */
    @Override
    public InetSocketAddress lookup(String serviceName) {

        String path = Contanst.PROVIDER_PATH + serviceName;

//        获取该服务下所有可用节点
        List<String> ipAndHost = ZookeeperUtil.getChildNode(zooKeeper, path, null);
        if (ipAndHost == null) {
            throw new RuntimeException("无可用服务");
        }

        List<InetSocketAddress> collect = ipAndHost.stream().map(ipstring -> {
            String[] split = ipstring.split(":");
            try {
                InetAddress host = InetAddress.getByName(split[0]);
                return new InetSocketAddress(host, Integer.parseInt(split[1]));
            } catch (UnknownHostException e) {
                throw new ZookeeperException(e.getMessage());
            }


        }).collect(Collectors.toList());
        if (collect.isEmpty()) {
            throw new ZookeeperException("暂无可用服务节点");
        }
//      TODO  进行节点的筛选，轮询，随机
        return collect.get(0);

    }
}
