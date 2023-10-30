package com.shiyi.serviceImpl;

import com.shiyi.Hello;
import com.shiyi.HelloService;
import com.shiyi.annotation.RpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * @Author:shiyi
 * @create: 2023-05-19  0:05
 */
@RpcService
@Slf4j
public class HelloServiceImpl implements HelloService {

    static {
        log.info("HelloServiceImpl1被创建");
    }

    @Override
    public String hello(Hello hello) {
        log.info("HelloServiceImpl1收到: {}.", hello.getMessage());
        String result = "Hello description is " + hello.getDescription();
        log.info("HelloServiceImpl1返回: {}.", result);
        return result;
    }

    @Override
    public void hello() {

    }
}
