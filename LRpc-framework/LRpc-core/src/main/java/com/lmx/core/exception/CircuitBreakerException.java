package com.lmx.core.exception;

public class CircuitBreakerException extends RuntimeException{
    public CircuitBreakerException() {
    }

    public CircuitBreakerException(String message) {
        super(message);
    }

    public CircuitBreakerException(Throwable cause) {
        super(cause);
    }
}
