package com.lmx.service.iml;

import com.commen.service.HelloService;
import com.commen.service.Student;
import com.lmx.core.annotation.Lrpc;

@Lrpc
public class HelloServiceIml implements HelloService {

    @Override
    public Student hello(String msg) {

        return new Student("hello,consumer:" + msg);

    }
}
