package com.lmx.core.netty;


import com.lmx.core.handler.MyClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyBootstrapInitialization {
    private static Bootstrap bootstrap = new Bootstrap();

    static {
        //    初始化bootstrap
        NioEventLoopGroup group = new NioEventLoopGroup();
        // 设置相关参数
        bootstrap.group(group) // 设置线程组
                .channel(NioSocketChannel.class) // 设置客户端通道的实现类
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        ChannelPipeline pipeline = nioSocketChannel.pipeline();
                        pipeline.addLast(new MyClientHandler());
                    }
                });
    }

    //    返回bootstrap
    public static Bootstrap getBootstrap() {
        return bootstrap;
    }
}
