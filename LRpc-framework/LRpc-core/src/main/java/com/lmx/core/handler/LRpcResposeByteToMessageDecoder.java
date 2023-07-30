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
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;


/**
 * netty客户端解码的部分，将byte转为LRpcRespose 响应部分
 */
@Slf4j
public class LRpcResposeByteToMessageDecoder extends LengthFieldBasedFrameDecoder {
    public LRpcResposeByteToMessageDecoder() {
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
        byte code = in.readByte();
        long timestap = in.readLong();
        byte serializationType = in.readByte();
        byte compressType = in.readByte();
        long requestId = in.readLong();

        LRpcRespose lRpcRespose = new LRpcRespose();
        lRpcRespose.setRequestId(requestId);
        lRpcRespose.setCode(code);
        lRpcRespose.setTimestamp(timestap);
        lRpcRespose.setCompressType(compressType);
        lRpcRespose.setSerializationType(serializationType);


        //        普通请求解析载荷

//        try {
        if (fullLength - headerLength != 0) {
            byte[] payloadbyte = new byte[(int) (fullLength - headerLength)];

            in.readBytes(payloadbyte);

            //        TODO 解压缩
            final Compress compress = CompressFactory.getCompressWapper(lRpcRespose.getCompressType()).getData();
            payloadbyte = compress.deCompress(payloadbyte);
            //        TODO   反序列化,现在此处默认使用JDK提供的序列化方式
            final Serializa serializa = SerializaFactory.getSerializa(serializationType).getData();
            final Object body = serializa.deSerializa(payloadbyte, Object.class);

//            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(payloadbyte);
//
//            ObjectInputStream objectInputStream;
//
//            objectInputStream = new ObjectInputStream(byteArrayInputStream);
//            Object body = objectInputStream.readObject();
            lRpcRespose.setBody(body);
        }

//        } catch (IOException | ClassNotFoundException e) {
//            log.error("载荷解析失败");
//            throw new RuntimeException(e);
//        }
        return lRpcRespose;
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close();
        throw new RuntimeException(cause);

    }
}
