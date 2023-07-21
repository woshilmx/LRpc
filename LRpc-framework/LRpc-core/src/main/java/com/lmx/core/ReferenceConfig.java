package com.lmx.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ReferenceConfig<T> {
    private Class<T> interfancecomsumer;

    public void setInterface(Class<T> interfancecomsumer) {
        this.interfancecomsumer = interfancecomsumer;
    }

    /**
     * 获取一个代理对象
     */
    public T get() {

        Object o = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{interfancecomsumer}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                //TODO 我们先输出一句话，后续做修改
                System.out.println(method.getName() + "被调用了");
                return null;
            }
        });
        return (T) o;
    }
}
