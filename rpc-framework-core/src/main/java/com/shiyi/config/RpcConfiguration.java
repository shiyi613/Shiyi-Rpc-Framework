package com.shiyi.config;

/**
 * the holder for RpcConfigurationProperties
 *
 * @Author:shiyi
 * @create: 2023-05-24  0:25
 */
public class RpcConfiguration {

    private static RpcProperties rpcConfig = null;

    public RpcConfiguration(RpcProperties rpcProperties){
        rpcConfig = rpcProperties;
    }

    public static RpcProperties getRpcConfig(){
        return rpcConfig;
    }


}
