package com.shiyi;

import com.shiyi.annotation.RpcReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * @Author:shiyi
 * @create: 2023-05-19  0:11
 */
@Component
public class HelloController {

    @RpcReference
    private HelloService helloService;

    public void test(){
        for (int i = 0; i < 10; i++) {
            System.out.println(helloService.hello(new Hello("111", "222")));
        }
    }
}
