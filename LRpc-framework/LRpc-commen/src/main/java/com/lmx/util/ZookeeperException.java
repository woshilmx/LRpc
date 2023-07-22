package com.lmx.util;

public class ZookeeperException extends RuntimeException  {
    private String message;

    public ZookeeperException(String message) {
        super(message);
        this.message = message;

    }
}
