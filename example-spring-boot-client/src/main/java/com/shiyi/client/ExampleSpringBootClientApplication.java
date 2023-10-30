package com.shiyi.client;

import com.shiyi.annotation.EnableRpc;
import com.shiyi.annotation.RpcScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableRpc
@RpcScan(basePackage = "com.shiyi.client.service")
@SpringBootApplication
public class ExampleSpringBootClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleSpringBootClientApplication.class, args);
    }

}
