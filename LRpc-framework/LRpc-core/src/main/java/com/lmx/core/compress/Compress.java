package com.lmx.core.compress;

public interface Compress {

    /**
     * 压缩
     * */
    byte[] compress(byte[] bytes);

    /**
     * 解压缩
     * */
    byte[] deCompress(byte[] bytes);
}
