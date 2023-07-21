package com.lmx.service.iml;

import com.lmx.service.HelloService;

public class HelloServiceIml implements HelloService {

    @Override
    public String hello(String msg) {

        return "hello,consumer:" + msg;

    }
}
