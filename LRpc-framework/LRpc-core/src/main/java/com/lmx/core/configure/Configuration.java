package com.lmx.core.configure;


import com.lmx.core.LRpcBootstrap;
import com.lmx.core.ProtocolConfig;
import com.lmx.core.RegistryConfig;
import com.lmx.core.loadbalancer.LoadBalancer;
import com.lmx.core.loadbalancer.iml.RoundLoadBalancer;
import com.lmx.core.protection.CircuitBreaker;
import com.lmx.core.protection.TokenBucketRateLimter;
import com.lmx.generator.IDGenerator;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局的配置类  先走代码-再走xml-最后默认
 */
@Data
public class Configuration {
    //    保存的是可用服务的响应时间
//    public static final Map<InetSocketAddress, Long> SERVICE_RESPOSE_TIME = new ConcurrentHashMap<>(16);
//    private static final LRpcBootstrap lRpcBootstrap = new LRpcBootstrap();
    /**
     * 应用的名称
     */
    public String applicationName = "default";
    /**
     * 注册中心的配置
     */
    public RegistryConfig registryConfig;

    /**
     * 协议的配置
     */
    public ProtocolConfig protocolConfig;


    //    生成全局的id生成器，单例
    public IDGenerator idGenerator = new IDGenerator(1, 2);

    /**
     * 端口
     */
    public int SERVICE_PORT = 8747;
    /**
     * 序列化方式
     */
    public String SERIALIZA_TYPE = "jdk"; // 序列化的方式,默认使用jdk
    /**
     * 压缩的方式
     */
    public String COMPRESS_TYPE = "gzip"; // 压缩的方式，默认使用gzip
    /**
     * 负载均衡的方式
     */
    public LoadBalancer LOADBALANCER = new RoundLoadBalancer(); // 默认
    /**
     * 针对不同ip的限流器
     */
    public Map<SocketAddress, TokenBucketRateLimter> rate_ip_limter = new ConcurrentHashMap<>(16);
    /**
     * 客户端使用的熔断器
     */
    public Map<InetSocketAddress, CircuitBreaker> CircuitBreaker_Ip_Map = new ConcurrentHashMap<>(16);
    private String group = "default";

    //    读取xml文件
    public Configuration() {

//       1. 解析spi
        final Spiresolver spiresolver = new Spiresolver();
        spiresolver.loadFromSpi(this);

//        2.解析xml
        XmlResoulver xmlResoulver = new XmlResoulver();
        xmlResoulver.loadFromXml(this);

    }


    public static void main(String[] args) {
        final Configuration configuration = new Configuration();
    }


    public void setGroup(String group) {
        this.group = group;
    }

    public String getGroup() {
        return group;
    }
}
