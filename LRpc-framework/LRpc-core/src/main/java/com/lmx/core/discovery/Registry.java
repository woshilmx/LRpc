package com.lmx.core.discovery;


import com.lmx.core.ServiceConfig;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 注册中心的通用接口
 * */
public interface Registry {
//    static Registry getRegistry(RegistryConfig registryConfig);

    /**
     * 注册
     * */
    public void registry(ServiceConfig<?> service);

    List<InetSocketAddress> lookup(String serviceName);

}
