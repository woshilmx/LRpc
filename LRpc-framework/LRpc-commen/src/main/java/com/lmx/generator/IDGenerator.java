package com.lmx.generator;


import com.lmx.util.SnowflakeIdGenerator;

/**
 * id生成器
 */
public class IDGenerator {

    /**
     * 数据中心id
     */
    private long dataCenterId;
    /**
     * 机器id
     */
    private long machineId;

    public IDGenerator(long dataCenterId, long machineId) {
        this.dataCenterId = dataCenterId;
        this.machineId = machineId;
    }

    /**
     * 获取id
     */
    public Long getId() {
        return SnowflakeIdGenerator.generateId(dataCenterId, machineId);
    }

}
