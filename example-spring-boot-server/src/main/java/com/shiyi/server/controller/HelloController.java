package com.shiyi.server.controller;

import com.shiyi.HelloService;
import com.shiyi.annotation.RpcReference;
import com.shiyi.remoting.dto.RpcResponse;
import com.shiyi.remoting.transport.netty.client.UnprocessedRequests;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @Author:shiyi
 * @create: 2023-05-24  17:18
 */
@Slf4j
@RestController
public class HelloController implements Serializable {


    @RpcReference()
    private HelloService helloService;

    @GetMapping("/hello")
    public String test(){
        long start = System.currentTimeMillis();
//        log.info("client主动远程调用hello()");
        helloService.hello();
        CompletableFuture<RpcResponse<Object>> future1 = UnprocessedRequests.getFuture();
//        helloService.hello();
//        CompletableFuture<RpcResponse<Object>> future2 = UnprocessedRequests.getFuture();
//        helloService.hello();
//        CompletableFuture<RpcResponse<Object>> future3 = UnprocessedRequests.getFuture();
        try {
            System.out.println(future1.get());
//            System.out.println(future2.get());
//            System.out.println(future3.get());
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
