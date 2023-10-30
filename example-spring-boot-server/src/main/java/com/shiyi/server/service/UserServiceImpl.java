package com.shiyi.server.service;

import com.shiyi.UserService;
import com.shiyi.annotation.RpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Author:shiyi
 * @create: 2023-05-18  20:19
 */
@RpcService
@Component
@Slf4j
public class UserServiceImpl implements UserService {

    private int count = 0;

    @Override
    public String hello() {
        log.info("hello()被调用");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return String.valueOf(++count);
    }
}
