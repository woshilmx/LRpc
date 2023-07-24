package com.lmx.core.transport.message;


import lombok.*;

import java.io.Serializable;

/**
 * 封装报文，
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class LRpcRequest implements Serializable {
    /**
     * 请求id
     */

    private long requestId;

    /**
     * 请求类型
     */
    private byte requestType;

    /**
     * 压缩类型
     */
    private byte compressType;


    /**
     * 序列化方式
     */
    private byte serializationType;

    /**
     * 请求负载,告诉服务提供者，调用哪个方法；
     */

    private Payload payload;
}
