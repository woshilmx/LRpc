# 手写Rpc远程调用框架

## 1. 简介
## 1.1 什么是RPC框架

RPC（Remote Procedure Call）是一种通信协议，即，远程过程调用，允许应用程序在不同的计算机或网络节点之间进行远程调用，就像调用本地函数一样。RPC框架是一套实现这种通信协议的软件工具集，用于简化分布式系统之间的远程调用过程。在RPC框架中，客户端可以像调用本地函数一样调用远程服务，并且不需要关心底层网络通信的细节。

常见的RPC框架有Dubbo、gRPC和Thrift等，它们在不同的语言和场景中得到了广泛应用。自己手写一个RPC框架可以更好地理解RPC的原理和实现，并满足特定项目或需求的定制化需求。

## 1.2 目标和用途

手写RPC框架的主要目标是实现高效、稳定、易用且灵活的远程调用框架。通过RPC框架，可以将分布式系统的不同部分连接在一起，实现跨网络的函数调用，从而实现系统之间的互联互通。RPC框架可以用于以下场景：

- 微服务架构：将系统拆分成独立的微服务，通过RPC框架进行服务调用，实现松耦合的分布式系统。
- 跨语言通信：RPC框架可以让不同语言编写的服务相互调用，实现跨语言的系统集成。
- 分布式任务调度：通过RPC框架可以在集群中实现任务的远程调度和执行。

## 1.3 主要特点和优势

一般情况下，手写的RPC框架应该具有以下特点和优势：

- **高性能**：通过优化网络通信、序列化和反序列化等环节，实现高效的远程调用，降低通信开销。
- **易扩展**：支持扩展不同的序列化和压缩算法，以及负载均衡、限流和熔断等功能。
- **易用性**：提供简单易用的API，使开发人员能够快速上手，方便集成到现有项目中。
- **稳定性**：通过心跳检测、异常处理和重试机制，保障RPC调用的稳定性和可靠性。

## 1.4 术语和定义

在RPC框架中，一些常用的术语和定义如下：

- **RPC接口**：在RPC框架中定义的远程服务接口，类似于普通接口，用于描述服务的方法和参数。
- **服务端**：提供RPC服务的计算机或节点，接收并处理客户端发起的远程调用请求。
- **客户端**：调用RPC服务的计算机或节点，向服务端发起远程调用请求。
- **序列化**：将数据结构或对象转换成字节流，以便在网络上传输。
- **反序列化**：将字节流转换成数据结构或对象，以便在接收端进行处理。
- **负载均衡**：在多个服务提供者中选择一个合适的节点来处理请求，以实现请求的均衡分配。
- **心跳检测**：用于检测服务端的存活状态，确保客户端能够感知到服务的上线和下线。

## 2. 架构设计
   ### 2.1 框架整体架构
   ### 2.2 组件和模块划分
   ### 2.3 数据流和调用流程

### 2.4 注册中心

- 选中Zookeeper作为注册中心

- 使用版本号：3.8.2

  ~~~xml
    <dependency>
      <groupId>org.apache.zookeeper</groupId>
      <artifactId>zookeeper</artifactId>
      <version>3.8.2</version>
   </dependency>
   
  
  ~~~


- 在本项目中我们采用Zookeeper作为注册中心，接下来我们将通过以下几个案例，复习java操控Zookeeper的方式

- 创建一个连接对象

~~~java
/**
 * 初始化一个zookeeper连接对象，后续操作我们将调用zookeeper对象的方法完成
 */
@Before
public void init(){
        String url="114.116.233.39:2181"; // 注册地址
        int sessionTimeout=3000; // 过期时间
        try{
        zooKeeper=new ZooKeeper(url,sessionTimeout,null);

        }catch(IOException e){
        e.printStackTrace();
        }
        }
~~~

- 增加一个节点

  - 节点的类型
    - `PERSISTENT` (0, false, false, false, false): 创建一个持久节点。这种类型的节点不会因为创建它们的客户端会话结束而被删除。
    - `PERSISTENT_SEQUENTIAL` (2, false, true, false, false): 创建一个持久顺序节点。这种类型的节点的名称将在其末尾附加一个单调递增的数字。
    - `EPHEMERAL` (1, true, false, false, false): 创建一个临时节点。这种类型的节点会在创建它们的客户端会话结束时被删除。
    - `EPHEMERAL_SEQUENTIAL` (3, true, true, false, false):
      创建一个临时顺序节点。这种类型的节点的名称将在其末尾附加一个单调递增的数字，并且会在创建它们的客户端会话结束时被删除。
    - `CONTAINER` (4, false, false, true, false): 创建一个容器节点。当最后一个子节点被删除时，容器节点将被异步删除。
    - `PERSISTENT_WITH_TTL` (5, false, false, false, true):
      创建一个带有TTL（生存时间）的持久节点。这种类型的节点将在给定的TTL时间后被删除，无论创建它们的客户端会话是否结束。
    - `PERSISTENT_SEQUENTIAL_WITH_TTL` (6, false, true, false, true):
      创建一个带有TTL（生存时间）的持久顺序节点。这种类型的节点的名称将在其末尾附加一个单调递增的数字，并且将在给定的TTL时间后被删除，无论创建它们的客户端会话是否结束。
  - 节点的权限列表
    - 在ZooDefs.Ids接口中
    - `ANYONE_ID_UNSAFE`：这是一个Id实例，表示所有用户。"world"是一个特殊的scheme，表示这个Id适用于所有的ZooKeeper客户端，而"anyone"
      是这个scheme下的一个特殊的id，表示所有的用户。
    - `AUTH_IDS`：这是一个Id实例，表示已经通过身份验证的用户。"auth"是一个特殊的scheme，表示这个Id适用于已经通过身份验证的ZooKeeper客户端。
    - `OPEN_ACL_UNSAFE`：这是一个ACL列表，其中包含一个ACL规则，这个规则允许所有用户执行所有操作（读取、写入、创建子节点、删除子节点、设置ACL）。这个列表被称为"unsafe"
      ，因为它允许所有用户执行所有操作，可能会导致安全问题。
    - `CREATOR_ALL_ACL`：这是一个ACL列表，其中包含一个ACL规则，这个规则允许节点的创建者执行所有操作。这个列表只允许创建节点的客户端执行所有操作，对其他客户端来说，这个节点是只读的。
    - `READ_ACL_UNSAFE`：这是一个ACL列表，其中包含一个ACL规则，这个规则允许所有用户读取节点数据。这个列表被称为"unsafe"，因为它允许所有用户读取节点数据，可能会导致安全问题。

  ~~~java
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
  ~~~


- 删除一个节点

~~~java
    @Test
public void deleteTest()throws InterruptedException,KeeperException{

        /**
         * version 为-1时，不考虑版本号直接删除
         * 版本号的存在可以起到乐观锁的作用
         * */

// 
        Stat lrpc=zooKeeper.exists("lrpc",null); // 获取版本号
        lrpc.getVersion();
        zooKeeper.delete("/lrpc",lrpc.getVersion());
        }
~~~

- Stat 对象中属性的含义

~~~ajva
在Apache ZooKeeper中，org.apache.zookeeper.data.Stat类表示一个ZooKeeper节点（znode）的元数据。以下是每个属性的含义：

czxid：这是创建这个节点的事务的zxid（ZooKeeper事务ID）。每个ZooKeeper事务都有一个唯一的zxid，这个zxid可以用来确定事务的顺序。

mzxid：这是最后一次修改这个节点的事务的zxid。

ctime：这是创建这个节点的时间，以毫秒为单位。

mtime：这是最后一次修改这个节点的时间，以毫秒为单位。

version：这是这个节点的数据的版本号。每次修改节点数据时，版本号都会增加。

cversion：这是这个节点的子节点列表的版本号。每次修改子节点列表（例如，添加或删除子节点）时，版本号都会增加。

aversion：这是这个节点的ACL（Access Control List）的版本号。每次修改ACL时，版本号都会增加。

ephemeralOwner：如果这个节点是一个临时节点，这个属性表示创建这个节点的会话的session ID。如果这个节点不是临时节点，这个属性的值为0。

dataLength：这是这个节点的数据的长度，以字节为单位。

numChildren：这是这个节点的子节点的数量。

pzxid：这是这个节点或其任何子节点最后一次被修改的事务的zxid。

这些属性提供了关于节点的详细信息，包括它的生命周期、数据、子节点和权限等。
~~~

- 查询节点数据

~~~jav
    @Test
    public void findTest() throws InterruptedException, KeeperException {

        /**
         * version 为-1时，不考虑版本号直接删除
         * 版本号的存在可以起到乐观锁的作用
         * */

        Stat lrpc = zooKeeper.exists("/lrpc", null); // 获取版本号
        lrpc.getVersion();
        byte[] data = zooKeeper.getData("/lrpc", null, null);
        String s = new String(data);
        System.out.println(s);
    }
~~~

- 修改节点数据

~~~java
 @Test
public void updateTest()throws InterruptedException,KeeperException{

        /**
         * version 为-1时，不考虑版本号直接删除
         * 版本号的存在可以起到乐观锁的作用
         * */
//
        Stat exists=zooKeeper.exists("/lrpc",null);

        System.out.println(exists.getVersion());
//        修改 -1时忽略版本号，
        zooKeeper.setData("/lrpc","newdata".getBytes(StandardCharsets.UTF_8),exists.getVersion());

        System.out.println(zooKeeper.exists("/lrpc",null).getVersion());
        }
~~~

- 复习Watacher机制

  ![image-20230720205419521](C:\Users\Lenovo\AppData\Roaming\Typora\typora-user-images\image-20230720205419521.png)

- Zookeeper提供了数据的发布/订阅功能。多个订阅者可监听某一特定主题对象（节点）。当主题对象发生改
  变（数据内容改变，被删除等），会实时通知所有订阅者。该机制在被订阅对象发生变化时，会异步通知客户端，因此客户端不必在注册监听后轮询阻塞。

- Watcher机制实际上与观察者模式类似，也可看作观察者模式在分布式场景中给的一种应用。

![image-20230720210051002](https://lmxboke.oss-cn-beijing.aliyuncs.com/image-20230720210051002.png)

- 在Apache ZooKeeper中，`org.apache.zookeeper.Watcher.Event.EventType`枚举定义了可以触发watcher（观察者）的事件类型。以下是每个事件类型的含义：

  - `None`：这是一个特殊的事件，表示没有发生特定的事件。这个事件通常用于表示会话的状态变化，例如，当客户端连接到ZooKeeper服务器或从服务器断开连接时，会触发一个`None`事件。
  - `NodeCreated`：这个事件表示一个节点被创建。当你对一个不存在的节点设置watcher，然后这个节点被创建时，会触发这个事件。
  - `NodeDeleted`：这个事件表示一个节点被删除。当你对一个存在的节点设置watcher，然后这个节点被删除时，会触发这个事件。
  - `NodeDataChanged`：这个事件表示一个节点的数据被修改。当你对一个存在的节点设置watcher，然后这个节点的数据被修改时，会触发这个事件。
  - `NodeChildrenChanged`：这个事件表示一个节点的子节点列表发生变化。当你对一个存在的节点设置watcher，然后这个节点的子节点被添加或删除时，会触发这个事件。
  - `DataWatchRemoved`：这个事件表示一个数据watcher被移除。这个事件在ZooKeeper 3.5.0及更高版本中可用。
  - `ChildWatchRemoved`：这个事件表示一个子节点watcher被移除。这个事件在ZooKeeper 3.5.0及更高版本中可用。
  - `PersistentWatchRemoved`：这个事件表示一个持久watcher被移除。这个事件在ZooKeeper 3.6.0及更高版本中可用。

  注意：每个watcher只会触发一次。也就是说，当一个事件发生并触发watcher后，如果你想要继续观察这个节点，你需要重新设置watcher。

-在Apache ZooKeeper中，`org.apache.zookeeper.Watcher.Event.KeeperState`枚举定义了ZooKeeper客户端会话的可能状态。以下是每个状态的含义：

- `Unknown`：这是一个特殊的状态，表示会话的状态未知。这个状态已经被弃用，不应在新的代码中使用。
- `Disconnected`：这个状态表示客户端与ZooKeeper服务器断开连接。当客户端无法与服务器通信时，会触发一个`None`事件，并将状态设置为`Disconnected`。
- `NoSyncConnected`：这个状态表示客户端已经连接到ZooKeeper服务器，但是客户端和服务器之间的数据可能不同步。这个状态已经被弃用，不应在新的代码中使用。
- `SyncConnected`：这个状态表示客户端已经连接到ZooKeeper服务器，并且客户端和服务器之间的数据已经同步。
- `AuthFailed`：这个状态表示客户端的身份验证失败。当客户端尝试使用无效的用户名或密码进行身份验证时，会触发一个`None`事件，并将状态设置为`AuthFailed`。
- `ConnectedReadOnly`：这个状态表示客户端已经以只读模式连接到ZooKeeper服务器。在只读模式下，客户端可以读取数据，但不能修改数据。
- `SaslAuthenticated`：这个状态表示客户端已经通过SASL（Simple Authentication and Security Layer）进行了身份验证。
- `Expired`：这个状态表示客户端的会话已经过期。当客户端在会话超时时间内无法与服务器通信时，会话会过期，会触发一个`None`事件，并将状态设置为`Expired`。
- `Closed`：这个状态表示客户端的会话已经关闭。当你调用`ZooKeeper.close`方法关闭客户端时，会触发一个`None`事件，并将状态设置为`Closed`。

注意：当会话的状态发生变化时，ZooKeeper会触发一个`None`事件，并将新的状态作为事件的状态。你可以在你的`Watcher`实现中处理这个事件，以便在会话的状态发生变化时执行适当的操作。

- 以下案例和

~~~java
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
        byte[] data = zooKeeper.getData("/lrpc", new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                if (watchedEvent.getType() == Event.EventType.NodeDataChanged) {
                    System.out.println("数据改变了");
                }
            }
        }, null);
        String s = new String(data);
        zooKeeper.setData("/lrpc", "newdata".getBytes(StandardCharsets.UTF_8), -1);
        System.out.println(s);
        while (true) {

        }
    }
~~~

**注意：**在以上代码中，watch事件只执行一次，如果需要继续观察，则需要重新注册

- 给某个节点增加一个watcher

- ~~~java
  zooKeeper.addWatch("/lrpc",wa,AddWatchMode.PERSISTENT); 
  ~~~

- 在Apache ZooKeeper中，`org.apache.zookeeper.AddWatchMode`枚举定义了添加watcher（观察者）时可以使用的模式。以下是每个模式的含义：

  - `PERSISTENT`：这个模式表示watcher是持久的。也就是说，即使触发了watcher，它仍然会继续观察节点的状态变化，直到客户端会话结束。这意味着你不需要手动重新设置watcher。
  - `PERSISTENT_RECURSIVE`：这个模式表示watcher是持久的，并且会递归地应用到所有的子节点。也就是说，当你在一个节点上添加一个`PERSISTENT_RECURSIVE`
    模式的watcher时，这个watcher不仅会观察这个节点的状态变化，还会观察这个节点的所有现有和未来的子节点的状态变化。
  - 在此段代码中，监听事件触发了四次

  ~~~java
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
          zooKeeper.addWatch("/lrpc",wa,AddWatchMode.PERSISTENT); // 这个
          zooKeeper.setData("/lrpc", "newdata".getBytes(StandardCharsets.UTF_8), -1);
          zooKeeper.setData("/lrpc", "newdata".getBytes(StandardCharsets.UTF_8), -1);
          zooKeeper.setData("/lrpc", "newdata".getBytes(StandardCharsets.UTF_8), -1);
  //        System.out.println(s);
          while (true) {
  
          }
      }
  ~~~

## 

## 3. 实现步骤
   ### 3.1 定义RPC接口
   ### 3.2 实现RPC服务器
   ### 3.3 实现RPC客户端
   ### 3.4 序列化和反序列化
   ### 3.5 网络通信
   ### 3.6 错误处理和异常

## 4. 底层通信
   ### 4.1 使用Netty实现基础通信

## 5. 传输协议
   ### 5.1 封装请求和响应

## 6. 序列化和反序列化
   ### 6.1 概述
   ### 6.2 JDK序列化
   ### 6.3 Hessian序列化
   ### 6.4 其他序列化方式

## 7. 压缩
   ### 7.1 概述
   ### 7.2 Gzip压缩
   ### 7.3 其他压缩算法

## 8. 负载均衡
   ### 8.1 基于轮询的负载均衡
   ### 8.2 一致性哈希
   ### 8.3 最短响应时间负载均衡

## 9. 心跳检测
   ### 9.1 动态感知服务上下线

## 10. 高级特性
   ### 10.1 服务注册与发现
   ### 10.2 包扫描机制
   ### 10.3 XML配置
   ### 10.4 SPI机制

## 11. 安全与稳定性
   ### 11.1 异常重试
   ### 11.2 服务端限流
   ### 11.3 客户端熔断
   ### 11.4 流量分组
   ### 11.5 优雅停机

## 14. 限制和未来改进
   ### 14.1 目前的局限性
   ### 14.2 可能的改进方向

