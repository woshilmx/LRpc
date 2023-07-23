package com.lmx.core.serialization.iml;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.lmx.core.serialization.Serializa;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class HessianSerializer implements Serializa {

    @Override
    public byte[] serializa(Object object) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Hessian2Output hessianOutput = new Hessian2Output(byteArrayOutputStream);

        try {
            hessianOutput.writeObject(object);
            hessianOutput.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new SerializationException("Failed to serialize object using Hessian.", e);
        } finally {
            if (hessianOutput != null) {
                try {
                    hessianOutput.close();
                } catch (IOException e) {
                    // Ignore close exception
                }
            }
        }
    }

    @Override
    public <T> T deSerializa(byte[] bytes, Class<T> tClass) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        Hessian2Input hessianInput = new Hessian2Input(byteArrayInputStream);

        try {
            Object obj = hessianInput.readObject(tClass);
            return tClass.cast(obj);
        } catch (IOException e) {
            throw new DeserializationException("Failed to deserialize object using Hessian.", e);
        } finally {
            if (hessianInput != null) {
                try {
                    hessianInput.close();
                } catch (IOException e) {
                    // Ignore close exception
                }
            }
        }
    }
}

// 自定义异常类
class SerializationException extends RuntimeException {
    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}

class DeserializationException extends RuntimeException {
    public DeserializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
