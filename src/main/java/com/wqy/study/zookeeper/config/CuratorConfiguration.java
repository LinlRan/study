package com.wqy.study.zookeeper.config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName : com.wqy.study.zookeeper.config.CuratorConfiguration
 * @Description :
 * Created by mac on 2021-07-29 23:56:52
 */

@Configuration
public class CuratorConfiguration {

    @Value("${curator.retryCount}")
    private int retryCount;

    @Value("${curator.elasedTimeMs}")
    private int elasedTimeMs;

    @Value("${curator.connectString}")
    private String connectString;

    @Value("${curator.sessionTimeoutMs}")
    private int sessionTimeoutMs;

    @Value("${curator.connectionTimeoutMs}")
    private int connectionTimeoutMs;

    @Bean(initMethod = "start")
    public CuratorFramework curatorFramework() {
        return CuratorFrameworkFactory.newClient(
                connectString,
                sessionTimeoutMs,
                connectionTimeoutMs,
                new RetryNTimes(retryCount, elasedTimeMs)
        );
    }
}
