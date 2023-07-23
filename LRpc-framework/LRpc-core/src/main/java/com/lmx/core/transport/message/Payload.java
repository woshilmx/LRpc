package com.lmx.core.transport.message;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 设置有关方法的一些内容
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Payload implements Serializable {
    /**
     * 接口名称
     */
    private String interfanceName;

    /**
     * 方法名称
     */
    private String methodName;

    /**
     * 参数类型
     */

    private Class<?>[] parameterType;

    /**
     * 参数值
     */
    private Object[] parameterValue;

    /**
     * 返回值类型
     */

    private Class<?> returnType;
}
