package com.shiyi.registry.zk;

import com.shiyi.config.RpcConfiguration;
import com.shiyi.enums.RpcErrorMessageEnum;
import com.shiyi.exception.RpcException;
import com.shiyi.extension.ExtensionLoader;
import com.shiyi.loadbalance.LoadBalance;
import com.shiyi.registry.ServiceDiscovery;
import com.shiyi.registry.zk.util.CuratorUtils;
import com.shiyi.remoting.dto.RpcRequest;
import com.shiyi.utils.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * service discovery based on zookeeper
 *
 * @Author:shiyi
 * @create: 2023-05-18  14:54
 */
@Slf4j
public class ZkServiceDiscoveryImpl implements ServiceDiscovery {

    private final LoadBalance loadBalance;

    public ZkServiceDiscoveryImpl() {
        //TODO
        this.loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(RpcConfiguration.getRpcConfig().getLoadBalance());
    }

    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest) {
        String rpcServiceName = rpcRequest.getRpcServiceName();
        // TODO：若注册中心挂了，则服务停止了，可以添加本地缓存功能，先搜索缓存
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        List<String> serviceUrlList = CuratorUtils.getChildrenNodes(zkClient, rpcServiceName);
        if(CollectionUtil.isEmpty(serviceUrlList)){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND,rpcServiceName);
        }
        //load balancing
        String targetServiceUrl = loadBalance.selectServiceAddress(serviceUrlList, rpcRequest);
        log.info("Successfully found the service:[{}] --> address:[{}]",rpcServiceName,targetServiceUrl);
        String[] socketAddressArray = targetServiceUrl.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);
        return new InetSocketAddress(host,port);
    }
}
