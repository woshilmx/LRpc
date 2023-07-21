package com.lmx.core;


/**
 * 关于服务配置的一些类
 */
public class ServiceConfig<T> {
    private Class<T> interfaceprovider;
    private Object ref;

    public void setInterface(Class<T> interfaceprovider) {
        this.interfaceprovider = interfaceprovider;
    }

    public Class<T> getInterface() {
        return this.interfaceprovider;
    }


    public Object getRef() {
        return ref;
    }

    public void setRef(Object ref) {
        this.ref = ref;
    }
}
