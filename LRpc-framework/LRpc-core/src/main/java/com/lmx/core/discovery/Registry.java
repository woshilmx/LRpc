package com.lmx.core.discovery;


import com.lmx.core.ServiceConfig;

import java.net.InetSocketAddress;

/**
 * 注册中心的通用接口
 * */
public interface Registry {
//    static Registry getRegistry(RegistryConfig registryConfig);

    /**
     * 注册
     * */
    public void registry(ServiceConfig<?> service);

    InetSocketAddress lookup(String serviceName);

}
