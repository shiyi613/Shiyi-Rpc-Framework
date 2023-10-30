package com.shiyi.client;

import com.shiyi.client.controller.HelloController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ExampleSpringBootClientApplicationTests {

    @Autowired
    private HelloController helloController;

    @Test
    void contextLoads() {
        helloController.test();
    }

}
