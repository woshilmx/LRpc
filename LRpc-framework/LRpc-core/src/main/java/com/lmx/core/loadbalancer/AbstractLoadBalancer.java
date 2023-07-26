package com.lmx.core.loadbalancer;

import com.lmx.core.LRpcBootstrap;
import com.lmx.core.configure.Configuration;
import com.lmx.core.discovery.Registry;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractLoadBalancer implements LoadBalancer {


//    private Registry registry;
    private List<InetSocketAddress> serviceIntaddress;

    public AbstractLoadBalancer() {
//        registry = LRpcBootstrap.getInstance().getRegistry();

    }


    @Override
    public InetSocketAddress getLoadBalance(String serviceName) {
       Registry registry = LRpcBootstrap.getInstance().getRegistry();
        List<InetSocketAddress> inetSocketAddresses = LRpcBootstrap.STRING_LIST_MAP.get(serviceName);
//        重新拉取列表
        if (inetSocketAddresses == null) {
            inetSocketAddresses = registry.lookup(serviceName); // 重新获取服务列表
//            LRpcBootstrap.STRING_LIST_MAP.put(serviceName, inetSocketAddresses);
        }
        if (inetSocketAddresses == null) {
            throw new RuntimeException(serviceName + "服务无可用节点");
        }

//        缓存可用服务列表，避免多次拉取耗费性能
//        if (this.serviceIntaddress == null || this.serviceIntaddress.isEmpty()) {
//            this.serviceIntaddress = registry.lookup(serviceName); // 返回了所有可用节点
//        }

//        获取selemtor
        Selector selector = getSelector(); // 单例

        return selector.getSelector(inetSocketAddresses);

    }

    /**
     * 是需要子类实现的方发
     */
    protected abstract Selector getSelector();


}
