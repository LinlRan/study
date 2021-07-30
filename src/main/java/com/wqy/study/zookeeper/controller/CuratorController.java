package com.wqy.study.zookeeper.controller;

import com.wqy.study.zookeeper.lock.DistributedLockByCurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TransferQueue;

/**
 * @ClassName : com.wqy.study.zookeeper.controller.CuratorController
 * @Description :
 * Created by mac on 2021-07-30 14:39:06
 */

@RestController
@RequestMapping("/curator")
public class CuratorController {
    private static final Logger logger = LoggerFactory.getLogger(CuratorController.class);

    @Autowired
    private DistributedLockByCurator distributedLockByCurator;

    private static final String path = "test";

    @GetMapping("/lock1")
    public Boolean getLock1() {
        Boolean flag;
        distributedLockByCurator.acrequireDistributedLock(path);
        try {
            logger.info("I am Lock1, I am Updating the source.....");
            Thread.sleep(20000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            flag = distributedLockByCurator.releaseDistributedLock(path);
        }
        return flag;
    }

    @GetMapping("/lock2")
    public Boolean getLock2() {
        Boolean flag;
        distributedLockByCurator.acrequireDistributedLock(path);
        try {
            logger.info("I am Lock2, I am Updating the source.....");
            Thread.sleep(15000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            flag = distributedLockByCurator.releaseDistributedLock(path);
        }
        return flag;
    }
}
