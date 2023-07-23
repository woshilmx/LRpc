package com.lmx.core.messageenum;


import lombok.Data;

/**
 * 响应状态的变化
 */

public enum ResposeCode {
    CORRENT_CODE((byte) 1, "正确"),
    ERROR_CODE((byte) 2, "失败");

    private byte code;
    private String message;


    ResposeCode(byte code, String message) {
        this.code = code;
        this.message = message;
    }

    public byte getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
