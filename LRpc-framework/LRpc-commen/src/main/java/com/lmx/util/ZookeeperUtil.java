package com.lmx.util;


import com.lmx.model.ZookeepNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 与zk操作相关的工具类
 */
@Slf4j
public class ZookeeperUtil {

    private ZooKeeper zooKeeper;


    /**
     * 创建一个zookeeper对象
     */
    public static ZooKeeper createZookeeper(String connectstring, int sessionTimeout) {
        try {
//            等待连接成功后才返回结果，
            CountDownLatch countDownLatch = new CountDownLatch(1); // 阻塞
            ZooKeeper zooKeeper = new ZooKeeper(connectstring, sessionTimeout, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                        countDownLatch.countDown();
                    }
                }
            });
            countDownLatch.await();

            log.info("zookeepre connect success");
            return zooKeeper;
        } catch (IOException | InterruptedException e) {
            log.info("zookeepre connect fail");
            e.printStackTrace();
        }

        return null;
    }


    /**
     * 创建节点
     */
    public static boolean createNode(ZooKeeper zooKeeper, ZookeepNode zookeeperNode, Watcher watcher) {
        if (zooKeeper == null) {
            log.info("zookeepre is null");
            return false;
        }

        try {
//            等于空时创建
            if (zooKeeper.exists(zookeeperNode.getPath(), watcher) == null) {
                zooKeeper.create(zookeeperNode.getPath(), zookeeperNode.getBytes(),
                        ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        zookeeperNode.getCreateMode());
                log.info(zookeeperNode.getPath() + " create success");
            } else {
                log.info(zookeeperNode.getPath() + " has exited");
            }
            return true;
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 关闭节点
     */
    public static void close(ZooKeeper zooKeeper) {
        try {
            zooKeeper.close();
            log.info("zookeeper closed success");
        } catch (InterruptedException e) {
            log.info("zookeeper closed fail");
            e.printStackTrace();

        }
    }


    /**
     * 获取某个节点下子节点的个数
     *
     * @param zooKeeper
     * @param path
     */
    public static List<String> getChildNode(ZooKeeper zooKeeper, String path, Watcher watcher) {
        if (zooKeeper == null) {
            log.info("zookeepre is null");
            return null;
        }
        try {
            return zooKeeper.getChildren(path, watcher);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
          throw new ZookeeperException("获取子节点失败");
        }

    }
}
