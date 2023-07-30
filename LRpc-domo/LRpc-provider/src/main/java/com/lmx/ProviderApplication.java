package com.lmx;

import com.lmx.core.LRpcBootstrap;
import com.lmx.core.ProtocolConfig;
import com.lmx.core.RegistryConfig;
import com.lmx.core.ServiceConfig;
import com.commen.service.HelloService;
import com.lmx.service.iml.HelloServiceIml;

/**
 * 服务提供方的主启动类
 * */
public class ProviderApplication {
    public static void main(String[] args) {
        // 1-basic/dubbo-samples-api/src/main/java/org/apache/dubbo/samples/provider/Application.java

// 定义所有的服务
        ServiceConfig<HelloService> service = new ServiceConfig<>();
        service.setInterface(HelloService.class);
        service.setRef(new HelloServiceIml());

// 启动 Dubbo
        LRpcBootstrap.getInstance()
                .application("first-Lrpc-provider")
//                .registry(new RegistryConfig("zookeeper://114.116.233.39:2181"))
                .protocol(new ProtocolConfig("Lrpc", -1))
//                .service(service)
                .scanService("com.lmx")
                .start();
    }

}
