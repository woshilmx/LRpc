package com.lmx.consumer;

import com.lmx.core.LRpcBootstrap;
import com.lmx.core.ReferenceConfig;
import com.lmx.core.RegistryConfig;
import com.lmx.core.loadbalancer.iml.HashLoadBalancer;
import com.lmx.core.loadbalancer.iml.RoundLoadBalancer;
import com.lmx.service.HelloService;

public class ConsumerApplication {
    public static void main(String[] args) {
        // 定义所有的订阅
        ReferenceConfig<HelloService> reference = new ReferenceConfig<>();
        reference.setInterface(HelloService.class);

// 启动 Dubbo
        LRpcBootstrap.getInstance()
                .application("first-dubbo-consumer")
                .registry(new RegistryConfig("zookeeper://114.116.233.39:2181")) // 这一步已经连接完成
                .serializa("Hessian")
                .loadBlance(new HashLoadBalancer()) // 指定使用哪种策略实现负载均衡
                .compress("gzip")
                .reference(reference);  // 将上述步骤的RegistryConfig设置到reference中
//                .start();

// 获取订阅到的 Stub
        HelloService service = reference.get();
// 像普通的 java 接口一样调用
        for (int i = 0; i < 10; i++) {
            String message = service.hello("李满祥");
            System.out.println(message);
        }

    }
}
