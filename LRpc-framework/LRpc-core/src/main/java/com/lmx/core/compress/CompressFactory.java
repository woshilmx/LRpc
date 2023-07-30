package com.lmx.core.compress;

import com.lmx.core.compress.iml.GzipCompress;
import com.lmx.core.configure.ObjectWapper;
import com.lmx.core.serialization.Serializa;
import com.lmx.core.serialization.SerializaWapper;
import com.lmx.core.serialization.iml.HessianSerializer;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 实现序列化的工场
 */
public class CompressFactory {

    private static final ConcurrentHashMap<String, ObjectWapper<Compress>> SERIALIZA_NAME_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Byte, ObjectWapper<Compress>> SERIALIZA_CODE_CACHE = new ConcurrentHashMap<>();

    //   可以指定序列化的方式，根据 Serializatype   SerializatypeName ,加载序列化器
    static {
        GzipCompress gzipCompress = new GzipCompress();
        ObjectWapper<Compress> gzipCompresswapper = new  ObjectWapper<Compress>((byte) 1, "gzip", gzipCompress);
        SERIALIZA_CODE_CACHE.put((byte) 1, gzipCompresswapper);
        SERIALIZA_NAME_CACHE.put("gzip", gzipCompresswapper);

    }

    //    获取序列化器
    public static  ObjectWapper<Compress> getCompressWapper(byte serializaTypeCode) {
        return SERIALIZA_CODE_CACHE.get(serializaTypeCode);
    }


    //    通过序列化方式名称获取序列化器
    public static  ObjectWapper<Compress> getCompressWapper(String serializaTypeName) {
        serializaTypeName = serializaTypeName.trim().toLowerCase();// 去除两端空格，都转为小写字母
        return SERIALIZA_NAME_CACHE.get(serializaTypeName);
    }


    /**
     * 判断是否支持serializaType序列化方式
     */
    public static Boolean isSupporType(String compressType) {

        final String s = compressType.trim().toLowerCase(); // 去除两端空格，都转为小写字母

        return SERIALIZA_NAME_CACHE.containsKey(s);

    }


    /**
     * 增加一个
     * */
    public static void addFactory(Byte code, String name, ObjectWapper<Compress> serializaObjectWapper) {
        SERIALIZA_CODE_CACHE.put(code, serializaObjectWapper);
        SERIALIZA_NAME_CACHE.put(name, serializaObjectWapper);
    }
}
