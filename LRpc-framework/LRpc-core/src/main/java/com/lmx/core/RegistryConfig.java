package com.lmx.core;


/**
 * 关于注册中心配置的选项
 * */
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
}
