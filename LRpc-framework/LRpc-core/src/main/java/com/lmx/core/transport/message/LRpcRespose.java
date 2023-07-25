package com.lmx.core.transport.message;


import lombok.*;

import java.io.Serializable;
import java.util.Date;

/**
 * 封装响应对象报文，
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class LRpcRespose implements Serializable {
    /**
     * 请求id
     */

    private long requestId;


    /**
     * 状态码
     * 1——成功  2——异常
     */
    private byte code;


    /**
     * 时间戳
     */
    private long timestamp;

    /**
     * 压缩类型
     */
    private byte compressType;


    /**
     * 序列化方式
     */
    private byte serializationType;

    /**
     * 返回的结果值
     */

    private Object body;
}
