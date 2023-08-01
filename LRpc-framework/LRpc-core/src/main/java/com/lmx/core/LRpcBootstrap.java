package com.lmx.core;


import com.lmx.annotation.Lrpc;
import com.lmx.core.compress.CompressFactory;
import com.lmx.core.configure.Configuration;
import com.lmx.core.discovery.Registry;
import com.lmx.core.handler.LRpcRequestByteToMessageDecoder;
import com.lmx.core.handler.LRpcReposeMessageToByteEncoder;
import com.lmx.core.handler.LRpcServerHandler;
import com.lmx.core.heartbeat.HeartbeatDetection;
import com.lmx.core.loadbalancer.LoadBalancer;
import com.lmx.core.serialization.SerializaFactory;
import com.lmx.core.shudown.LrpcShutdoemHooh;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.ZooKeeper;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 依赖启动类
 */
@Slf4j
@Data
public class LRpcBootstrap {
    //    保存的是可用服务的响应时间
    public static final Map<InetSocketAddress, Long> SERVICE_RESPOSE_TIME = new ConcurrentHashMap<>(16);
    private static final LRpcBootstrap lRpcBootstrap = new LRpcBootstrap();
//    private String applicationName = "default";
//    private RegistryConfig registryConfig;

    //    private ProtocolConfig protocolConfig;
    private ZooKeeper zooKeeper;
    //    设置服务消费者与nettychannel通道缓存，避免每次调用方法重新连接
    public static final Map<InetSocketAddress, Channel> SERVER_CHANNEL_CACHE = new ConcurrentHashMap<>(16);
    // 保存的是服务提供者发布的接口的实现类
    public static final Map<String, ServiceConfig<?>> serviceConfigMap = new ConcurrentHashMap<>(16);
    //  阻塞的CompletableFuture，
    public static final Map<Long, CompletableFuture<Object>> PEDDING_Future = new ConcurrentHashMap<>(16);

    //    生成全局的id生成器，单例
//    public static final IDGenerator idGenerator = new IDGenerator(1, 2);

    //    保存可用服务的列表
    public static final Map<String, List<InetSocketAddress>> STRING_LIST_MAP = new ConcurrentHashMap<>(16);
    /**
     * 注册中心
     */
    private Registry registry;

//    public final static int SERVICE_PORT = 8744;
//    public static String SERIALIZA_TYPE = "jdk"; // 序列化的方式,默认使用jdk
//    public static String COMPRESS_TYPE = "gzip"; // 压缩的方式，默认使用gzip
//    public static LoadBalancer LOADBALANCER = new RoundLoadBalancer(); // 默认使用轮询的负载均衡策略

    private Configuration configuration;

    public LRpcBootstrap() {
//        执行引导类初始化的工作
        configuration = new Configuration();
//        Arrays.sort();
    }

    /**
     * 使用单例模式，获取唯一的引导类对象
     */
    public static LRpcBootstrap getInstance() {
        return lRpcBootstrap;

    }

    /**
     * 执行应用的名称
     */
    public LRpcBootstrap application(String applicationName) {
        configuration.setApplicationName(applicationName);
        return this;

    }

    /**
     * 注册到注册中心中
     */
    public LRpcBootstrap registry(RegistryConfig registryConfig) {
//        registryConfig.getRegistry();
//        registryConfig
        configuration.setRegistryConfig(registryConfig);
//        this.registryConfig = registryConfig;
        this.registry = registryConfig.getRegistry();


////        创建一个注册中心的连接对象 ,后续我们将对他进行抽象，现在无法扩展
//        zooKeeper = ZookeeperUtil.createZookeeper(Contanst.DEFAULT_CONNECTSTRING, Contanst.SESSIONTIMEOUT);
//        log.info("connect " + registryConfig.getConnectString() + "success");
        return this;
    }


    /**
     * 配置序列化协议
     */
    public LRpcBootstrap protocol(ProtocolConfig protocolConfig) {
        configuration.setProtocolConfig(protocolConfig);
//        this.protocolConfig = protocolConfig;
        return this;
    }


    /**
     * 服务提供者将服务发布到注册中心中
     */
    public LRpcBootstrap service(ServiceConfig<?> service) {
        configuration.getRegistryConfig().getRegistry().registry(service);
//        当服务注册完成后，需要保存当前接口的具体实现，不需要再次创建该实例对象
        serviceConfigMap.put(service.getInterface().getName(), service);
        return this;
    }


    /**
     * 启动服务端引导类
     */
    public void start() {

//        实现优雅停机代码
        Runtime.getRuntime().addShutdownHook(new LrpcShutdoemHooh());


//        启动netty
        NioEventLoopGroup bossgroup = new NioEventLoopGroup();
        NioEventLoopGroup workgroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossgroup, workgroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                            final ChannelPipeline pipeline = nioSocketChannel.pipeline();
//                            添加一个处理器
                            pipeline.addLast(new LRpcRequestByteToMessageDecoder());
                            pipeline.addLast(new LRpcServerHandler());
                            pipeline.addLast(new LRpcReposeMessageToByteEncoder());
                        }
                    });
//            以888端口启动

            ChannelFuture sync = serverBootstrap.bind(configuration.getSERVICE_PORT()).sync();
            sync.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
//            最终关闭
            bossgroup.shutdownGracefully();
            workgroup.shutdownGracefully();
        }

    }

    /**
     * 客户端的方法，订阅一个service
     */
    public LRpcBootstrap reference(ReferenceConfig<?> reference) {
        reference.setRegistry(registry); // 只有个
        log.info("配置项为:{}", configuration.toString());
//        进行心跳检测
        HeartbeatDetection.startHeartbeatTask(reference.getInterfancecomsumer().getName());
        return this;
    }

    /**
     * 指定序列化的方式
     */
    public LRpcBootstrap serializa(String serializaType) {
        configuration.
                SERIALIZA_TYPE = serializaType;
//        判断是否支出该序列化方式
        Boolean issupport = SerializaFactory.isSupporType(serializaType);
        if (issupport) {
            return this;
        } else {
            throw new RuntimeException("不支持" + serializaType + "类型的序列化方式");
        }


    }

    public LRpcBootstrap compress(String compressType) {
        configuration.setCOMPRESS_TYPE(compressType);
//        COMPRESS_TYPE = compressType;
        Boolean issupport = CompressFactory.isSupporType(compressType);
        if (issupport) {
            return this;
        } else {
            throw new RuntimeException("不支持" + compressType + "类型的序列化方式");
        }

    }


    /**
     * 自动负载均衡策略
     */
    public LRpcBootstrap loadBlance(LoadBalancer loadBalancer) {
        configuration.setLOADBALANCER(loadBalancer);
//        LOADBALANCER = loadBalancer;
        return this;
    }


    /**
     * 扫描包
     */
    public LRpcBootstrap scanService(String packageName) {
        String packagePath = packageName.replace('.', '/');
//        System.out.println(packagePath);
        URL resource = ClassLoader.getSystemClassLoader().getResource(packagePath);
//        System.o/ut.println(resource);

        if (resource == null) {
            throw new RuntimeException("包扫描出现错误，请确保您的路径正确");
        }
//        获取该文件夹下所有的接口的全限定名
        ArrayList<String> classNameList = new ArrayList<>();


        getFileofInterfnce(packageName, new File(resource.getPath()), classNameList);

        if (classNameList.isEmpty()) {
            throw new RuntimeException("该包下不存在类");
        }

        getInterfanceLrpc(classNameList);


        System.out.println(classNameList);
//        System.out.println(classLoader);

        return this;
    }


    /**
     * 根据类名创建对象，进行发布
     */
    private void getInterfanceLrpc(ArrayList<String> classNameList) {
        //        获取包含Lrpc注解的类
        List<? extends Class<?>> aclassList = classNameList.stream().map(classNmage -> {
            try {
                return Class.forName(classNmage);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }).filter(aClass -> {

            if (aClass != null) {
                return aClass.isAnnotationPresent(Lrpc.class);
            }

            return false;
        }).collect(Collectors.toList());
//
        if (aclassList.isEmpty()) {
            throw new RuntimeException("暂无需要发布的类");
        }

        for (Class c : aclassList) {

            try {
                Lrpc annotation = (Lrpc) c.getAnnotation(Lrpc.class);
                final String group = annotation.group();
                final Object o = c.getConstructor().newInstance();
//                service.setRef(o);

                final Class[] interfaces = c.getInterfaces();

                for (Class interfance : interfaces) {
                    ServiceConfig<Object> service = new ServiceConfig<>();
                    service.setRef(o);
                    service.setInterface(interfance);
                    service.setGroup(group);
//                    发布接口
                    service(service);
                }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
//            service(service);
        }
    }


    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * 获取包下面所有类的全限定名
     */
    private void getFileofInterfnce(String packageName, File file, ArrayList<String> classNameList) {
//        if
//        final File file = new File(path);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                getFileofInterfnce(packageName, f, classNameList);
            }
            return;
        }
//        如果是class类型的文件
        if (file.getName().endsWith(".class")) {
            try {
//                final String absolutePath = file.getAbsolutePath();
                String path = file.getPath();
                String replace = packageName.replace(".", File.separator);
                String substring = path.substring(path.lastIndexOf(replace));
//                System.out.println(path);
                substring = substring.replace(".class", "").replace(File.separator, ".");
//                System.out.println(substring);
//                Class<?> aClass = Class.forName();
//                if (aClass.isInterface()) {
                classNameList.add(substring);
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    public static void main(String[] args) {
        LRpcBootstrap.getInstance().scanService("com.lmx.core");
    }

    public LRpcBootstrap group(String group) {

        configuration.setGroup(group);
        return this;
    }
}
