package com.shiyi;

import com.shiyi.config.CustomShutdownHook;
import com.shiyi.config.RpcServiceConfig;
import com.shiyi.remoting.transport.socket.SocketRpcServer;
import com.shiyi.serviceImpl.HelloServiceImpl;

/**
 * @Author:shiyi
 * @create: 2023-05-19  0:08
 */
public class SocketServerMain {

    public static void main(String[] args) {
        HelloServiceImpl helloService = new HelloServiceImpl();
        SocketRpcServer socketRpcServer = new SocketRpcServer();
//        CustomShutdownHook.getCustomShutdownHook().clearAll();
        RpcServiceConfig rpcServiceConfig = new RpcServiceConfig();
        rpcServiceConfig.setClazz(HelloServiceImpl.class);
        rpcServiceConfig.setVersion("v1.0");
        rpcServiceConfig.setGroup("g1");
        socketRpcServer.registerService(rpcServiceConfig);
        socketRpcServer.start();
    }
}
