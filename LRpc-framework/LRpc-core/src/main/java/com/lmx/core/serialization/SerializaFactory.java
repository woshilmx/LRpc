package com.lmx.core.serialization;

import com.lmx.core.configure.ObjectWapper;
import com.lmx.core.serialization.iml.HessianSerializer;
import com.lmx.core.serialization.iml.JdkSerializa;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 实现序列化的工场
 */
public class SerializaFactory {

    private static final ConcurrentHashMap<String, ObjectWapper<Serializa>> SERIALIZA_NAME_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Byte, ObjectWapper<Serializa>> SERIALIZA_CODE_CACHE = new ConcurrentHashMap<>();

    //   可以指定序列化的方式，根据 Serializatype   SerializatypeName ,加载序列化器
    static {
        JdkSerializa jdkSerializa = new JdkSerializa();
        ObjectWapper<Serializa> jdkSerializaWapper = new ObjectWapper<Serializa>((byte) 1, "jdk", jdkSerializa);
        SERIALIZA_CODE_CACHE.put((byte) 1, jdkSerializaWapper);
        SERIALIZA_NAME_CACHE.put("jdk", jdkSerializaWapper);

        HessianSerializer hessianSerializa = new HessianSerializer();
        SerializaWapper hessianSerializaWapper = new SerializaWapper((byte) 2, "hessian", jdkSerializa);
        SERIALIZA_CODE_CACHE.put((byte) 2, jdkSerializaWapper);
        SERIALIZA_NAME_CACHE.put("hessian", jdkSerializaWapper);
    }

    //    获取序列化器
    public static ObjectWapper<Serializa> getSerializa(byte serializaTypeCode) {
        return SERIALIZA_CODE_CACHE.get(serializaTypeCode);
    }


    //    通过序列化方式名称获取序列化器
    public static ObjectWapper<Serializa> getSerializa(String serializaTypeName) {
        serializaTypeName = serializaTypeName.trim().toLowerCase();// 去除两端空格，都转为小写字母
        return SERIALIZA_NAME_CACHE.get(serializaTypeName);
    }


    /**
     * 判断是否支持serializaType序列化方式
     */
    public static Boolean isSupporType(String serializaType) {

        final String s = serializaType.trim().toLowerCase(); // 去除两端空格，都转为小写字母

        return SERIALIZA_NAME_CACHE.containsKey(s);
    }

    public static void addFactory(Byte code, String name, ObjectWapper<Serializa> serializaObjectWapper) {
        SERIALIZA_CODE_CACHE.put(code, serializaObjectWapper);
        SERIALIZA_NAME_CACHE.put(name, serializaObjectWapper);
    }
}
