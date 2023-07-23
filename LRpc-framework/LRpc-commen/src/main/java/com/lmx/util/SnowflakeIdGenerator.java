package com.lmx.util;

import java.util.concurrent.atomic.AtomicLong;

public class SnowflakeIdGenerator {

    private static final long START_TIMESTAMP = 1630435200000L; // 2021-09-01 00:00:00 GMT

    private static final long SEQUENCE_BITS = 12;
    private static final long MACHINE_ID_BITS = 10;
    private static final long DATA_CENTER_ID_BITS = 5;

    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);
    private static final long MAX_MACHINE_ID = ~(-1L << MACHINE_ID_BITS);
    private static final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_ID_BITS);

    private static final long DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + MACHINE_ID_BITS;
    private static final long MACHINE_ID_SHIFT = SEQUENCE_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + MACHINE_ID_BITS + DATA_CENTER_ID_BITS;

    private static final AtomicLong sequence = new AtomicLong(0);
    private static long lastTimestamp = -1;

    /**
     * 生成雪花ID
     *
     * @param dataCenterId 数据中心ID，取值范围为0到31
     * @param machineId    机器ID，取值范围为0到1023
     * @return 返回生成的雪花ID
     */
    public static synchronized long generateId(long dataCenterId, long machineId) {
        if (dataCenterId < 0 || dataCenterId > MAX_DATA_CENTER_ID) {
            throw new IllegalArgumentException("Data Center ID must be between 0 and " + MAX_DATA_CENTER_ID);
        }
        if (machineId < 0 || machineId > MAX_MACHINE_ID) {
            throw new IllegalArgumentException("Machine ID must be between 0 and " + MAX_MACHINE_ID);
        }

        long timestamp = System.currentTimeMillis();

        if (timestamp < lastTimestamp) {
            // 时钟回拨，尝试等待时钟追赶，或者抛出异常
            long offset = lastTimestamp - timestamp;
            if (offset <= 5) {
                try {
                    Thread.sleep(offset << 1);
                    timestamp = System.currentTimeMillis();
                    if (timestamp < lastTimestamp) {
                        throw new RuntimeException("Clock moved backwards. Unable to generate id.");
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException("Clock synchronization failed.", e);
                }
            } else {
                throw new RuntimeException("Clock moved backwards. Unable to generate id.");
            }
        }

        if (timestamp == lastTimestamp) {
            long sequenceValue = sequence.incrementAndGet() & MAX_SEQUENCE;
            if (sequenceValue == 0) {
                // 同一毫秒内序列号用尽，等待下一毫秒
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence.set(0);
        }

        lastTimestamp = timestamp;

        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT) |
                (dataCenterId << DATA_CENTER_ID_SHIFT) |
                (machineId << MACHINE_ID_SHIFT) |
                (sequence.get() & MAX_SEQUENCE);
    }

    // 等待下一毫秒，直到获得新的时间戳
    private static long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    // 示例用法
    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            long id = generateId(1, 1);
            System.out.println("Generated ID: " + id);
        }
    }
}
