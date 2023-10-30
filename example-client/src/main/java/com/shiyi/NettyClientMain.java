package com.shiyi;

import com.shiyi.annotation.EnableRpc;
import com.shiyi.config.RpcServiceConfig;
import com.shiyi.proxy.RpcClientProxy;
import com.shiyi.remoting.transport.netty.client.NettyRpcClient;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

/**
 * @Author:shiyi
 * @create: 2023-05-22  15:59
 */
@EnableRpc
@ComponentScan("com.shiyi")
public class NettyClientMain {
    public static void main(String[] args) {
        NettyRpcClient nettyRpcClient = new NettyRpcClient();
        RpcServiceConfig rpcServiceConfig = new RpcServiceConfig();
        RpcClientProxy proxy = new RpcClientProxy(nettyRpcClient, rpcServiceConfig);
        HelloService service = proxy.getProxy(HelloService.class);
        System.out.println(service.hello(new Hello("shiyi", "66666")));

//        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyClientMain.class);
//        HelloController helloController = (HelloController) applicationContext.getBean("helloController");
//        helloController.test();
    }
}
