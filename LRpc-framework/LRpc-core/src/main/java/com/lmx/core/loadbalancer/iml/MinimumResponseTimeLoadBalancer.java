package com.lmx.core.loadbalancer.iml;

import com.lmx.core.LRpcBootstrap;
import com.lmx.core.loadbalancer.AbstractLoadBalancer;
import com.lmx.core.loadbalancer.Selector;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

/**
 * 最短响应时间负载均衡算法
 */
public class MinimumResponseTimeLoadBalancer extends AbstractLoadBalancer {

    @Override
    protected Selector getSelector() {
        return new MinimumResponseTimeSelector();
    }

    /**
     * 最短响应时间选择器类
     */
    private static class MinimumResponseTimeSelector implements Selector {

        @Override
        public InetSocketAddress getSelector(List<InetSocketAddress> serviceAddresses) {
            // 检查服务地址列表是否为空或null
            if (LRpcBootstrap.SERVICE_RESPOSE_TIME.isEmpty()) {
                // 如果为空，返回第一个
                return serviceAddresses.get(0);
            }

            // 初始化变量，用于记录最短的响应时间
            long minResponseTime = Long.MAX_VALUE;
            InetSocketAddress selectedAddress = null;

            // 遍历所有的服务地址，找到响应时间最短的地址
            for (InetSocketAddress address : serviceAddresses) {
                // 从服务响应时间Map中获取当前地址的响应时间
                Long responseTime = LRpcBootstrap.SERVICE_RESPOSE_TIME.get(address);
                if (responseTime != null && responseTime < minResponseTime) {
                    // 如果当前地址的响应时间比最小响应时间还短，更新选中的地址
                    minResponseTime = responseTime;
                    selectedAddress = address;
                }
            }

            return selectedAddress;
        }
    }
}
