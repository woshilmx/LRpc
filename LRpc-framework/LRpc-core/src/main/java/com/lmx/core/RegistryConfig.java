package com.lmx.core;


import com.lmx.core.discovery.Registry;
import com.lmx.core.discovery.iml.ZookeeperRegistry;
import lombok.ToString;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * 关于注册中心配置的选项
 */
@ToString
public class RegistryConfig {
    private String connectString;

    public RegistryConfig(String connectString) {
        this.connectString = connectString;
    }

    public String getConnectString() {
        return connectString;
    }

    public void setConnectString(String connectString) {
        this.connectString = connectString;
    }


    /**
     * 获取注册中心
     */
    public Registry getRegistry() {
//        清除空格，字母转化为小写
        String registryType = getRegistryType(connectString).trim().toLowerCase();
        String host = getHost(connectString);
        int port = getPort(connectString);
        if (registryType.equals("zookeeper")) {
            return new ZookeeperRegistry(host, port);
        }

        return null;
    }

    /**
     * 获取主机
     */
    private String getHost(String connectString) {

        try {
            URI uri = null;
            uri = new URI(connectString);
            return uri.getHost();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException("注册中心地址异常");
        }


    }

    /**
     * 获取端口
     */
    private int getPort(String connectString) {

        try {
            URI uri = null;
            uri = new URI(connectString);
            return uri.getPort();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException("注册中心地址异常");
        }
    }


    /**
     * 获取注册中心类型 zookeeper
     */
    private String getRegistryType(String connectString) {
        try {
            URI uri = null;
            uri = new URI(connectString);
            return uri.getScheme();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException("注册中心地址异常");
        }

    }

}
