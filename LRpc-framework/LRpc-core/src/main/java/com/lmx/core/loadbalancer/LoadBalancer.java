package com.lmx.core.loadbalancer;


import java.net.InetSocketAddress;

/**
 * 负载均衡器，使用合适算法获取一个可用的服务节点
 */
public interface LoadBalancer {

    /**
     * 获取服务名称
     * */
    InetSocketAddress getLoadBalance(String serviceName);
}
