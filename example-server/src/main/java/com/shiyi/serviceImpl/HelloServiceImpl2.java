package com.shiyi.serviceImpl;

import com.shiyi.Hello;
import com.shiyi.HelloService;
import com.shiyi.annotation.RpcService;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author:shiyi
 * @create: 2023-05-25  21:21
 */
@Slf4j
@RpcService
public class HelloServiceImpl2 implements HelloService {
    static {
        log.info("HelloServiceImpl2被创建");
    }

    @Override
    public String hello(Hello hello) {
        log.info("HelloServiceImpl2收到: {}.", hello.getMessage());
        String result = "Hello description is " + hello.getDescription();
        log.info("HelloServiceImpl2返回: {}.", result);
        return result;
    }

    @Override
    public void hello() {

    }
}
