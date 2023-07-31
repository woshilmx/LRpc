package com.lmx.core.discovery.iml;


import com.lmx.core.LRpcBootstrap;
import com.lmx.core.ServiceConfig;
import com.lmx.core.discovery.Registry;
import com.lmx.model.Contanst;
import com.lmx.model.ZookeepNode;
import com.lmx.util.IpUtil;
import com.lmx.util.ZookeeperException;
import com.lmx.util.ZookeeperUtil;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * zookeeper的注册实例
 */
@Slf4j
public class ZookeeperRegistry implements Registry {

    private ZooKeeper zooKeeper;
    private int port;
    private String host;
    int serviceyport = LRpcBootstrap.getInstance().getConfiguration().getSERVICE_PORT();// service服务的端口

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

//       创建组名
        String groupPath = path + "/" + service.getGroup();
        ZookeepNode groupNode = new ZookeepNode(groupPath, null, CreateMode.PERSISTENT);
        ZookeeperUtil.createNode(zooKeeper, groupNode, null);
//        保存临时节点,临时节点是ip:port


        String ipNodepath = groupPath + "/" + IpUtil.getIp() + ":" + serviceyport; // 这个端口应该是netty的端口

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
    public List<InetSocketAddress> lookup(String serviceName) {
//        final Registry registry = LRpcBootstrap.getInstance().getRegistry();
        final List<InetSocketAddress> inetSocketAddresses = LRpcBootstrap.STRING_LIST_MAP.get(serviceName);
        if (inetSocketAddresses != null && !inetSocketAddresses.isEmpty()) {
            return inetSocketAddresses;
        }
//   否则重新拉取
        log.info("正在拉取{}服务列表", serviceName);
        return pullServiceNode(serviceName);
    }


    /**
     * 拉取服务节点的动作
     */
    private List<InetSocketAddress> pullServiceNode(String serviceName) {
        String group = LRpcBootstrap.getInstance().getConfiguration().getGroup(); // 增加流量分组
        String path = Contanst.PROVIDER_PATH + serviceName + "/" + group;
//        获取该服务下所有可用节点
        List<String> ipAndHost = ZookeeperUtil.getChildNode(zooKeeper, path, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {


                if (watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
                    log.info("触发子节点修改事件");
                    List<InetSocketAddress> updatedNodes = pullServiceNode(serviceName);
                    List<InetSocketAddress> oldList = LRpcBootstrap.STRING_LIST_MAP.get(serviceName);

//                    服务上线，updatedNodes不在channelcatch中

//                    服务下线，在canch中，不在旧的list中
                    for (InetSocketAddress address : updatedNodes) {
                        if (!oldList.contains(address) && LRpcBootstrap.SERVER_CHANNEL_CACHE.containsKey(address)) {
                            LRpcBootstrap.SERVER_CHANNEL_CACHE.remove(address);
                        }
                    }
////                    获取原有服务列表
//                    List<InetSocketAddress> inetSocketAddresses = LRpcBootstrap.STRING_LIST_MAP.get(serviceName);
//                    //                重新拉取服务节点
//                    List<InetSocketAddress> updatedNodes = pullServiceNode(serviceName);
////                    获取最新的服务列表
//                    List<InetSocketAddress> newServiceList = LRpcBootstrap.STRING_LIST_MAP.get(serviceName);
//
//                    if (inetSocketAddresses != null && !inetSocketAddresses.isEmpty()) {
//                        if (newServiceList==null || newServiceList.isEmpty()){
//                            for (InetSocketAddress inetSocketAddress : inetSocketAddresses) {
//                                LRpcBootstrap.SERVER_CHANNEL_CACHE.remove(inetSocketAddress);
//                            }
//                            return;
//                        }
//                        if (inetSocketAddresses.size() > newServiceList.size() ) {
////                            说明服务下线了
//                            List<InetSocketAddress> collect = inetSocketAddresses.stream().filter(inetSocketAddress -> {
//                                return !newServiceList.contains(inetSocketAddress);
//                            }).collect(Collectors.toList());
////                            移出通道列表中的连接器
//                            for (InetSocketAddress inetSocketAddress : collect) {
//                                LRpcBootstrap.SERVER_CHANNEL_CACHE.remove(inetSocketAddress);
//                            }
//                        }
//                    }
//                    log.info("现有服务列表是{}",LRpcBootstrap.STRING_LIST_MAP);
                }

//                updateServerChannelCache(serviceName, updatedNodes);
//                监听服务上下线，上线时，更新channel，下线时移出channel
//                final Map<InetSocketAddress, Channel> serverChannelCache = LRpcBootstrap.SERVER_CHANNEL_CACHE;
            }
        });

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
        LRpcBootstrap.STRING_LIST_MAP.put(serviceName, collect); // 缓存不同接口的服务列表，不用每次都连接
        return collect;
    }


}
