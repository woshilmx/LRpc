package com.lmx.core.compress;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CompressWapper {
    /**
     * 编号
     */
    private byte code;
    /**
     * 类型
     */
    private String type;
    /**
     * 压缩的实现类
     */
    private Compress compress;

    public CompressWapper(byte code, String type, Compress compress) {
        this.code = code;
        this.type = type;
        this.compress = compress;
    }
}
