package com.lmx.core.messageenum;

import lombok.Data;

public enum RequestTypeEnum {
    COMMEN_REQUEST((byte) 1, "普通请求"),
    HEART_BEAT_REQUEST((byte) 2, "心跳请求");

    private byte code;
    private String message;

    RequestTypeEnum(byte code, String message) {
        this.code = code;
        this.message = message;
    }

    public byte getCode() {
        return code;
    }

    public void setCode(byte code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
