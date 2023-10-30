package com.shiyi;

import com.shiyi.annotation.EnableRpc;
import com.shiyi.annotation.RpcScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;


/**
 * @Author:shiyi
 * @create: 2023-05-22  15:59
 */
@ComponentScan("com.shiyi")
@EnableRpc
@RpcScan(basePackage = "com.shiyi")
public class NettyServerMain {
    public static void main(String[] args) {
//        NettyRpcServer nettyRpcServer = new NettyRpcServer();
//        RpcServiceConfig rpcServiceConfig = new RpcServiceConfig();
//        rpcServiceConfig.setVersion("v1.0");
//        rpcServiceConfig.setGroup("g1");
//        rpcServiceConfig.setClazz(HelloServiceImpl.class);
//        nettyRpcServer.registerService(rpcServiceConfig);
//        nettyRpcServer.start();

        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyServerMain.class);

    }
}
