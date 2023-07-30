package com.lmx.core.configure;

import com.lmx.core.serialization.Serializa;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ObjectWapper<T> {
    /**
     * 序列化器编号编号
     */
    private byte code;
    /**
     * 名称
     */
    private String name;
    /**
     * 序列化器
     */
    private T data;
}
