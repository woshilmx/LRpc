package com.lmx.core.compress.iml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.lmx.core.compress.Compress;
import lombok.extern.slf4j.Slf4j;


/**
 * 实现了gzip的压缩方式
 */
@Slf4j
public class GzipCompress implements Compress {

    @Override
    public byte[] compress(byte[] bytes) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream);
            gzipOutputStream.write(bytes);
            gzipOutputStream.close();
            log.debug("Data compressed successfully.");
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("Error occurred during compression: {}", e.getMessage());
            return null; // or throw a custom exception
        }
    }

    @Override
    public byte[] deCompress(byte[] bytes) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipInputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }
            gzipInputStream.close();
            log.debug("Data decompressed successfully.");
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("Error occurred during decompression: {}", e.getMessage());
            return null; // or throw a custom exception
        }
    }
}
