package com.lmx.annotation;

import java.lang.annotation.*;


/**
 * 标记哪些类是需要被注册的
 * */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Lrpc {
    String group() default "default";
}
