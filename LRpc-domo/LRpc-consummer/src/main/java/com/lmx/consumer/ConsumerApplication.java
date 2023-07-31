package com.lmx.consumer;

import com.lmx.core.LRpcBootstrap;
import com.lmx.core.ReferenceConfig;
import com.lmx.core.RegistryConfig;
import com.lmx.core.loadbalancer.iml.MinimumResponseTimeLoadBalancer;
import com.commen.service.HelloService;
import com.commen.service.Student;
import com.lmx.core.loadbalancer.iml.RoundLoadBalancer;

import java.io.IOException;

public class ConsumerApplication {
    public static void main(String[] args) throws IOException {
        // 定义所有的订阅
        ReferenceConfig<HelloService> reference = new ReferenceConfig<>();
        reference.setInterface(HelloService.class);

// 启动 Dubbo
        LRpcBootstrap.getInstance()
//                .application("first-dubbo-consumer")
//                .registry(new RegistryConfig("zookeeper://114.116.233.39:2181")) // 这一步已经连接完成
//                .serializa("Hessian")
//                .serializa("jdk")
//                .loadBlance(new RoundLoadBalancer()) // 指定使用哪种策略实现负载均衡
//                .compress("gzip")
//                .group("hello")
                .reference(reference);  // 将上述步骤的RegistryConfig设置到reference中
//                .start();

// 获取订阅到的 Stub
        HelloService service = reference.get();
//// 像普通的 java 接口一样调用
        int lmx=0;
//        for (int i = 0; i < 1000; i++) {
//            Student message = service.hello("李满祥");
//           if (message!=null && message.getName().contains("李满祥")){
//               lmx++;
//               System.out.println(message.toString());
//           }
//
//        }
//
//
//
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        for (int i = 0; i < 1000; i++) {
            Student message = service.hello("李满祥");
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (message!=null && message.getName().contains("李满祥")){
                lmx++;
                System.out.println(message.toString());
            }

        }
        System.out.println(lmx);
    }
}
