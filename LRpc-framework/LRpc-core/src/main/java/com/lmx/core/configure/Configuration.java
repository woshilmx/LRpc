package com.lmx.core.configure;


import com.lmx.core.LRpcBootstrap;
import com.lmx.core.ProtocolConfig;
import com.lmx.core.RegistryConfig;
import com.lmx.core.loadbalancer.LoadBalancer;
import com.lmx.core.loadbalancer.iml.RoundLoadBalancer;
import com.lmx.generator.IDGenerator;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.net.URL;

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
    private String applicationName = "default";
    /**
     * 注册中心的配置
     */
    private RegistryConfig registryConfig;

    /**
     * 协议的配置
     */
    private ProtocolConfig protocolConfig;


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

    //    读取xml文件
    public Configuration() {
        try {
            // 1. 创建一个 DocumentBuilderFactory 对象
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

//            关闭ddt校验
            factory.setValidating(false);
            // 2. 创建一个 DocumentBuilder 对象
            DocumentBuilder builder = factory.newDocumentBuilder();
            final URL resource = ClassLoader.getSystemClassLoader().getResource("lrpc.xml");
            // 3. 通过 DocumentBuilder 对象解析 XML 文件，得到一个 Document 对象
            Document document = null; // 假设文件名为 config.xml
            if (resource == null) {
                return;
            }
            document= builder.parse(resource.getPath());
            if (document==null){
//                走默认
                return;
            }

            // 4. 创建一个 XPath 对象
            XPath xpath = XPathFactory.newInstance().newXPath();

            // 5. 使用 XPath 语法获取各项参数的值
            int port = Integer.parseInt(xpath.evaluate("/configuration/port", document));
            if (port != 0) {
                SERVICE_PORT = port;
            }
            String applicationName = xpath.evaluate("/configuration/applicationname", document);
            if (StringUtils.isNotBlank(applicationName)) {
                this.applicationName = applicationName;
            }
            String registryAddress = xpath.evaluate("/configuration/registryAddress", document);
            if (StringUtils.isNotBlank(registryAddress)) {
                this.registryConfig = new RegistryConfig(registryAddress);
            }

            int dataCenterId = Integer.parseInt(xpath.evaluate("/configuration/IDGenerator/dataCenter", document));
            int machineId = Integer.parseInt(xpath.evaluate("/configuration/IDGenerator/machineId", document));

            if (dataCenterId != 0 && machineId != 0) {
                idGenerator = new IDGenerator(dataCenterId, machineId);
            }
//            序列化
            String serialization = xpath.evaluate("/configuration/serialization", document);
            if (StringUtils.isNotBlank(serialization)) {
                this.SERIALIZA_TYPE = serialization;
            }

            String compress = xpath.evaluate("/configuration/compress", document);
            if (StringUtils.isNotBlank(compress)) {
                this.COMPRESS_TYPE = compress;
            }


            String loadBalancerClass = xpath.evaluate("/configuration/loadblance/@class", document);

            if (StringUtils.isNotBlank(loadBalancerClass)) {
                 Class<?> aClass = Class.forName(loadBalancerClass);
                if (aClass == null) {
                    throw new RuntimeException("负载均衡器异常");
                }
                this.LOADBALANCER = (LoadBalancer)aClass.getConstructor().newInstance();
            }

            // 6. 打印各项参数的值
            System.out.println("端口号：" + port);
            System.out.println("应用名称：" + applicationName);
            System.out.println("注册中心连接地址：" + registryAddress);
            System.out.println("数据中心id：" + dataCenterId);
            System.out.println("机器中心：" + machineId);
            System.out.println("序列化：" + serialization);
            System.out.println("压缩的方式：" + compress);
            System.out.println("负载均衡的策略类：" + loadBalancerClass);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        final Configuration configuration = new Configuration();
    }


}
