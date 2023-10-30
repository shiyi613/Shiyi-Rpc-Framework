package com.shiyi.registry.zk;

import com.shiyi.registry.ServiceRegistry;
import com.shiyi.registry.zk.util.CuratorUtils;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;

/**
 * service registration based on zookeeper
 *
 * @Author:shiyi
 * @create: 2023-05-18  14:54
 */
public class ZkServiceRegistryImpl implements ServiceRegistry {


    @Override
    public void registryService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        String servicePath = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName + inetSocketAddress.toString();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
//        CuratorUtils.createPersistentNode(zkClient,servicePath);
        CuratorUtils.createTemporaryNode(zkClient,servicePath);
    }
}
