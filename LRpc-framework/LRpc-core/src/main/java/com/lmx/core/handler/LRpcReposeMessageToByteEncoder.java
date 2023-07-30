package com.lmx.core.handler;

import com.lmx.core.compress.Compress;
import com.lmx.core.compress.CompressFactory;
import com.lmx.core.serialization.Serializa;
import com.lmx.core.serialization.SerializaFactory;
import com.lmx.core.transport.message.LRpcRequest;
import com.lmx.core.transport.message.LRpcRespose;
import com.lmx.core.transport.message.MessageContant;
import com.lmx.core.transport.message.Payload;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * 设计一个报文格式
 * 协议名称————lrpc协议 3字节
 * 版本号——1 1字节
 * header length 2字节
 * full length 4字节（header length+body length）
 * 请求状态码1字节
 * 序列化类型 1字节
 * 压缩类型 1字节
 * 请求id 8字节
 * <p>
 * <p>
 * body 负载 payload
 * 将LRpcRequest转化为报文的处理器
 */

/**
 * 将响应对象编码为报文，服务端
 */
@Slf4j
public class LRpcReposeMessageToByteEncoder extends MessageToByteEncoder<LRpcRespose> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, LRpcRespose lRpcRespose, ByteBuf byteBuf) throws Exception {
//
        log.info("开始封装响应报文{}", lRpcRespose.toString());
        byteBuf.writeBytes(MessageContant.MOSHU_NAME);
        byteBuf.writeByte(MessageContant.VERSION);
        byteBuf.writeShort(MessageContant.HEADER_LENGTH);
        int i = byteBuf.writerIndex(); // 整体报文长度的起始位置
        byteBuf.writerIndex(i + MessageContant.FULL_FILED_LENGTH); // 从当前位置向后移动四个字节
        byteBuf.writeByte(lRpcRespose.getCode());
        byteBuf.writeLong(lRpcRespose.getTimestamp());
        byteBuf.writeByte(lRpcRespose.getSerializationType());
        byteBuf.writeByte(lRpcRespose.getCompressType());
        byteBuf.writeLong(lRpcRespose.getRequestId());

        int bodyLength = 0;
        if (lRpcRespose.getBody() != null) {
            Serializa serializa = SerializaFactory.getSerializa(lRpcRespose.getSerializationType()).getData();
            byte[] bytesByPayload = serializa.serializa(lRpcRespose.getBody()); // 获取载荷的字符
//        压缩
            log.info("准备获取压缩器");
            Compress compress = CompressFactory.getCompressWapper
                    (lRpcRespose.getCompressType()).
                    getData();
            log.info("获取压缩器");
            bytesByPayload = compress.compress(bytesByPayload); // 执行压缩
//        byte[] bytesByPayload = getBytesByPayload(lRpcRespose.getBody()); // 获取载荷的字符
            byteBuf.writeBytes(bytesByPayload);
            bodyLength = bytesByPayload.length;
        }

        int currentpostion = byteBuf.writerIndex(); // 保存当前位置
        byteBuf.writerIndex(i);
        byteBuf.writeInt(MessageContant.HEADER_LENGTH + bodyLength);
        log.info("请求头长度{},载荷长度{}", MessageContant.HEADER_LENGTH, bodyLength);
        byteBuf.writerIndex(currentpostion); // 写指针归为
        log.info("响应报文{}封装结束", lRpcRespose.toString());
    }

//    private byte[] getBytesByPayload(Object body) {
//
//
//        try {
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);
//            objectOutputStream.writeObject(body);
//            return out.toByteArray();
//
//
//        } catch (IOException e) {
//            log.info("响应报文封装失败");
//            throw new RuntimeException(e);
//        }
//
//    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close();
        throw new RuntimeException(cause);

    }
}
