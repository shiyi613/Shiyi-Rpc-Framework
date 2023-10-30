package com.shiyi.server;

import com.shiyi.annotation.EnableRpc;
import com.shiyi.annotation.RpcScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@RpcScan(basePackage = {"com.shiyi.server.service"})
@EnableRpc
@ComponentScan("com.shiyi")
@SpringBootApplication
public class ExampleSpringBootServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleSpringBootServerApplication.class, args);
    }

}
