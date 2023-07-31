package com.lmx.core.configure;

import com.lmx.core.RegistryConfig;
import com.lmx.core.loadbalancer.LoadBalancer;
import com.lmx.generator.IDGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.net.URL;
@Slf4j
public class XmlResoulver {
    /**
     * 加载xml配置
     * */
    public void loadFromXml(Configuration configuration) {
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
                configuration.SERVICE_PORT = port;
            }
            String applicationName = xpath.evaluate("/configuration/applicationname", document);
            if (StringUtils.isNotBlank(applicationName)) {
                configuration.applicationName = applicationName;
            }
            String registryAddress = xpath.evaluate("/configuration/registryAddress", document);
            if (StringUtils.isNotBlank(registryAddress)) {
                configuration.registryConfig = new RegistryConfig(registryAddress);
            }
            String group = xpath.evaluate("/configuration/group", document);
            if (StringUtils.isNotBlank(group)) {
                configuration.setGroup(group);
            }

            int dataCenterId = Integer.parseInt(xpath.evaluate("/configuration/IDGenerator/dataCenter", document));
            int machineId = Integer.parseInt(xpath.evaluate("/configuration/IDGenerator/machineId", document));

            if (dataCenterId != 0 && machineId != 0) {
                configuration.idGenerator = new IDGenerator(dataCenterId, machineId);
            }
//            序列化
            String serialization = xpath.evaluate("/configuration/serialization", document);
            if (StringUtils.isNotBlank(serialization)) {
                configuration.SERIALIZA_TYPE = serialization;
            }

            String compress = xpath.evaluate("/configuration/compress", document);
            if (StringUtils.isNotBlank(compress)) {
                configuration.COMPRESS_TYPE = compress;
            }


            String loadBalancerClass = xpath.evaluate("/configuration/loadblance/@class", document);

            if (StringUtils.isNotBlank(loadBalancerClass)) {
                Class<?> aClass = Class.forName(loadBalancerClass);
                if (aClass == null) {
                    throw new RuntimeException("负载均衡器异常");
                }
                configuration.LOADBALANCER = (LoadBalancer)aClass.getConstructor().newInstance();
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
}
