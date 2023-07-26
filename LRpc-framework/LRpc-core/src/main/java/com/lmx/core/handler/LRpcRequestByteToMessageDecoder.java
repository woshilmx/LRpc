package com.lmx.core.handler;

import com.lmx.core.compress.Compress;
import com.lmx.core.compress.CompressFactory;
import com.lmx.core.messageenum.RequestTypeEnum;
import com.lmx.core.serialization.Serializa;
import com.lmx.core.serialization.SerializaFactory;
import com.lmx.core.serialization.SerializaWapper;
import com.lmx.core.transport.message.Payload;

import com.lmx.core.transport.message.LRpcRequest;
import com.lmx.core.transport.message.MessageContant;
import com.lmx.model.Contanst;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

/**
 * 解码器，服务端
 */
@Slf4j
public class LRpcRequestByteToMessageDecoder extends LengthFieldBasedFrameDecoder {
    public LRpcRequestByteToMessageDecoder() {
        super(MessageContant.MAX_FRAME_LENGTH,
                MessageContant.LENGHTH_FILED_OFFSET,
                MessageContant.FULL_FILED_LENGTH);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {

        return decodeLRpcReuqest(in);

    }

    /**
     * 从bytebuf中读取数据，然后封装成为LRpcRequest
     */
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
     * 将LRpcRequest转化为报文的处理器
     */
    private Object decodeLRpcReuqest(ByteBuf in) {
        byte[] bytes = new byte[MessageContant.MOSHU_NAME.length];
        in.readBytes(bytes);
//        判断协议是否一致
        for (int i = 0; i < MessageContant.MOSHU_NAME.length; i++) {
            if (bytes[i] != MessageContant.MOSHU_NAME[i]) {
                throw new RuntimeException("协议不一致");
            }
        }
//        读取版本号
        byte version = in.readByte();
        if (version > MessageContant.VERSION) {
            throw new RuntimeException("协议版本不支持");
        }
//        读取headerleneght
        short headerLength = in.readShort();
        long fullLength = in.readInt();
        log.info("请求头长度{},总长度{}", headerLength, fullLength);
        byte requestType = in.readByte();
        final long timestap = in.readLong();
        byte serializationType = in.readByte();
        byte compressType = in.readByte();
        long requestId = in.readLong();

        LRpcRequest lRpcRequest = new LRpcRequest();
        lRpcRequest.setRequestId(requestId);
        lRpcRequest.setRequestType(requestType);
        lRpcRequest.setCompressType(compressType);
        lRpcRequest.setTimestamp(timestap);
        lRpcRequest.setSerializationType(serializationType);


        // 如果是心跳请求
        if (requestType == RequestTypeEnum.HEART_BEAT_REQUEST.getCode()) {
            return lRpcRequest;
        }

//        普通请求解析载荷


        if (fullLength-headerLength!=0){
            byte[] payloadbyte = new byte[(int) (fullLength - headerLength)];

            in.readBytes(payloadbyte);
            //        TODO 解压缩
            final Compress compress = CompressFactory.getCompressWapper(lRpcRequest.getCompressType()).getCompress();
            payloadbyte = compress.deCompress(payloadbyte);
//          TODO   反序列化
            SerializaWapper serializaWapper = SerializaFactory.getSerializa(lRpcRequest.getSerializationType());
            Serializa serializa = serializaWapper.getSerializa();
            Payload payload = serializa.deSerializa(payloadbyte, Payload.class);
//
//            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(payloadbyte);
//
//            ObjectInputStream objectInputStream;
//
//            objectInputStream = new ObjectInputStream(byteArrayInputStream);
//            Payload payload = (Payload) objectInputStream.readObject();
            lRpcRequest.setPayload(payload);
            log.info("请求的数据为{}", lRpcRequest.toString());
        }
        return lRpcRequest;
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close();
        throw new RuntimeException(cause);

    }
}
