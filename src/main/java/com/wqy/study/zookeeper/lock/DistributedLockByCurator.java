package com.wqy.study.zookeeper.lock;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.checkerframework.checker.units.qual.C;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;

/**
 * @ClassName : com.wqy.study.zookeeper.lock.DistributedLockByCurator
 * @Description : Curator实现zk分布式锁工具类
 * Created by mac on 2021-07-30 14:12:23
 */

@Service
public class DistributedLockByCurator implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(DistributedLockByCurator.class);

    private static final String ROOT_PATH_LOCK = "rootlock";
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    @Autowired
    private CuratorFramework curatorFramework;


    /*
    * 获取分布式锁
    * */
    public void acrequireDistributedLock(String path) {
        String keyPath = "/" + ROOT_PATH_LOCK + "/" +path;
        while (true) {
            try {
                curatorFramework
                        .create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.EPHEMERAL)
                        .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                        .forPath(keyPath);
                logger.info("Success for acquire lock for path:{}" + keyPath);
                break;
            } catch (Exception e) {
                logger.error("Fail for acquire lock for path:{}" + keyPath);
                logger.info("while try again....");
                try {
                    if (countDownLatch.getCount() <= 0) {
                        countDownLatch = new CountDownLatch(1);
                    }
                    countDownLatch.await();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    /*
    * 释放分布式锁
    * */
    public boolean releaseDistributedLock(String path) {
        try {
            String keyPath = "/" + ROOT_PATH_LOCK + "/" +path;
            if (curatorFramework.checkExists().forPath(keyPath) != null) {
                curatorFramework.delete().forPath(keyPath);
                logger.info("Success to release the lock for path = " + keyPath);
            }
        } catch (Exception e) {
            logger.error("Fail to release lock");
            return false;
        }
        return true;
    }

    /*
    * 创建watcher事件
    * */
    private void addWatcher(String path) throws Exception {
        String keyPath;
        if (path.equals(ROOT_PATH_LOCK)) {
            keyPath = "/" + path;
        } else {
            keyPath = "/" + ROOT_PATH_LOCK + "/" +path;
        }
        final PathChildrenCache cache = new PathChildrenCache(curatorFramework, keyPath, false);
        cache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        cache.getListenable().addListener(((client, event) -> {
            if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)) {
                String oldPath = event.getData().getPath();
                logger.info("Success to release lock for path:{}", oldPath);
                if (oldPath.contains(path)) {
                    //释放计数器，让当前的请求获取锁
                    countDownLatch.countDown();
                }
            }
        }));
    }

    /*
    * 创建父节点，并创建永久节点
    * */
    @Override
    public void afterPropertiesSet() throws Exception {
        curatorFramework = curatorFramework.usingNamespace("lock-namespace");
        String path = "/" + ROOT_PATH_LOCK;
        try {
            if (curatorFramework.checkExists().forPath(path) == null) {
                curatorFramework.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.EPHEMERAL)
                        .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                        .forPath(path);
            }
            addWatcher(ROOT_PATH_LOCK);
            logger.info("root path 的 watcher 事件创建成功");
        } catch (Exception e) {
            logger.error("connected zk fail, please check the log >> {}", e.getMessage(), e);
        }
    }
}
