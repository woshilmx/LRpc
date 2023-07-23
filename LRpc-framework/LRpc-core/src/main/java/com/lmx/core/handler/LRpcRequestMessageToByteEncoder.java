package com.lmx.core.handler;

import com.lmx.core.serialization.Serializa;
import com.lmx.core.serialization.SerializaFactory;
import com.lmx.core.transport.message.LRpcRequest;
import com.lmx.core.transport.message.MessageContant;
import com.lmx.core.transport.message.Payload;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * 设计一个报文格式
 * 协议名称————lrpc协议 3字节
 * 版本号——1 1字节
 * header length 2字节
 * full length 4字节（header length+body length）
 * 请求类型1字节
 * 序列化类型 1字节
 * 压缩类型 1字节
 * 请求id 8字节
 * <p>
 * <p>
 * body 负载 payload
 * 将LRpcRequest转化为报文的处理器 客户端
 */
@Slf4j
public class LRpcRequestMessageToByteEncoder extends MessageToByteEncoder<LRpcRequest> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, LRpcRequest lRpcRequest, ByteBuf byteBuf) throws Exception {
//
        byteBuf.writeBytes(MessageContant.MOSHU_NAME);
        byteBuf.writeByte(MessageContant.VERSION);
        byteBuf.writeShort(MessageContant.HEADER_LENGTH);
        int i = byteBuf.writerIndex(); // 整体报文长度的起始位置
        byteBuf.writerIndex(i + MessageContant.FULL_FILED_LENGTH); // 从当前位置向后移动四个字节
        byteBuf.writeByte(lRpcRequest.getRequestType());
        byteBuf.writeByte(lRpcRequest.getSerializationType());
        byteBuf.writeByte(lRpcRequest.getCompressType());
        byteBuf.writeLong(lRpcRequest.getRequestId());
//        序列化
        Serializa serializa = SerializaFactory.getSerializa(lRpcRequest.getSerializationType()).getSerializa();
        byte[] bytesByPayload = serializa.serializa(lRpcRequest.getPayload());
//        byte[] bytesByPayload = getBytesByPayload(); // 获取载荷的字符
        byteBuf.writeBytes(bytesByPayload);
        int currentpostion = byteBuf.writerIndex(); // 保存当前位置
        byteBuf.writerIndex(i);
        byteBuf.writeInt(MessageContant.HEADER_LENGTH + bytesByPayload.length);
        log.info("请求头长度{},载荷长度{}", MessageContant.HEADER_LENGTH, bytesByPayload.length);
        byteBuf.writerIndex(currentpostion); // 写指针归为

    }

//    private byte[] getBytesByPayload(Payload payload) {
//
//
//        try {
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);
//            objectOutputStream.writeObject(payload);
//            return out.toByteArray();
//
//
//        } catch (IOException e) {
//            log.info("报文封装失败");
//            throw new RuntimeException(e);
//        }
//
//    }
}
