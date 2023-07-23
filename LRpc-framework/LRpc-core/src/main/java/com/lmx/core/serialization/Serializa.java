package com.lmx.core.serialization;

public interface Serializa {

    /**
     * 实现序列化
     */
    byte[] serializa(Object object);

    /**
     * 实现反序列化
     */
    <T> T deSerializa(byte[] bytes, Class<T> tClass);
}
