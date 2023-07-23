package com.lmx.core.serialization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SerializaWapper {
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
    private Serializa serializa;
}
