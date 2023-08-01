package com.lmx.core.exception;

public class ResposeException extends RuntimeException {

    private final int code;

    public ResposeException(int code, String message) {
        super("错误码" + code + ";错误原因" + message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
