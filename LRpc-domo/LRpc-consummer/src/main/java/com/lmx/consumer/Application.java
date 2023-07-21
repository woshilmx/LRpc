package com.lmx.consumer;

import com.lmx.core.LRpcBootstrap;
import com.lmx.core.ReferenceConfig;
import com.lmx.core.RegistryConfig;
import com.lmx.service.HelloService;

public class Application {
    public static void main(String[] args) {
        // 定义所有的订阅
        ReferenceConfig<HelloService> reference = new ReferenceConfig<>();
        reference.setInterface(HelloService.class);

// 启动 Dubbo
        LRpcBootstrap.getInstance()
                .application("first-dubbo-consumer")
                .registry(new RegistryConfig("zookeeper://114.116.233.39:2181"))
                .reference(reference)
                .start();

// 获取订阅到的 Stub
        HelloService service = reference.get();
// 像普通的 java 接口一样调用
        String message = service.hello("lRpc");
    }
}
