package com.lmx;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


public class ZkTest {

    public ZooKeeper zooKeeper;


    /**
     * 初始化一个zookeeper连接对象，后续操作我们将调用zookeeper对象的方法完成
     */
    @Before
    public void init() {
        String url = "114.116.233.39:2181"; // 注册地址
        int sessionTimeout = 3000; // 过期时间
        try {
            zooKeeper = new ZooKeeper(url, sessionTimeout, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    if (watchedEvent.getState() == Event.KeeperState.SaslAuthenticated) {
                        System.out.println("zookeeper连接成功");
                    }
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void addTest() throws InterruptedException, KeeperException {

        /**
         * 再次我们创建了一个持久节点，同时节点的类型有
         * */

        String s = zooKeeper.create("/lrpc", "hello".getBytes(StandardCharsets.UTF_8),
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);
        System.out.println(s.length()); // 返回值是节点的路径
//        System.out.println("2232323");
    }


    @Test
    public void deleteTest() throws InterruptedException, KeeperException {

        /**
         * version 为-1时，不考虑版本号直接删除
         * 版本号的存在可以起到乐观锁的作用
         * */

        Stat lrpc = zooKeeper.exists("/lrpc", null); // 获取版本号
        lrpc.getVersion();
        zooKeeper.delete("/lrpc", lrpc.getVersion());
    }

    @Test
    public void watchTest() throws InterruptedException, KeeperException {

        /**
         * version 为-1时，不考虑版本号直接删除
         * 版本号的存在可以起到乐观锁的作用
         * */

        Stat lrpc = zooKeeper.exists("/lrpc", null); // 获取版本号
        lrpc.getVersion();
        /**
         * 当数据改变时触发
         * */
        Watcher wa = new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                if (watchedEvent.getType() == Event.EventType.NodeDataChanged) {
                    System.out.println("数据改变了");
                }
            }
        };
        byte[] data = zooKeeper.getData("/lrpc", wa, null);
        String s = new String(data);
        zooKeeper.setData("/lrpc", "newdata".getBytes(StandardCharsets.UTF_8), -1);
        zooKeeper.addWatch("/lrpc", wa, AddWatchMode.PERSISTENT); // 这个
        zooKeeper.setData("/lrpc", "newdata".getBytes(StandardCharsets.UTF_8), -1);
        zooKeeper.setData("/lrpc", "newdata".getBytes(StandardCharsets.UTF_8), -1);
        zooKeeper.setData("/lrpc", "newdata".getBytes(StandardCharsets.UTF_8), -1);
//        System.out.println(s);
        while (true) {

        }
    }


    @Test
    public void updateTest() throws InterruptedException, KeeperException {
        /**
         * version 为-1时，不考虑版本号直接删除
         * 版本号的存在可以起到乐观锁的作用
         * */
        Stat exists = zooKeeper.exists("/lrpc", true);

        System.out.println(exists.getVersion());
//       修改 -1时忽略版本号，
        zooKeeper.setData("/lrpc", "newdata".getBytes(StandardCharsets.UTF_8), exists.getVersion());

        System.out.println(zooKeeper.exists("/lrpc", true).getVersion());
    }


}
