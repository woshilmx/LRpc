package com.lmx.core.loadbalancer.iml;

import com.lmx.core.loadbalancer.AbstractLoadBalancer;
import com.lmx.core.loadbalancer.Selector;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class HashLoadBalancer extends AbstractLoadBalancer {
    @Override
    protected Selector getSelector() {
        return new ConsistentHashSelector();
    }



        // 构造函数，初始化一致性哈希环
        public static class ConsistentHashSelector implements Selector {

            private final int virtualNodes;
            private final TreeMap<Long, InetSocketAddress> hashRing;

            public ConsistentHashSelector() {
                this.virtualNodes = 100; // 默认虚拟节点数量为100
                this.hashRing = new TreeMap<>();
            }

            @Override
            public InetSocketAddress getSelector(List<InetSocketAddress> serviceAddresses) {
                // 清空哈希环
                hashRing.clear();

                // 为每个真实节点创建虚拟节点，并将虚拟节点添加到哈希环中
                for (InetSocketAddress address : serviceAddresses) {
                    addNode(address);
                }

                // 获取请求的哈希值
                long requestHash = hash(String.valueOf(System.currentTimeMillis()));

                // 在哈希环上寻找第一个大于等于请求哈希值的节点，如果没有，则选择第一个节点
                SortedMap<Long, InetSocketAddress> tailMap = hashRing.tailMap(requestHash);
                Long targetHash = !tailMap.isEmpty() ? tailMap.firstKey() : hashRing.firstKey();

                return hashRing.get(targetHash);
            }

            // 哈希函数，使用SHA-256算法计算给定字符串的哈希值，并转换为64位长整型
            private long hash(String key) {
                try {
                    MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
                    sha256.update(StandardCharsets.UTF_8.encode(key));
                    byte[] digest = sha256.digest();
                    ByteBuffer buffer = ByteBuffer.wrap(digest);
                    return buffer.getLong();
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException("SHA-256 hash algorithm not found.");
                }
            }

            // 添加节点
            public void addNode(InetSocketAddress nodeAddress) {
                for (int i = 0; i < virtualNodes; i++) {
                    String virtualNodeName = nodeAddress.toString() + "&&VN" + i;
                    long hash = hash(virtualNodeName);
                    hashRing.put(hash, nodeAddress);
                }
            }

            // 移除节点
            public void removeNode(InetSocketAddress nodeAddress) {
                for (int i = 0; i < virtualNodes; i++) {
                    String virtualNodeName = nodeAddress.toString() + "&&VN" + i;
                    long hash = hash(virtualNodeName);
                    hashRing.remove(hash);
                }
            }
        }

}
