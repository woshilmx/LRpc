package com.lmx.core.transport.message;

import java.nio.charset.StandardCharsets;


/**
 * * 设计一个报文格式
 * * 协议名称————lrpc协议 3字节
 * * 版本号——1 1字节
 * * header length 2字节
 * * full length 4字节（header length+body length）
 * * 请求类型1字节
 * * 序列化类型 1字节
 * * 压缩类型 1字节
 * * 请求id 8字节
 * * <p>
 * * <p>
 * * body 负载 payload
 */
public class MessageContant {
    public static final byte[] MOSHU_NAME = "lrpc".getBytes(StandardCharsets.UTF_8);
    public static final byte VERSION = 1;
    public static final short HEADER_LENGTH = (short) (MOSHU_NAME.length + 1 + 2 + 4 + 1 + 1 + 1 + 8);
    public static final int MAX_FRAME_LENGTH = 1024*1024; // 最大帧长度
    public static final int FULL_FILED_LENGTH = 4; // 总长度占4个字节
    private static final int VERSION_LENGTH = 1; // 版本号长度
    private static final int HEADER_FILED_LENGTH = 2;  // header长度两个字节
    public static final int LENGHTH_FILED_OFFSET = MOSHU_NAME.length+VERSION_LENGTH+HEADER_FILED_LENGTH;  // 总长度偏移量
}
