package com.lmx.core;

import com.lmx.core.discovery.Registry;
import com.lmx.core.handler.MyClientHandler;
import com.lmx.core.netty.NettyBootstrapInitialization;
import com.lmx.core.proxy.Handler.NettyInvovationHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 客户端使用的方法
 */
@Slf4j
public class ReferenceConfig<T> {
    private Class<T> interfancecomsumer;

    //    注册中心的配置，通过这个类可以获取到注册中心
    private Registry registry;

    public Registry getRegistry() {
        return registry;
    }

    public void setInterface(Class<T> interfancecomsumer) {
        this.interfancecomsumer = interfancecomsumer;
    }

    /**
     * 获取一个代理对象
     */
    public T get() {
//      在这个类中发现可用的服务节点，与该节点建立连接，封装报文，发送报文，接收结果
        NettyInvovationHandler nettyInvovationHandler = new NettyInvovationHandler(interfancecomsumer);
        Object o = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{interfancecomsumer}, nettyInvovationHandler);
        return (T) o;
    }
    public void setRegistry(Registry registry) {
        this.registry = registry;
    }
}
