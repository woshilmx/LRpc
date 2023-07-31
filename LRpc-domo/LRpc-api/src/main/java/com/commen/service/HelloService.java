package com.commen.service;


import com.lmx.annotation.Retry;

/**
 * 通用类
 */
public interface HelloService {
    @Retry(trycount = 2, timeout = 2000)
    public Student hello(String msg);
}
