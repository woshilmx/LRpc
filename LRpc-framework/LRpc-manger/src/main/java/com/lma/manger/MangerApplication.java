package com.lma.manger;

import com.lmx.model.Contanst;
import com.lmx.model.ZookeepNode;
import com.lmx.util.ZookeeperUtil;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

public class MangerApplication {
    public static void main(String[] args) {
//        创建zookeeper对象

        ZooKeeper zookeeper = ZookeeperUtil.createZookeeper(Contanst.DEFAULT_CONNECTSTRING, Contanst.SESSIONTIMEOUT);

        if (zookeeper != null) {
//            创建节点
            String Base = "/LRpc";
            String provider = "/LRpc/providers";
            String consumer = "/LRpc/consumers";

            ZookeepNode metaNode = new ZookeepNode(Base, null, CreateMode.PERSISTENT);
            ZookeepNode providerNode = new ZookeepNode(provider, null, CreateMode.PERSISTENT);
            ZookeepNode consumerNode = new ZookeepNode(consumer, null, CreateMode.PERSISTENT);


//            创建三个节点
            ZookeeperUtil.createNode(zookeeper, metaNode, null);
            ZookeeperUtil.createNode(zookeeper, providerNode, null);
            ZookeeperUtil.createNode(zookeeper, consumerNode, null);

            ZookeeperUtil.close(zookeeper);

        }


    }
}
