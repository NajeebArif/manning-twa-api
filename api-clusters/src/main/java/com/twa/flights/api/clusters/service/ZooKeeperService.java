package com.twa.flights.api.clusters.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.barriers.DistributedBarrier;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.twa.flights.api.clusters.configuration.zookeeper.ZooKeeperCuratorConfiguration;

@Service
public class ZooKeeperService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperService.class);

    private static final int MAX_WAITING_TIME = 10000;

    private final CuratorFramework zkClient;

    @Autowired
    public ZooKeeperService(ZooKeeperCuratorConfiguration zooKeeperCuratorConfiguration) {
        zkClient = zooKeeperCuratorConfiguration.getClient();
    }

    public boolean checkIfBarrierExists(String path) {
        Stat stat;
        try {
            stat = zkClient.checkExists().forPath(path);
        } catch (Exception e) {
            LOGGER.error("I'm {}: There was an error checking if barrier {} exists", getHostName(), path, e);
            stat = null;
        }

        return Objects.nonNull(stat);
    }

    public boolean createBarrier(String path) {
        boolean barrierExists = false;

        try {
            getBarrier(path).setBarrier();
            LOGGER.debug("Creating barrier in {}", path);
        } catch (Exception e) {
            LOGGER.debug("I'm {}: There was an error creating barrier for path {}", getHostName(), path, e);
            barrierExists = true;
        }

        return barrierExists;
    }

    public void deleteBarrier(String path) {

        if (!checkIfBarrierExists(path)) {
            return;
        }

        try {
            zkClient.delete().quietly().forPath(path);
            LOGGER.debug("Barrier {} was deleted", path);
        } catch (Exception e) {
            LOGGER.debug("I'm {}: There was an error deleting the barrier {}", getHostName(), path, e);
        }
    }

    public void waitOnBarrier(String path) {

        try {
            LOGGER.debug("Waiting on barrier {}", path);
            getBarrier(path).waitOnBarrier(MAX_WAITING_TIME, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            LOGGER.error("I'm {}: There was an error waiting in barrier {}", getHostName(), path, e);
        }
    }

    private DistributedBarrier getBarrier(String path) {

        return new DistributedBarrier(zkClient, path) {
            @Override
            public synchronized void setBarrier() throws Exception {
                try {
                    zkClient.create().creatingParentContainersIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
                } catch (KeeperException.NodeExistsException nodeExistsException) {
                    LOGGER.debug("I'm {}: Node exists exception for path {}", getHostName(), path);
                    throw nodeExistsException;
                }
            }
        };
    }

    private String getHostName() {
        String hostName = "";

        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            LOGGER.error("There was an error to obtain the hostname");
        }

        return hostName;
    }

}