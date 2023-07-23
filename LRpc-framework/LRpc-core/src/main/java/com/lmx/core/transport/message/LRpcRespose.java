package com.lmx.core.transport.message;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 封装响应对象报文，
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LRpcRespose implements Serializable {
    /**
     * 请求id
     */

    private long requestId;



    /**
     * 状态码
     * 1——成功  2——异常
     * */
    private byte code;

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
