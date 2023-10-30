package com.shiyi.registry;

import com.shiyi.extension.SPI;

import java.net.InetSocketAddress;

/**
 * service registry
 *
 * @Author:shiyi
 * @create: 2023-05-18  13:53
 */
@SPI
public interface ServiceRegistry {


    /**
     * registry service
     *
     * @param rpcServiceName rpc service name
     * @param inetSocketAddress service address
     */
    void registryService(String rpcServiceName, InetSocketAddress inetSocketAddress);

}
