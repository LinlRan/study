package com.wqy.study.zookeeper.origin;

import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * @ClassName : com.wqy.study.zookeeper.origin.ZookeeperSession
 * @Description :Zookeeper 原生实现
 * Created by mac on 2021-07-30 00:10:11
 */

public class ZookeeperSession {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperSession.class);

    private static CountDownLatch connectedSemaphore = new CountDownLatch(1);

    private ZooKeeper zooKeeper;

    public ZookeeperSession() {
        //连接zookeeper server是异步创建的会话
        //通过一个监听器+CountDownLatch，来确认真正建立了 Zk server 的连接

        try {
            this.zooKeeper = new ZooKeeper("127.0.0.1:2181", 50000, new ZookeeperWatcher());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    * 初始化实例
    * */
    public static void init() {
        getInstance();
    }

    /*
    * 建立zk session的watcher
    * */
    private class ZookeeperWatcher implements Watcher {

        @Override
        public void process(WatchedEvent event) {
            if (Event.KeeperState.SyncConnected == event.getState()) {
                connectedSemaphore.countDown();
            }
        }
    }

    /*
    * 静态内部类实现单例
    * */
    private static class Sinleton {
        private static ZookeeperSession instance;

        static {
            instance = new ZookeeperSession();
        }

        public static ZookeeperSession getInstance() {
            return instance;
        }
    }

    /*
    * 获取单例
    * */
    public static ZookeeperSession getInstance() {
        return Sinleton.getInstance();
    }

    /*
    * 重试获取分布式锁
    * */
    public void acquireDistributedLock(Long adId) {
        String path = "/ad-lock-" + adId;

        try{
            zooKeeper.create(path, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            logger.info("Success to acquire lock for adId = " + adId);
        } catch (Exception e) {
            //如果那个广告对应的锁node，已经存在了，就是已经被别人加锁了，这里抛出错误
            //NodeExistsException
            int count = 0;
            while(true) {
                try{
                    Thread.sleep(1000);
                    zooKeeper.create(path, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                } catch (Exception e2) {
                    count++;
                    logger.info("the " + count + "times try acquire lock for adId = " + adId);
                    continue;
                }
                logger.info("Success to acquire lock for adId = " + adId + " after " + count + " times try.....");
                break;
            }
        }
    }

    /*
    * 释放分布式锁
    * */
    public void releaseDistributedLock(Long adId) {
        String path = "/ad-lock-" + adId;
        try {
            zooKeeper.delete(path, -1);
            logger.info("release the lock for adId = " + adId);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Long adId = 1L;
        ZookeeperSession zookeeperSession = ZookeeperSession.getInstance();
        //1.获取锁
        zookeeperSession.acquireDistributedLock(adId);
        //2.执行一些修改资源的操作
        logger.info("I am updating the common resource");
        //3.释放锁
        zookeeperSession.releaseDistributedLock(adId);
    }
}
