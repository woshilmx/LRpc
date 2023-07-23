package com.lmx.core.serialization.iml;

import com.lmx.core.serialization.Serializa;
import lombok.extern.slf4j.Slf4j;

import java.io.*;


/**
 * 使用jdk实现序列化
 */
@Slf4j
public class JdkSerializa implements Serializa {
    @Override
    public byte[] serializa(Object object) {
        if (object == null) {
            return null;
        }
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);
            objectOutputStream.writeObject(object);
            return out.toByteArray();
        } catch (IOException e) {
            log.info("对象{}序列化失败", object);
            throw new RuntimeException(e);
        }
    }


    @Override
    public <T> T deSerializa(byte[] bytes, Class<T> tClass) {
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

            ObjectInputStream objectInputStream;

            objectInputStream = new ObjectInputStream(byteArrayInputStream);
            Object body = objectInputStream.readObject();
            return (T) body;
        } catch (IOException | ClassNotFoundException e) {
            log.error("对象反序列化失败");
            throw new RuntimeException(e);
        }
    }
}
