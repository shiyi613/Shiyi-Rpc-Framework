package com.shiyi.client.controller;


import com.shiyi.UserService;
import com.shiyi.annotation.RpcReference;
import com.shiyi.remoting.dto.RpcResponse;
import com.shiyi.remoting.transport.netty.client.ResultFuture;
import com.shiyi.remoting.transport.netty.client.UnprocessedRequests;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @Author:shiyi
 * @create: 2023-05-18  20:23
 */
@RestController
@Slf4j
public class HelloController {

//    @DubboReference
    @RpcReference(async = true)
    private UserService userService;

    @GetMapping("/hello")
    public String test(){
        log.info("client主动远程调用hello()");
        long start = System.currentTimeMillis();
        userService.hello();
        ResultFuture future1 = ResultFuture.getFuture();
        userService.hello();
        ResultFuture future2 = ResultFuture.getFuture();
        userService.hello();
        ResultFuture future3 = ResultFuture.getFuture();
        try {
            System.out.println(future1.get());
            System.out.println(future2.get());
            System.out.println(future3.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        System.out.println("耗时：" + (end - start));
        return "1";
    }

}
