package com.lmx.core.compress;

import com.lmx.core.compress.iml.GzipCompress;
import com.lmx.core.serialization.SerializaWapper;
import com.lmx.core.serialization.iml.HessianSerializer;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 实现序列化的工场
 */
public class CompressFactory {

    private static final ConcurrentHashMap<String, CompressWapper> SERIALIZA_NAME_CACHE = new ConcurrentHashMap<String, CompressWapper>();
    private static final ConcurrentHashMap<Byte, CompressWapper> SERIALIZA_CODE_CACHE = new ConcurrentHashMap<Byte, CompressWapper>();

    //   可以指定序列化的方式，根据 Serializatype   SerializatypeName ,加载序列化器
    static {
        GzipCompress gzipCompress = new GzipCompress();
        CompressWapper gzipCompresswapper = new CompressWapper((byte) 1, "gzip", gzipCompress);
        SERIALIZA_CODE_CACHE.put((byte) 1, gzipCompresswapper);
        SERIALIZA_NAME_CACHE.put("gzip", gzipCompresswapper);

    }

    //    获取序列化器
    public static CompressWapper getCompressWapper(byte serializaTypeCode) {
        return SERIALIZA_CODE_CACHE.get(serializaTypeCode);
    }


    //    通过序列化方式名称获取序列化器
    public static CompressWapper getCompressWapper(String serializaTypeName) {
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
}
