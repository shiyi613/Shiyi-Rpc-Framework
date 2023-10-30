package com.shiyi.client.service;

import com.shiyi.Hello;
import com.shiyi.HelloService;
import com.shiyi.annotation.RpcService;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * @Author:shiyi
 * @create: 2023-05-24  17:17
 */
//@DubboService
@RpcService
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(Hello hello) {
        return null;
    }

    @Override
    public void hello() {
        System.out.println("666666");
    }
}
