package com.lmx.core.configure;


import com.lmx.core.compress.Compress;
import com.lmx.core.compress.CompressFactory;
import com.lmx.core.loadbalancer.LoadBalancer;
import com.lmx.core.serialization.Serializa;
import com.lmx.core.serialization.SerializaFactory;
import lombok.extern.slf4j.Slf4j;



/**
 * spi加载
 */
@Slf4j
public class Spiresolver {
    /**
     * 加载spi的机制
     */
    public void loadFromSpi(Configuration configuration) {

//        压缩类型
        ObjectWapper<Serializa> serializaObjectWapper = SpiHandler.get(Serializa.class);
        if (serializaObjectWapper != null) {
            SerializaFactory.addFactory(serializaObjectWapper.getCode(), serializaObjectWapper.getName(), serializaObjectWapper);
            configuration.setSERIALIZA_TYPE(serializaObjectWapper.getName());
            log.info("spi加载序列化器{}",serializaObjectWapper.getName());
        }
//        序列化类型
        ObjectWapper<Compress> compressObjectWapper = SpiHandler.get(Compress.class);

        if (compressObjectWapper != null) {
            CompressFactory.addFactory(compressObjectWapper.getCode(), compressObjectWapper.getName(), compressObjectWapper);
            configuration.setCOMPRESS_TYPE(compressObjectWapper.getName());
            log.info("spi加载压缩器{}",compressObjectWapper.getName());
        }

        ObjectWapper<LoadBalancer> loadBalancerObjectWapper = SpiHandler.get(LoadBalancer.class);

        if (loadBalancerObjectWapper != null) {

            configuration.setLOADBALANCER(loadBalancerObjectWapper.getData());
            log.info("spi加载负载均衡器{}",loadBalancerObjectWapper.getName());
        }





    }
}
