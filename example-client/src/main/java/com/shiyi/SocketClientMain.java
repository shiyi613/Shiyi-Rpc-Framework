package com.shiyi;

import com.shiyi.config.CustomShutdownHook;
import com.shiyi.config.RpcServiceConfig;
import com.shiyi.proxy.RpcClientProxy;
import com.shiyi.remoting.transport.socket.SocketRpcClient;
import com.shiyi.utils.concurrent.threadpool.ThreadPoolFactoryUtil;

/**
 * @Author:shiyi
 * @create: 2023-05-19  0:14
 */
public class SocketClientMain {

    public static void main(String[] args) throws InterruptedException {
        SocketRpcClient socketRpcClient = new SocketRpcClient();
        RpcServiceConfig rpcServiceConfig = new RpcServiceConfig();
        rpcServiceConfig.setGroup("g1");
        rpcServiceConfig.setVersion("v1.0");
        RpcClientProxy rpcClientProxy = new RpcClientProxy(socketRpcClient, rpcServiceConfig);
        HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
        for (int i = 0; i < 5; i++) {
            System.out.println(helloService.hello(new Hello("shiyi", "66666")));
        }
    }
}
