package com.shiyi.provider;

import com.shiyi.config.RpcServiceConfig;

/**
 * store and provide service object
 *
 * @Author:shiyi
 * @create: 2023-05-18  18:02
 */
public interface ServiceProvider {

    /**
     * @param rpcServiceConfig rpc service related attributes
     */
    void addService(RpcServiceConfig rpcServiceConfig);

    /**
     * @param rpcServiceName rpc service name
     * @return service object
     */
    Object getService(String rpcServiceName);

    /**
     *
     * @param rpcServiceName rpc service name
     * @return service timeout
     */
    String getServiceTimeout(String rpcServiceName);

    /**
     * @param rpcServiceConfig rpc service related attributes
     */
    void publishService(RpcServiceConfig rpcServiceConfig);

}
