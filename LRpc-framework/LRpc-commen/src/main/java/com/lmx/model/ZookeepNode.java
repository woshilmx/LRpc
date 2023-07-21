package com.lmx.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.zookeeper.CreateMode;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZookeepNode {
    /**
     * 节点的路径
     */
    private String path;
    /**
     * 节点中保存的数据
     */
    private byte[] bytes;


    private CreateMode createMode;
}
