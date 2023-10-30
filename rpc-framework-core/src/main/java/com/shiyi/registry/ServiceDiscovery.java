package com.shiyi.registry;

import com.shiyi.extension.SPI;
import com.shiyi.remoting.dto.RpcRequest;

import java.net.InetSocketAddress;

/**
 * service discovery
 *
 * @Author:shiyi
 * @create: 2023-05-18  13:53
 */
@SPI
public interface ServiceDiscovery {

    /**
     * look up service by rpcServiceName
     *
     * @param rpcRequest rpc service pojo
     * @return service address
     */
    InetSocketAddress lookupService(RpcRequest rpcRequest);
}
